package com.microsoft.bot.dialogs;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import javax.lang.model.util.ElementScanner6;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.Service;
import com.microsoft.bot.builder.BotStateSet;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextStateCollection;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.dialogs.memory.DialogStateManagerConfiguration;

import org.joda.time.Instant;

/**
 * Class which runs the dialog system.
 */
public class DialogManager {

    private final String LastAccess = "_lastAccess";
    private String _rootDialogId;
    private final String _dialogStateProperty;

    /// <summary>
    /// Initializes a new instance of the <see cref="DialogManager"/> class.
    /// </summary>
    /// <param name="rootDialog">Root dialog to use.</param>
    /// <param name="dialogStateProperty">alternate name for the dialogState property. (Default is "DialogState").</param>
    public DialogManager(Dialog rootDialog, String dialogStateProperty)
    {
        if (rootDialog != null)
        {
            this.rootDialog = rootDialog;
        }

        if (dialogStateProperty == null)
            _dialogStateProperty = dialogStateProperty;
        else
            _dialogStateProperty = "DialogState";
    }

    /// <summary>
    /// Gets or sets the ConversationState.
    /// </summary>
    /// <value>
    /// The ConversationState.
    /// </value>
    public ConversationState conversationState;

    /// <summary>
    /// Gets or sets the UserState.
    /// </summary>
    /// <value>
    /// The UserState.
    /// </value>
    public UserState userState;

    /// <summary>
    /// Gets InitialTurnState collection to copy into the TurnState on every turn.
    /// </summary>
    /// <value>
    /// TurnState.
    /// </value>
    public TurnContextStateCollection initialTurnState = new TurnContextStateCollection();

    /// <summary>
    /// Gets or sets root dialog to use to start conversation.
    /// </summary>
    /// <value>
    /// Root dialog to use to start conversation.
    /// </value>
    public Dialog rootDialog;

    /// <summary>
    /// Gets or sets global dialogs that you want to have be callable.
    /// </summary>
    /// <value>Dialogs set.</value>
    @JsonIgnore
    public DialogSet dialogs = new DialogSet();

    /// <summary>
    /// Gets or sets the DialogStateManagerConfiguration.
    /// </summary>
    /// <value>
    /// The DialogStateManagerConfiguration.
    /// </value>
    public DialogStateManagerConfiguration stateConfiguration;

    /// <summary>
    /// Gets or sets (optional) number of milliseconds to expire the bot's state after.
    /// </summary>
    /// <value>
    /// Number of milliseconds.
    /// </value>
    public Integer expireAfter;

