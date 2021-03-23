package com.microsoft.bot.dialogs;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.Middleware;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.schema.Activity;

public class DialogTestClient {

    private DialogContext dialogContext;

    private DialogTurnResult dialogTurnResult;

    private ConversationState conversationState;

    private final BotCallbackHandler _callback;
    private final TestAdapter testAdapter;

    /**
     * Initializes a new instance of the {@link DialogTestClient} class.
     *
     * @param channelId             The channelId (see {@link Channels} ) to be used for the test. Use
     *                              {@link Channels#emulator} or {@link Channels#test} if you are uncertain
     *                              of the channel you are targeting. Otherwise, it is recommended that you
     *                              use the id for the channel(s) your bot will be using. Consider writing a
     *                              test case for each channel.
     * @param targetDialog          The dialog to be tested. This will
     *                              be the root dialog for the test client.
     * @param initialDialogOptions  (Optional) additional argument(s) to
     *                              pass to the dialog being started.
     * @param middlewares           (Optional) A list of middlewares to
     *                              be added to the test adapter.
     * @param conversationState     (Optional) A
     *                              {@link ConversationState} to use in the test client.
     */
    public DialogTestClient(
        String channelId,
        Dialog targetDialog,
        Object initialDialogOptions,
        List<Middleware> middlewares,
        ConversationState conversationState
    ) {
        if (conversationState == null) {
            this.conversationState = new ConversationState(new MemoryStorage());
        } else {
            this.conversationState = conversationState;
        }
        this.testAdapter = new TestAdapter(channelId).use(new AutoSaveStateMiddleware(conversationState));

        addUserMiddlewares(middlewares);

        StatePropertyAccessor<DialogState> dialogState = getConversationState().createProperty("DialogState");

        _callback = getDefaultCallback(targetDialog, initialDialogOptions, dialogState);
    }

    /**
     * Initializes a new instance of the {@link DialogTestClient} class.
     *
     * @param testAdapter           The {@link TestAdapter} to use.
     * @param targetDialog          The dialog to be tested. This will
     *                              be the root dialog for the test client.
     * @param initialDialogOptions  (Optional) additional argument(s) to
     *                              pass to the dialog being started.
     * @param middlewares           (Optional) A list of middlewares to
     *                              be added to the test adapter.
     *                                   * @param conversationState     (Optional) A
     *                              {@link ConversationState} to use in the test client.
     */
    public DialogTestClient(
        TestAdapter testAdapter,
        Dialog targetDialog,
        Object initialDialogOptions,
        List<Middleware> middlewares,
        ConversationState conversationState
    ) {

        if (conversationState == null) {
            this.conversationState = new ConversationState(new MemoryStorage());
        } else {
            this.conversationState = conversationState;
        }
        this.testAdapter = testAdapter.use(new AutoSaveStateMiddleware(conversationState));

        addUserMiddlewares(middlewares);

        StatePropertyAccessor<DialogState> dialogState = getConversationState().createProperty("DialogState");

        _callback = getDefaultCallback(targetDialog, initialDialogOptions, dialogState);
    }

    /**
     * Sends an {@link Activity} to the target dialog.
     *
     * @param activity  The activity to send.
     *
     * @return   A {@link CompletableFuture} representing the result of
     *           the asynchronous operation.
     */
    public <T extends Activity> CompletableFuture<T> sendActivity(Activity activity) {
        testAdapter.processActivity(activity, _callback).join();
        return CompletableFuture.completedFuture(getNextReply());
    }

    /**
     * Sends a message activity to the target dialog.
     *
     * @param text  The text of the message to send.
     *
     * @return   A {@link CompletableFuture} representing the result of
     *           the asynchronous operation.
     */
    public <T extends Activity> CompletableFuture<T> sendActivity(String text){
         testAdapter.sendTextToBot(text, _callback).join();
         return CompletableFuture.completedFuture(getNextReply());
    }

    /**
     * Gets the next bot response.
     *
     * @return   The next activity in the queue; or null, if the queue
     *           is empty.
     * @param <T> the type.
     */
    public <T extends Activity> T getNextReply() {
        return (T) testAdapter.getNextReply();
    }

    private BotCallbackHandler getDefaultCallback(
        Dialog targetDialog,
        Object initialDialogOptions,
        StatePropertyAccessor<DialogState> dialogState
    ) {
        BotCallbackHandler handler =
        (turnContext) -> {
            // Ensure dialog state instanceof created and pass it to DialogSet.
            dialogState.get(turnContext, () -> new DialogState());
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(targetDialog);

            dialogContext =  dialogs.createContext(turnContext).join();
            dialogTurnResult = dialogContext.continueDialog().join();
            switch (dialogTurnResult.getStatus()) {
                case EMPTY:
                    dialogTurnResult =  dialogContext.beginDialog(targetDialog.getId(), initialDialogOptions).join();
                    break;
                case COMPLETE:
                default:
                    // Dialog has ended
                    break;
            }
            return CompletableFuture.completedFuture(null);
        };
        return handler;
    }

    private void addUserMiddlewares(List<Middleware> middlewares) {
        if (middlewares != null) {
            for (Middleware middleware : middlewares) {
                testAdapter.use(middleware);
            }
        }
    }
    /**
     * Gets a reference for the {@link DialogContext} .
     * This property will be null until at least one activity is sent to
     * {@link DialogTestClient} .
     * @return the DialogContext value as a getDialogContext().
     */
    public DialogContext getDialogContext() {
        return this.dialogContext;
    }

    /**
     * Gets a reference for the {@link DialogContext} .
     * This property will be null until at least one activity is sent to
     * {@link DialogTestClient} .
     * @param withDialogContext The DialogContext value.
     */
    private void setDialogContext(DialogContext withDialogContext) {
        this.dialogContext = withDialogContext;
    }

    /**
     * Gets the latest {@link DialogTurnResult} for the dialog being tested.
     * @return the DialogTurnResult value as a getDialogTurnResult().
     */
    public DialogTurnResult getDialogTurnResult() {
        return this.dialogTurnResult;
    }

    /**
     * Gets the latest {@link DialogTurnResult} for the dialog being tested.
     * @param withDialogTurnResult The DialogTurnResult value.
     */
    private void setDialogTurnResult(DialogTurnResult withDialogTurnResult) {
        this.dialogTurnResult = withDialogTurnResult;
    }

    /**
     * Gets the latest {@link ConversationState} for
     * {@link DialogTestClient} .
     * @return the ConversationState value as a getConversationState().
     */
    public ConversationState getConversationState() {
        return this.conversationState;
    }

}

