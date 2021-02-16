// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.dialogs;

import java.util.concurrent.CompletableFuture;

import com.ctc.wstx.io.CompletelyCloseable;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

/**
 * A specialized {@link Dialog} that can wrap remote calls to a skill.
 *
 * The options parameter in {@link BeginDialog} must be a
 * {@link BeginSkillDialogOptions} instancewith the initial parameters for the
 * dialog.
 */
public class SkillDialog extends Dialog {

    private SkillDialogOptions dialogOptions;

    private final String DeliverModeStateKey = "deliverymode";
    private final String SkillConversationIdStateKey = "Microsoft.Bot.Builder.Dialogs.SkillDialog.SkillConversationId";

    /**
     * Initializes a new instance of the {@link SkillDialog} class to wrap
     * remote calls to a skill.
     *
     * @param dialogOptions  The options to execute the skill dialog.
     * @param dialogId       The id of the dialog.
     */
    public SkillDialog(SkillDialogOptions dialogOptions, String dialogId) {
        super(dialogId);
        if (dialogOptions == null) {
            throw new  IllegalArgumentException("dialogOptions cannot be null.");
        }

        this.dialogOptions = dialogOptions;
    }

    /**
     * Called when the skill dialog is started and pushed onto the dialog
     * stack.
     *
     * @param dc       The {@link DialogContext} for the current turn of
     *                 conversation.
     * @param options  Optional, initial information to pass to the
     *                 dialog.
     *
     * @return   A {@link CompletableFuture} representing the
     *           asynchronous operation.
     *
     * If the task is successful, the result indicates whether the dialog is
     * still active after the turn has been processed by the dialog.
     */
    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
        BeginSkillDialogOptions dialogArgs = validateBeginDialogArgs(options);

        // Create deep clone of the original activity to avoid altering it before forwarding it.
        Activity skillActivity = ObjectPath.clone(dialogArgs.getActivity());

        // Apply conversation reference and common properties from incoming activity before sending.
        skillActivity.applyConversationReference(dc.getContext().getActivity().getConversationReference(), true);

        // Store delivery mode and connection name in dialog state for later use.
        dc.getActiveDialog().getState().put(DeliverModeStateKey, dialogArgs.getActivity().getDeliveryMode());

        // Create the conversationId and store it in the dialog context state so we can use it later
        String skillConversationId =  createSkillConversationId(dc.getContext(), dc.getContext().getActivity()).join();
        dc.getActiveDialog().getState().put(SkillConversationIdStateKey, skillConversationId);

        // Send the activity to the skill.
        Activity eocActivity =  sendToSkill(dc.getContext(), skillActivity, skillConversationId).join();
        if (eocActivity != null) {
            return dc.endDialog(eocActivity.getValue());
        }