    /**
     * @param context
     * @return CompletableFuture<DialogManagerResult>
     */
    /// <summary>
    /// Runs dialog system in the context of an ITurnContext.
    /// </summary>
    /// <param name="context">turn context.</param>
    /// <param name="cancellationToken">Cancellation token.</param>
    /// <returns>result of the running the logic against the activity.</returns>
    public CompletableFuture<DialogManagerResult> OnTurnAsync(TurnContext context)
    {
        // Lazy initialize rootdialog so it can refer to assets like LG function templates
        if (_rootDialogId == null)
        {
            synchronized (this)
            {
                if (_rootDialogId == null)
                {
                    _rootDialogId = rootDialog.getId();
                    dialogs.setTelemetryClient(rootDialog.getTelemetryClient());
                    dialogs.add(rootDialog);
                    RegisterContainerDialogs(rootDialog, false);
                }
            }
        }

        BotStateSet botStateSet = new BotStateSet();

        // Preload TurnState with DM TurnState.
        initialTurnState.foreach(pair -> context.TurnState.Set(pair.Key, pair.Value));

        // register DialogManager with TurnState.
        context.TurnState.Set(this);

        if (ConversationState == null)
        {
            ConversationState = context.TurnState.Get();
            if (ConversationState == null)
                throw new UnsupportedOperationException("Unable to get an instance of ConversationState from turnContext.");
        }
        else
        {
            context.TurnState.Set(ConversationState);
        }

        botStateSet.Add(ConversationState);

        if (userState == null)
        {
            userState = context.getTurnState().get(UserState.class);
        }
        else
        {
            context.TurnState.Set(UserState);
        }

        if (UserState != null)
        {
            botStateSet.Add(UserState);
        }

        // create property accessors
        StatePropertyAccessor<Instant> lastAccessProperty = conversationState.createProperty(LastAccess);
        Instant lastAccess = lastAccessProperty.get(context, ()->{Instant.now();});

        // Check for expired conversation
        if (expireAfter != null && (Instant.now() - lastAccess) >= Duration.ofMillis(expireAfter))
        {
            // Clear conversation state
            conversationState.clearState(context).join();
        }

        lastAccess = Instant.now();
        lastAccessProperty.set(context, lastAccess);

        // get dialog stack
        StatePropertyAccessor<DialogState> dialogsProperty = conversationState.createProperty(_dialogStateProperty);
        DialogState dialogState = dialogsProperty.get(context, DialogState::new).join();

        // Create DialogContext
        DialogContext dc = new DialogContext(Dialogs, context, dialogState);

        // promote initial TurnState into dc.services for contextual services
        for (iterable_type dc.services : iterable) {

        }

        foreach (var service in dc.Services)
        {
            dc.Services[service.Key] = service.Value;
        }

        // map TurnState into root dialog context.services
        foreach (var service in context.TurnState)
        {
            dc.Services[service.Key] = service.Value;
        }

        // get the DialogStateManager configuration
        var dialogStateManager = new DialogStateManager(dc, StateConfiguration);
        await dialogStateManager.LoadAllScopesAsync(cancellationToken).ConfigureAwait(false);
        dc.Context.TurnState.Add(dialogStateManager);

        DialogTurnResult turnResult = null;

        // Loop as long as we are getting valid OnError handled we should continue executing the actions for the turn.
        //
        // NOTE: We loop around this block because each pass through we either complete the turn and break out of the loop
        // or we have had an exception AND there was an OnError action which captured the error.  We need to continue the
        // turn based on the actions the OnError handler introduced.
        var endOfTurn = false;
        while (!endOfTurn)
        {
            try
            {
                if (context.TurnState.Get<IIdentity>(BotAdapter.BotIdentityKey) is ClaimsIdentity claimIdentity && SkillValidation.IsSkillClaim(claimIdentity.Claims))
                {
                    // The bot is running as a skill.
                    turnResult =
                    /**
                     * @param dc
                     * @param cancellationToken).ConfigureAwait(false
                     * @return await
                     */
                    await HandleSkillOnTurnAsync(dc, cancellationToken).ConfigureAwait(false);
                }
                else
                {
                    // The bot is running as root bot.
                    turnResult =
                    /**
                     * @param dc
                     * @param err
                     * @return await
                     */
                    await HandleBotOnTurnAsync(dc, cancellationToken).ConfigureAwait(false);
                }

                // turn successfully completed, break the loop
                endOfTurn = true;
            }
            catch (Exception err)
            {
                // fire error event, bubbling from the leaf.
                var handled = await dc.EmitEventAsync(DialogEvents.Error, err, bubble: true, fromLeaf: true, cancellationToken: cancellationToken).ConfigureAwait(false);

                if (!handled)
                {
                    // error was NOT handled, throw the exception and end the turn. (This will trigger the Adapter.OnError handler and end the entire dialog stack)
                    throw;
                }
            }
        }

        // save all state scopes to their respective botState locations.
        await dialogStateManager.SaveAllChangesAsync(cancellationToken).ConfigureAwait(false);

        // save BotState changes
        await botStateSet.SaveAllChangesAsync(dc.Context, false, cancellationToken).ConfigureAwait(false);

        return new DialogManagerResult { TurnResult = turnResult };
    }


    /**
     * @param dc
     * @param traceLabel
     * @param cancellationToken
     * @return CompletableFuture<Void>
     */
    /// <summary>
    /// Helper to send a trace activity with a memory snapshot of the active dialog DC.
    /// </summary>
    private static CompletableFuture<Void> SendStateSnapshotTraceAsync(DialogContext dc, String traceLabel, CancellationToken cancellationToken)
    {
        // send trace of memory
        var snapshot = GetActiveDialogContext(dc).State.GetMemorySnapshot();
        var traceActivity = (Activity)Activity.CreateTraceActivity("BotState", "https://www.botframework.com/schemas/botState", snapshot, traceLabel);
        await dc.Context.SendActivityAsync(traceActivity, cancellationToken).ConfigureAwait(false);
    }


    /**
     * @param turnContext
     * @return Boolean
     */
    private static Boolean IsFromParentToSkill(ITurnContext turnContext)
    {
        if (turnContext.TurnState.Get<SkillConversationReference>(SkillHandler.SkillConversationReferenceKey) != null)
        {
            return false;
        }

        return turnContext.TurnState.Get<IIdentity>(BotAdapter.BotIdentityKey) is ClaimsIdentity claimIdentity && SkillValidation.IsSkillClaim(claimIdentity.Claims);
    }


    /**
     * @param dialogContext
     * @return DialogContext
     */
    // Recursively walk up the DC stack to find the active DC.
    private static DialogContext GetActiveDialogContext(DialogContext dialogContext)
    {
        var child = dialogContext.Child;
        if (child == null)
        {
            return dialogContext;
        }

        return GetActiveDialogContext(child);
    }


    /**
     * @param context
     * @param turnResult
     * @return Boolean
     */
    /// <summary>
    /// Helper to determine if we should send an EndOfConversation to the parent or not.
    /// </summary>
    private static Boolean ShouldSendEndOfConversationToParent(ITurnContext context, DialogTurnResult turnResult)
    {
        if (!(turnResult.Status == DialogTurnStatus.Complete || turnResult.Status == DialogTurnStatus.Cancelled))
        {
            // The dialog is still going, don't return EoC.
            return false;
        }

        if (context.TurnState.Get<IIdentity>(BotAdapter.BotIdentityKey) is ClaimsIdentity claimIdentity && SkillValidation.IsSkillClaim(claimIdentity.Claims))
        {
            // EoC Activities returned by skills are bounced back to the bot by SkillHandler.
            // In those cases we will have a SkillConversationReference instance in state.
            var skillConversationReference = context.TurnState.Get<SkillConversationReference>(SkillHandler.SkillConversationReferenceKey);
            if (skillConversationReference != null)
            {
                // If the skillConversationReference.OAuthScope is for one of the supported channels, we are at the root and we should not send an EoC.
                return skillConversationReference.OAuthScope != AuthenticationConstants.ToChannelFromBotOAuthScope && skillConversationReference.OAuthScope != GovernmentAuthenticationConstants.ToChannelFromBotOAuthScope;
            }

            return true;
        }

        return false;
    }


