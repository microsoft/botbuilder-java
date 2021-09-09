// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.dialogs;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserTokenProvider;
import com.microsoft.bot.builder.skills.BotFrameworkSkill;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryOptions;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.DeliveryModes;
import com.microsoft.bot.schema.ExpectedReplies;
import com.microsoft.bot.schema.OAuthCard;
import com.microsoft.bot.schema.SignInConstants;
import com.microsoft.bot.schema.TokenExchangeInvokeRequest;
import com.microsoft.bot.schema.TokenExchangeRequest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A specialized {@link Dialog} that can wrap remote calls to a skill.
 *
 * The options parameter in BeginDialog must be a
 * {@link BeginSkillDialogOptions} instancewith the initial parameters for the
 * dialog.
 */
public class SkillDialog extends Dialog {

    private SkillDialogOptions dialogOptions;

    private final String deliverModeStateKey = "deliverymode";
    private final String skillConversationIdStateKey = "Microsoft.Bot.Builder.Dialogs.SkillDialog.SkillConversationId";

    /**
     * Initializes a new instance of the {@link SkillDialog} class to wrap remote
     * calls to a skill.
     *
     * @param dialogOptions The options to execute the skill dialog.
     * @param dialogId      The id of the dialog.
     */
    public SkillDialog(SkillDialogOptions dialogOptions, String dialogId) {
        super(dialogId);
        if (dialogOptions == null) {
            throw new IllegalArgumentException("dialogOptions cannot be null.");
        }

        this.dialogOptions = dialogOptions;
    }

    /**
     * Called when the skill dialog is started and pushed onto the dialog stack.
     *
     * @param dc      The {@link DialogContext} for the current turn of
     *                conversation.
     * @param options Optional, initial information to pass to the dialog.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog.
     */
    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
        BeginSkillDialogOptions dialogArgs = validateBeginDialogArgs(options);

        // Create deep clone of the original activity to avoid altering it before
        // forwarding it.
        Activity skillActivity = Activity.clone(dialogArgs.getActivity());

        // Apply conversation reference and common properties from incoming activity
        // before sending.
        ConversationReference conversationReference = dc.getContext().getActivity().getConversationReference();
        skillActivity.applyConversationReference(conversationReference, true);

        // Store delivery mode and connection name in dialog state for later use.
        dc.getActiveDialog().getState().put(deliverModeStateKey, dialogArgs.getActivity().getDeliveryMode());