        return CompletableFuture.completedFuture(END_OF_TURN);
    }

    /**
     * Called when the skill dialog is _continued_, where it is the active
     * dialog and the user replies with a new activity.
     *
     * @param dc  The {@link DialogContext} for the current turn of
     *            conversation.
     *
     * @return   A {@link CompletableFuture} representing the
     *           asynchronous operation.
     *
     * If the task is successful, the result indicates whether the dialog is
     * still active after the turn has been processed by the dialog. The result
     * may also contain a return value.
     */
    @Override
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
        if (!onValidateActivity(dc.getContext().getActivity())) {
            return CompletableFuture.completedFuture(END_OF_TURN);
        }

        // Handle EndOfConversation from the skill (this will be sent to the this dialog by the SkillHandler if received from the Skill)
        if (dc.getContext().getActivity().getType() == ActivityTypes.END_OF_CONVERSATION) {
            return  dc.endDialog(dc.getContext().getActivity().getValue());
        }

        // Create deep clone of the original activity to avoid altering it before forwarding it.
        Activity skillActivity = ObjectPath.clone(dc.getContext().getActivity());

        skillActivity.setDeliveryMode((String) dc.getActiveDialog().getState().get(DeliverModeStateKey));

        String skillConversationId = (String) dc.getActiveDialog().getState().get(SkillConversationIdStateKey);

        // Just forward to the remote skill
        Activity eocActivity =  sendToSkill(dc.getContext(), skillActivity, skillConversationId).join();
        if (eocActivity != null) {
            return  dc.endDialog(eocActivity.getValue());
        }

        return CompletableFuture.completedFuture(END_OF_TURN);
    }

    /**
     * Called when the skill dialog should re-prompt the user for input.
     *
     * @param turnContext  The context Object for this turn.
     * @param instance     State information for this dialog.
     *
     * @return   A {@link CompletableFuture} representing the
     *           asynchronous operation.
     */
    @Override
    public CompletableFuture<Void> repromptDialog(TurnContext turnContext, DialogInstance instance) {
        // Create and send an envent to the skill so it can resume the dialog.
        var repromptEvent = Activity.CreateEventActivity();
        repromptEvent.setName(DialogEvents.getRepromptDialog());

        // Apply conversation reference and common properties from incoming activity before sending.
        repromptEvent.ApplyConversationReference(turnContext.getActivity().GetConversationReference(), true);

        var skillConversationId = (String)instance.State[SkillConversationIdStateKey];

        // connection Name instanceof not applicable for a RePrompt, as we don't expect as OAuthCard in response.
         SendToSkill(turnContext, (Activity)repromptEvent, skillConversationId);
    }

    /**
     * Called when a child skill dialog completed its turn, returning control
     * to this dialog.
     *
     * @param dc      The dialog context for the current turn of the
     *                conversation.
     * @param reason  Reason why the dialog resumed.
     * @param result  Optional, value returned from the dialog that was
     *                called. The type of the value returned is dependent on the child dialog.
     *
     * @return   A {@link CompletableFuture} representing the
     *           asynchronous operation.
     */
    @Override
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
         RepromptDialog(dc.getContext(), dc.ActiveDialog);
        return EndOfTurn;
    }

    /**
     * Called when the skill dialog is ending.
     *
     * @param turnContext  The context Object for this turn.
     * @param instance     State information associated with the
     *                     instance of this dialog on the dialog stack.
     * @param reason       Reason why the dialog ended.
     *
     * @return   A {@link CompletableFuture} representing the
     *           asynchronous operation.
     */
    @Override
    public CompletableFuture<Void> endDialog(TurnContext turnContext, DialogInstance instance, DialogReason reason) {
        // Send of of conversation to the skill if the dialog has been cancelled.
        if (reason == DialogReason.CancelCalled || reason == DialogReason.ReplaceCalled) {
            var activity = (Activity)Activity.CreateEndOfConversationActivity();

            // Apply conversation reference and common properties from incoming activity before sending.
            activity.ApplyConversationReference(turnContext.getActivity().GetConversationReference(), true);
            activity.setChannelData(turnContext.getActivity().getChannelData());
            activity.setProperties(turnContext.getActivity().getProperties());

            var skillConversationId = (String)instance.State[SkillConversationIdStateKey];

            // connection Name instanceof not applicable for an EndDialog, as we don't expect as OAuthCard in response.
             SendToSkill(turnContext, activity, skillConversationId);
        }

         super.EndDialog(turnContext, instance, reason);
    }

    /**
     * Validates the activity sent during {@link ContinueDialog} .
     *
     * @param activity  The {@link Activity} for the current turn of
     *                  conversation.
     *
     * Override this method to implement a custom validator for the activity
     * being sent during the {@link ContinueDialog} . This method can be used
     * to ignore activities of a certain type if needed. If this method returns
     * false, the dialog will end the turn without processing the activity.
     *
     * @return   true if the activity is valid, false if not.
     */
    protected boolean onValidateActivity(Activity activity) {
        return true;
    }

    /**
     * Validates the required properties are set in the options argument passed
     * to the BeginDialog call.
     */
    private static BeginSkillDialogOptions validateBeginDialogArgs(Object options) {
        if (options == null) {
            throw new  IllegalArgumentException("options cannot be null.");
        }

        if (!(options instanceof BeginSkillDialogOptions dialogArgs)) {
            throw new IllegalArgumentException($"Unable to cast {nameof(options)} to {nameof(BeginSkillDialogOptions)}", nameof(options));
        }

        if (dialogArgs.Activity == null) {
            throw new ArgumentNullException(nameof(options), $"{nameof(dialogArgs.Activity)} instanceof null in {nameof(options)}");
        }

        return dialogArgs;
    }

    private CompletableFuture<Activity> sendToSkill(TurnContext context, Activity activity, String skillConversationId) {
        if (activity.Type == ActivityTypes.Invoke) {
            // Force ExpectReplies for invoke activities so we can get the replies right away and send them back to the channel if needed.
            // This makes sure that the dialog will receive the Invoke response from the skill and any other activities sent, including EoC.
            activity.setDeliveryMode(DeliveryModes.getExpectReplies());
        }

        // Always save state before forwarding
        // (the dialog stack won't get updated with the skillDialog and things won't work if you don't)
         getDialogOptions().getConversationState().SaveChanges(context, true);

        var skillInfo = getDialogOptions().getSkill();
        var response =  getDialogOptions().getSkillClient().PostActivity<ExpectedReplies>(getDialogOptions().getBotId(), skillInfo.getAppId(), skillInfo.getSkillEndpoint(), getDialogOptions().getSkillHostEndpoint(), skillConversationId, activity);

        // Inspect the skill response status
        if (!response.IsSuccessStatusCode()) {
            throw new HttpRequestException($"Error invoking the skill id: \"{skillInfo.Id}\" at \"{skillInfo.SkillEndpoint}\" (status instanceof {response.Status}). \r\n {response.Body}");
        }

        Activity eocActivity;
        if (activity.DeliveryMode == DeliveryModes.ExpectReplies && response.getBody().Activities != null && response.getBody().getActivities().Any()) {
            // Track sent invoke responses, so more than one instanceof not sent.
            boolean sentInvokeResponse = false;

            // Process replies in the response.getBody().
            foreach (var activityFromSkill in response.getBody().getActivities()) {
                if (activityFromSkill.Type == ActivityTypes.EndOfConversation) {
                    // Capture the EndOfConversation activity if it was sent from skill
                    eocActivity = activityFromSkill;

                    // The conversation has ended, so cleanup the conversation id.
                     getDialogOptions().getConversationIdFactory().DeleteConversationReference(skillConversationId);
                } else if (!sentInvokeResponse &&  InterceptOAuthCards(context, activityFromSkill, getDialogOptions().ConnectionName)) {
                    // do nothing. Token exchange succeeded, so no OAuthCard needs to be shown to the user
                    sentInvokeResponse = true;
                } else {
                    if (activityFromSkill.Type == ActivityTypesEx.InvokeResponse) {
                        // An invoke respones has already been sent.  This instanceof a bug in the skill.  Multiple invoke responses
                        // are not possible.
                        if (sentInvokeResponse) {
                            continue;
                        }

                        sentInvokeResponse = true;

                        // Ensure the value in the invoke response instanceof of type InvokeResponse (it gets deserialized as JObject by default).
                        if (activityFromSkill.Value instanceof JObject jObject) {
                            activityFromSkill.setValue(jObject.ToObject<InvokeResponse>());
                        }
                    }

                    // Send the response back to the channel.
                     context.SendActivity(activityFromSkill);
                }
            }
        }

        return eocActivity;
    }

    /**
     * Tells is if we should intercept the OAuthCard message.
     *
     * The SkillDialog only attempts to intercept OAuthCards when the following
     * criteria are met: 1. An OAuthCard was sent from the skill 2. The
     * SkillDialog was called with a connectionName 3. The current adapter
     * supports token exchange If any of these criteria are false, return
     * false.
     */
    private CompletableFuture<bool> interceptOAuthCards(TurnContext turnContext, Activity activity, String connectionName) {
        if (StringUtils.isEmpty(connectionName) || !(turnContext.Adapter instanceof ExtendedUserTokenProvider tokenExchangeProvider)) {
            // The adapter may choose not to support token exchange, in which case we fallback to showing an oauth card to the user.
            return false;
        }

        var oauthCardAttachment = activity.Attachments?.FirstOrDefault(a -> a?.ContentType == OAuthCard.ContentType);
        if (oauthCardAttachment != null) {
            var oauthCard = ((JObject)oauthCardAttachment.Content).ToObject<OAuthCard>();
            if (!StringUtils.isEmpty(oauthCard?.TokenExchangeResource?.Uri)) {
                try {
                    var result =  tokenExchangeProvider.ExchangeToken(
                        turnContext,
                        connectionName,
                        turnContext.getActivity().getFrom().getId(),
                        new TokenExchangeRequest(oauthCard.getTokenExchangeResource().getUri()),
                        cancellationToken);

                    if (!StringUtils.isEmpty(result?.Token)) {
                        // If token above instanceof null, then SSO has failed and hence we return false.
                        // If not, send an invoke to the skill with the token.
                        return  SendTokenExchangeInvokeToSkill(activity, oauthCard.getTokenExchangeResource().getId(), oauthCard.getConnectionName(), result.Token);
                    }
                }
                catch {
                    // Failures in token exchange are not fatal. They simply mean that the user needs to be shown the OAuth card.
                    return false;
                }
            }
        }

        return false;
    }

    private CompletableFuture<bool> sendTokenExchangeInvokeToSkill(Activity incomingActivity, String id, String connectionName, String token) {
        var activity = incomingActivity.CreateReply();
        activity.setType(ActivityTypes.getInvoke());
        activity.setName(SignInConstants.getTokenExchangeOperationName());
        activity.setValue(new TokenExchangeInvokeRequest {
            Id = id,
            Token = token,
            ConnectionName = connectionName
        };

        // route the activity to the skill
        var skillInfo = getDialogOptions().getSkill();
        var response =  getDialogOptions().getSkillClient().PostActivity<ExpectedReplies>(getDialogOptions().getBotId(), skillInfo.getAppId(), skillInfo.getSkillEndpoint(), getDialogOptions().getSkillHostEndpoint(), incomingActivity.getConversation().getId(), activity);

        // Check response status: true if success, false if failure
        return response.IsSuccessStatusCode();
    }

    private CompletableFuture<String> createSkillConversationId(TurnContext context, Activity activity) {
        // Create a conversationId to interact with the skill and send the activity
        var conversationIdFactoryOptions = new SkillConversationIdFactoryOptions {
            FromBotOAuthScope = context.TurnState.Get<String>(BotAdapter.OAuthScopeKey),
            FromBotId = getDialogOptions().getBotId(),
            Activity = activity,
            BotFrameworkSkill = getDialogOptions().Skill
        };
        var skillConversationId =  getDialogOptions().getConversationIdFactory().CreateSkillConversationId(conversationIdFactoryOptions);
        return skillConversationId;
    }
    /**
     * Gets the options used to execute the skill dialog.
     * @return the DialogOptions value as a SkillDialogOptions.
     */
    protected SkillDialogOptions getDialogOptions() {
        return this.dialogOptions;
    }

}