    /**
     * @param dc
     * @param cancellationToken
     * @return CompletableFuture<DialogTurnResult>
     */
    private CompletableFuture<DialogTurnResult> HandleSkillOnTurnAsync(DialogContext dc, CancellationToken cancellationToken)
    {
        // the bot is running as a skill.
        var turnContext = dc.Context;

        // Process remote cancellation
        if (turnContext.Activity.Type == ActivityTypes.EndOfConversation && dc.ActiveDialog != null && IsFromParentToSkill(turnContext))
        {
            // Handle remote cancellation request from parent.
            var activeDialogContext = GetActiveDialogContext(dc);

            // Send cancellation message to the top dialog in the stack to ensure all the parents are canceled in the right order.
            return await activeDialogContext.CancelAllDialogsAsync(true, cancellationToken: cancellationToken).ConfigureAwait(false);
        }

        // Handle reprompt
        // Process a reprompt event sent from the parent.
        if (turnContext.Activity.Type == ActivityTypes.Event && turnContext.Activity.Name == DialogEvents.RepromptDialog)
        {
            if (dc.ActiveDialog == null)
            {
                return new DialogTurnResult(DialogTurnStatus.Empty);
            }

            await dc.RepromptDialogAsync(cancellationToken).ConfigureAwait(false);
            return new DialogTurnResult(DialogTurnStatus.Waiting);
        }

        // Continue execution
        // - This will apply any queued up interruptions and execute the current/next step(s).
        var turnResult = await dc.ContinueDialogAsync(cancellationToken).ConfigureAwait(false);
        if (turnResult.Status == DialogTurnStatus.Empty)
        {
            // restart root dialog
            turnResult = await dc.BeginDialogAsync(_rootDialogId, cancellationToken: cancellationToken).ConfigureAwait(false);
        }


        /**
         * @param dc
         * @param State"
         * @param (ShouldSendEndOfConversationToParent(turnContext
         * @param turnResult)
         * @return await
         */
        await SendStateSnapshotTraceAsync(dc, "Skill State", cancellationToken).ConfigureAwait(false);

        if (ShouldSendEndOfConversationToParent(turnContext, turnResult))
        {
            // Send End of conversation at the end.
            var activity = new Activity(ActivityTypes.EndOfConversation)
            {
                Value = turnResult.Result,
                Locale = turnContext.Activity.Locale
            };
            await turnContext.SendActivityAsync(activity, cancellationToken).ConfigureAwait(false);
        }

        return turnResult;
    }


    /**
     * @param dialog
     * @param true
     */
    /// <summary>
    /// Recursively traverses the <see cref="Dialog"/> tree and registers instances of <see cref="DialogContainer"/>
    /// in the <see cref="DialogSet"/> for this <see cref="DialogManager"/> instance.
    /// </summary>
    /// <param name="dialog">Root of the <see cref="Dialog"/> subtree to iterate and register containers from.</param>
    /// <param name="registerRoot">Whether to register the root of the subtree. </param>
    private void RegisterContainerDialogs(Dialog dialog, Boolean registerRoot = true)
    {
        if (dialog is DialogContainer container)
        {
            if (registerRoot)
            {
                Dialogs.Add(container);
            }


            /**
             * @param container.Dialogs.GetDialogs()
             */
            foreach (var inner in container.Dialogs.GetDialogs())
            {
                RegisterContainerDialogs(inner);
            }
        }
    }


    /**
     * @param dc
     * @param cancellationToken
     * @return CompletableFuture<DialogTurnResult>
     */
    private CompletableFuture<DialogTurnResult> HandleBotOnTurnAsync(DialogContext dc, CancellationToken cancellationToken)
    {
        DialogTurnResult turnResult;

        // the bot is running as a root bot.
        if (dc.ActiveDialog == null)
        {
            // start root dialog
            turnResult = await dc.BeginDialogAsync(_rootDialogId, cancellationToken: cancellationToken).ConfigureAwait(false);
        }
        else
        {
            // Continue execution
            // - This will apply any queued up interruptions and execute the current/next step(s).
            turnResult = await dc.ContinueDialogAsync(cancellationToken).ConfigureAwait(false);

            if (turnResult.Status == DialogTurnStatus.Empty)
            {
                // restart root dialog
                turnResult = await dc.BeginDialogAsync(_rootDialogId, cancellationToken: cancellationToken).ConfigureAwait(false);
            }
        }

        await SendStateSnapshotTraceAsync(dc, "Bot State", cancellationToken).ConfigureAwait(false);

        return turnResult;
    }
}