        // Create the conversationId and store it in the dialog context state so we can
        // use it later
        return createSkillConversationId(dc.getContext(), dc.getContext().getActivity())
                .thenCompose(skillConversationId -> {
                    dc.getActiveDialog().getState().put(skillConversationIdStateKey, skillConversationId);

                    // Send the activity to the skill.
                    return sendToSkill(dc.getContext(), skillActivity, skillConversationId).thenCompose(eocActivity -> {
                        if (eocActivity != null) {
                            return dc.endDialog(eocActivity.getValue());
                        }
                        return CompletableFuture.completedFuture(END_OF_TURN);
                    });
                });
    }

    /**
     * Called when the skill dialog is _continued_, where it is the active dialog
     * and the user replies with a new activity.
     *
     * @param dc The {@link DialogContext} for the current turn of conversation.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog. The
     *         result may also contain a return value.
     */
    @Override
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {

        Boolean interrupted = dc.getState().getValue(TurnPath.INTERRUPTED, false, Boolean.class);
        if (interrupted) {
            dc.getState().setValue(TurnPath.INTERRUPTED, false);
            return resumeDialog(dc, DialogReason.END_CALLED);
        }


        if (!onValidateActivity(dc.getContext().getActivity())) {
            return CompletableFuture.completedFuture(END_OF_TURN);
        }

        // Handle EndOfConversation from the skill (this will be sent to the this dialog
        // by the SkillHandler
        // if received from the Skill)
        if (dc.getContext().getActivity().getType().equals(ActivityTypes.END_OF_CONVERSATION)) {
            return dc.endDialog(dc.getContext().getActivity().getValue());
        }

        // Create deep clone of the original activity to avoid altering it before
        // forwarding it.
        Activity skillActivity = Activity.clone(dc.getContext().getActivity());
        if (dc.getActiveDialog().getState().get(deliverModeStateKey) != null) {
            skillActivity.setDeliveryMode((String) dc.getActiveDialog().getState().get(deliverModeStateKey));
        }

        String skillConversationId = (String) dc.getActiveDialog().getState().get(skillConversationIdStateKey);

        // Just forward to the remote skill
        return sendToSkill(dc.getContext(), skillActivity, skillConversationId).thenCompose(eocActivity -> {
            if (eocActivity != null) {
                return dc.endDialog(eocActivity.getValue());
            }

            return CompletableFuture.completedFuture(END_OF_TURN);
        });
    }

    /**
     * Called when the skill dialog should re-prompt the user for input.
     *
     * @param turnContext The context Object for this turn.
     * @param instance    State information for this dialog.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    public CompletableFuture<Void> repromptDialog(TurnContext turnContext, DialogInstance instance) {
        // Create and send an envent to the skill so it can resume the dialog.
        Activity repromptEvent = Activity.createEventActivity();
        repromptEvent.setName(DialogEvents.REPROMPT_DIALOG);

        // Apply conversation reference and common properties from incoming activity
        // before sending.
        repromptEvent.applyConversationReference(turnContext.getActivity().getConversationReference(), true);

        String skillConversationId = (String) instance.getState().get(skillConversationIdStateKey);

        // connection Name instanceof not applicable for a RePrompt, as we don't expect
        // as OAuthCard in response.
        return sendToSkill(turnContext, (Activity) repromptEvent, skillConversationId).thenApply(result -> null);
    }

    /**
     * Called when a child skill dialog completed its turn, returning control to
     * this dialog.
     *
     * @param dc     The dialog context for the current turn of the conversation.
     * @param reason Reason why the dialog resumed.
     * @param result Optional, value returned from the dialog that was called. The
     *               type of the value returned is dependent on the child dialog.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
        return repromptDialog(dc.getContext(), dc.getActiveDialog()).thenCompose(x -> {
            return CompletableFuture.completedFuture(END_OF_TURN);
        });
    }

    /**
     * Called when the skill dialog is ending.
     *
     * @param turnContext The context Object for this turn.
     * @param instance    State information associated with the instance of this
     *                    dialog on the dialog stack.
     * @param reason      Reason why the dialog ended.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    public CompletableFuture<Void> endDialog(TurnContext turnContext, DialogInstance instance, DialogReason reason) {
        // Send of of conversation to the skill if the dialog has been cancelled.
        return onEndDialog(turnContext, instance, reason)
                .thenCompose(result -> super.endDialog(turnContext, instance, reason));
    }

    private CompletableFuture<Void> onEndDialog(TurnContext turnContext, DialogInstance instance, DialogReason reason) {
        if (reason == DialogReason.CANCEL_CALLED || reason == DialogReason.REPLACE_CALLED) {
            Activity activity = Activity.createEndOfConversationActivity();

            // Apply conversation reference and common properties from incoming activity
            // before sending.
            activity.applyConversationReference(turnContext.getActivity().getConversationReference(), true);
            activity.setChannelData(turnContext.getActivity().getChannelData());
            for (Map.Entry<String, JsonNode> entry : turnContext.getActivity().getProperties().entrySet()) {
                activity.setProperties(entry.getKey(), entry.getValue());
            }

            String skillConversationId = (String) instance.getState().get(skillConversationIdStateKey);

            // connection Name instanceof not applicable for an EndDialog, as we don't
            // expect as OAuthCard in response.
            return sendToSkill(turnContext, activity, skillConversationId).thenApply(result -> null);
        } else {
            return CompletableFuture.completedFuture(null);
        }

    }

    /**
     * Validates the activity sent during ContinueDialog .
     *
     * @param activity The {@link Activity} for the current turn of conversation.
     *
     *                 Override this method to implement a custom validator for the
     *                 activity being sent during the ContinueDialog . This
     *                 method can be used to ignore activities of a certain type if
     *                 needed. If this method returns false, the dialog will end the
     *                 turn without processing the activity.
     *
     * @return true if the activity is valid, false if not.
     */
    protected boolean onValidateActivity(Activity activity) {
        return true;
    }

    /**
     * Validates the required properties are set in the options argument passed to
     * the BeginDialog call.
     */
    private static BeginSkillDialogOptions validateBeginDialogArgs(Object options) {
        if (options == null) {
            throw new IllegalArgumentException("options cannot be null.");
        }

        if (!(options instanceof BeginSkillDialogOptions)) {
            throw new IllegalArgumentException("Unable to cast options to beginSkillDialogOptions}");
        }

        BeginSkillDialogOptions dialogArgs = (BeginSkillDialogOptions) options;

        if (dialogArgs.getActivity() == null) {
            throw new IllegalArgumentException("dialogArgs.getActivity is null in options");
        }

        return dialogArgs;
    }

    private CompletableFuture<Activity> sendToSkill(TurnContext context, Activity activity,
            String skillConversationId) {
        if (activity.getType().equals(ActivityTypes.INVOKE)) {
            // Force ExpectReplies for invoke activities so we can get the replies right
            // away and send them
            // back to the channel if needed. This makes sure that the dialog will receive
            // the Invoke response
            // from the skill and any other activities sent, including EoC.
            activity.setDeliveryMode(DeliveryModes.EXPECT_REPLIES.toString());
        }

        // Always save state before forwarding
        // (the dialog stack won't get updated with the skillDialog and things won't
        // work if you don't)
        getDialogOptions().getConversationState().saveChanges(context, true);

        BotFrameworkSkill skillInfo = getDialogOptions().getSkill();
        return getDialogOptions().getSkillClient()
                .postActivity(getDialogOptions().getBotId(), skillInfo.getAppId(), skillInfo.getSkillEndpoint(),
                        getDialogOptions().getSkillHostEndpoint(), skillConversationId, activity, Object.class)
                .thenCompose(response -> {
                    // Inspect the skill response status
                    if (!response.getIsSuccessStatusCode()) {
                        return Async.completeExceptionally(new SkillInvokeException(String.format(
                                "Error invoking the skill id: %s at %s  (status is %s).  %s", skillInfo.getId(),
                                skillInfo.getSkillEndpoint(), response.getStatus(), response.getBody())));
                    }

                    ExpectedReplies replies = null;
                    if (response.getBody() instanceof ExpectedReplies) {
                        replies = (ExpectedReplies) response.getBody();
                    }

                    Activity eocActivity = null;
                    if (activity.getDeliveryMode() != null
                            && activity.getDeliveryMode().equals(DeliveryModes.EXPECT_REPLIES.toString())
                            && replies.getActivities() != null && replies.getActivities().size() > 0) {
                        // Track sent invoke responses, so more than one instanceof not sent.
                        boolean sentInvokeResponse = false;

                        // Process replies in the response.getBody().
                        for (Activity activityFromSkill : replies.getActivities()) {
                            if (activityFromSkill.getType().equals(ActivityTypes.END_OF_CONVERSATION)) {
                                // Capture the EndOfConversation activity if it was sent from skill
                                eocActivity = activityFromSkill;

                                // The conversation has ended, so cleanup the conversation id.
                                getDialogOptions().getConversationIdFactory()
                                        .deleteConversationReference(skillConversationId).join();
                            } else if (!sentInvokeResponse && interceptOAuthCards(context, activityFromSkill,
                                    getDialogOptions().getConnectionName()).join()) {
                                // do nothing. Token exchange succeeded, so no OAuthCard needs to be shown to
                                // the user
                                sentInvokeResponse = true;
                            } else {
                                if (activityFromSkill.getType().equals(ActivityTypes.INVOKE_RESPONSE)) {
                                    // An invoke respones has already been sent. This instanceof a bug in the skill.
                                    // Multiple invoke responses are not possible.
                                    if (sentInvokeResponse) {
                                        continue;
                                    }

                                    sentInvokeResponse = true;

                                    // Not sure this is needed in Java, looks like a workaround for some .NET issues
                                    // Ensure the value in the invoke response instanceof of type InvokeResponse
                                    // (it gets deserialized as JObject by default).

                                    // if (activityFromSkill.getValue() instanceof JObject jObject) {
                                    // activityFromSkill.setValue(jObject.ToObject<InvokeResponse>());
                                    // }
                                }

                                // Send the response back to the channel.
                                context.sendActivity(activityFromSkill);
                            }
                        }
                    }

                    return CompletableFuture.completedFuture(eocActivity);

                });
    }

    /**
     * Tells is if we should intercept the OAuthCard message.
     *
     * The SkillDialog only attempts to intercept OAuthCards when the following
     * criteria are met: 1. An OAuthCard was sent from the skill 2. The SkillDialog
     * was called with a connectionName 3. The current adapter supports token
     * exchange If any of these criteria are false, return false.
     */
    private CompletableFuture<Boolean> interceptOAuthCards(TurnContext turnContext, Activity activity,
            String connectionName) {

        UserTokenProvider tokenExchangeProvider;

        if (StringUtils.isEmpty(connectionName) || !(turnContext.getAdapter() instanceof UserTokenProvider)) {
            // The adapter may choose not to support token exchange,
            // in which case we fallback to showing an oauth card to the user.
            return CompletableFuture.completedFuture(false);
        } else {
            tokenExchangeProvider = (UserTokenProvider) turnContext.getAdapter();
        }

        Attachment oauthCardAttachment = null;

        if (activity.getAttachments() != null) {
            Optional<Attachment> optionalAttachment = activity.getAttachments().stream()
                    .filter(a -> a.getContentType() != null && a.getContentType().equals(OAuthCard.CONTENTTYPE))
                    .findFirst();
            if (optionalAttachment.isPresent()) {
                oauthCardAttachment = optionalAttachment.get();
            }
        }

        if (oauthCardAttachment != null) {
            OAuthCard oauthCard = (OAuthCard) oauthCardAttachment.getContent();
            if (oauthCard != null && oauthCard.getTokenExchangeResource() != null
                    && !StringUtils.isEmpty(oauthCard.getTokenExchangeResource().getUri())) {
                try {
                    return tokenExchangeProvider
                            .exchangeToken(turnContext, connectionName, turnContext.getActivity().getFrom().getId(),
                                    new TokenExchangeRequest(oauthCard.getTokenExchangeResource().getUri(), null))
                            .thenCompose(result -> {
                                if (result != null && !StringUtils.isEmpty(result.getToken())) {
                                    // If token above instanceof null, then SSO has failed and hence we return
                                    // false.
                                    // If not, send an invoke to the skill with the token.
                                    return sendTokenExchangeInvokeToSkill(activity,
                                        oauthCard.getTokenExchangeResource().getId(), oauthCard.getConnectionName(),
                                            result.getToken());
                                } else {
                                    return CompletableFuture.completedFuture(false);
                                }

                            });
                } catch (Exception ex) {
                    // Failures in token exchange are not fatal. They simply mean that the user
                    // needs
                    // to be shown the OAuth card.
                    return CompletableFuture.completedFuture(false);
                }
            }
        }
        return CompletableFuture.completedFuture(false);
    }

    // private CompletableFuture<Boolean> interceptOAuthCards(TurnContext turnContext, Activity activity,
    //         String connectionName) {

    //     UserTokenProvider tokenExchangeProvider;

    //     if (StringUtils.isEmpty(connectionName) || !(turnContext.getAdapter() instanceof UserTokenProvider)) {
    //         // The adapter may choose not to support token exchange,
    //         // in which case we fallback to showing an oauth card to the user.
    //         return CompletableFuture.completedFuture(false);
    //     } else {
    //         tokenExchangeProvider = (UserTokenProvider) turnContext.getAdapter();
    //     }

    //     Attachment oauthCardAttachment = null;

    //     if (activity.getAttachments() != null) {
    //         Optional<Attachment> optionalAttachment = activity.getAttachments().stream()
    //                 .filter(a -> a.getContentType() != null && a.getContentType().equals(OAuthCard.CONTENTTYPE))
    //                 .findFirst();
    //         if (optionalAttachment.isPresent()) {
    //             oauthCardAttachment = optionalAttachment.get();
    //         }
    //     }

    //     if (oauthCardAttachment != null) {
    //         OAuthCard oauthCard = (OAuthCard) oauthCardAttachment.getContent();
    //         if (oauthCard != null && oauthCard.getTokenExchangeResource() != null
    //                 && !StringUtils.isEmpty(oauthCard.getTokenExchangeResource().getUri())) {
    //             try {
    //                 TokenResponse result = tokenExchangeProvider
    //                         .exchangeToken(turnContext, connectionName, turnContext.getActivity().getFrom().getId(),
    //                                 new TokenExchangeRequest(oauthCard.getTokenExchangeResource().getUri(), null))
    //                         .join();

    //                 if (result != null && !StringUtils.isEmpty(result.getToken())) {
    //                     // If token above instanceof null, then SSO has failed and hence we return
    //                     // false.
    //                     // If not, send an invoke to the skill with the token.
    //                     return sendTokenExchangeInvokeToSkill(activity, oauthCard.getTokenExchangeResource().getId(),
    //                             oauthCard.getConnectionName(), result.getToken());
    //                 }
    //             } catch (Exception ex) {
    //                 // Failures in token exchange are not fatal. They simply mean that the user
    //                 // needs
    //                 // to be shown the OAuth card.
    //                 return CompletableFuture.completedFuture(false);
    //             }
    //         }
    //     }

    //     return CompletableFuture.completedFuture(false);
    // }


    private CompletableFuture<Boolean> sendTokenExchangeInvokeToSkill(Activity incomingActivity, String id,
            String connectionName, String token) {
        Activity activity = incomingActivity.createReply();
        activity.setType(ActivityTypes.INVOKE);
        activity.setName(SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME);
        TokenExchangeInvokeRequest tokenRequest = new TokenExchangeInvokeRequest();
        tokenRequest.setId(id);
        tokenRequest.setToken(token);
        tokenRequest.setConnectionName(connectionName);
        activity.setValue(tokenRequest);

        // route the activity to the skill
        BotFrameworkSkill skillInfo = getDialogOptions().getSkill();
        return getDialogOptions().getSkillClient()
                .postActivity(getDialogOptions().getBotId(), skillInfo.getAppId(), skillInfo.getSkillEndpoint(),
                        getDialogOptions().getSkillHostEndpoint(), incomingActivity.getConversation().getId(), activity,
                        Object.class)
                .thenApply(response -> response.getIsSuccessStatusCode());
    }

    private CompletableFuture<String> createSkillConversationId(TurnContext context, Activity activity) {
        // Create a conversationId to interact with the skill and send the activity
        SkillConversationIdFactoryOptions conversationIdFactoryOptions = new SkillConversationIdFactoryOptions();
        conversationIdFactoryOptions.setFromBotOAuthScope(context.getTurnState().get(BotAdapter.OAUTH_SCOPE_KEY));
        conversationIdFactoryOptions.setFromBotId(getDialogOptions().getBotId());
        conversationIdFactoryOptions.setActivity(activity);
        conversationIdFactoryOptions.setBotFrameworkSkill(getDialogOptions().getSkill());

        return getDialogOptions().getConversationIdFactory().createSkillConversationId(conversationIdFactoryOptions);
    }

    /**
     * Gets the options used to execute the skill dialog.
     *
     * @return the DialogOptions value as a SkillDialogOptions.
     */
    protected SkillDialogOptions getDialogOptions() {
        return this.dialogOptions;
    }

}
