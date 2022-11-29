// Copyright (c) Microsoft Corporation. All rights reserved
// Licensed under the MT License

package com.microsoft.bot.dialogs.prompts;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogSet;
import com.microsoft.bot.dialogs.DialogState;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.DialogTurnStatus;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.bot.schema.OAuthCard;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.SignInConstants;
import com.microsoft.bot.schema.TokenExchangeInvokeRequest;
import com.microsoft.bot.schema.TokenExchangeInvokeResponse;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenResponse;

import org.junit.Assert;
import org.junit.Test;

public class OAuthPromptTests {

    @Test
    public void OAuthPromptWithEmptySettingsShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new OAuthPrompt("abc", null, null));
    }

    @Test
    public void OAuthPromptWithEmptyIdShouldFail() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new OAuthPrompt("", new OAuthPromptSettings(), null));
    }

    @Test
    public void OAuthPromptWithDefaultTypeHandlingForStorage() {
        OAuthPrompt(new MemoryStorage());
    }

    @Test
    public void OAuthPromptBeginDialogWithNoDialogContext() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            try {
                OAuthPrompt prompt = new OAuthPrompt("abc", new OAuthPromptSettings(), null);
                prompt.beginDialog(null).join();
            } catch (CompletionException ex) {
                throw ex.getCause();
            }
        });
    }

    @Test
    public void OAuthPromptBeginDialogWithWrongOptions() {
        Assert.assertThrows(NullPointerException.class, () -> {
            OAuthPrompt prompt = new OAuthPrompt("abc", new OAuthPromptSettings(), null);
            ConversationState convoState = new ConversationState(new MemoryStorage());
            StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

            TestAdapter adapter = new TestAdapter().use(new AutoSaveStateMiddleware(convoState));

            // Create new DialogSet.
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(prompt);
            ConversationAccount conversation = new ConversationAccount();
            conversation.setId("123");
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setConversation(conversation);
            activity.setChannelId("test");
            TurnContextImpl tc = new TurnContextImpl(adapter, activity);

            DialogContext dc = dialogs.createContext(tc).join();

            prompt.beginDialog(dc).join();
        });
    }

    @Test
    public void OAuthPromptWithNoneTypeHandlingForStorage() {
        OAuthPrompt(new MemoryStorage(new HashMap<String, JsonNode>()));
    }

    @Test
    public void OAuthPromptWithMagicCode() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String token = "abc123";
        String magicCode = "888999";

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        OAuthPromptSettings settings = new OAuthPromptSettings();
        settings.setText("Please sign in");
        settings.setConnectionName(connectionName);
        settings.setTitle("Sign in");
        OAuthPrompt oAuthPrompt = new OAuthPrompt("OAuthPrompt", settings);
        dialogs.add(oAuthPrompt);

        BotCallbackHandler botCallbackHandler =  (turnContext) -> {
            DialogContext dc =  dialogs.createContext(turnContext).join();

            DialogTurnResult results =  dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.prompt("OAuthPrompt", new PromptOptions());
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if (results.getResult() instanceof TokenResponse) {
                    turnContext.sendActivity(MessageFactory.text("Logged in.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Failed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        new TestFlow(adapter, botCallbackHandler)
        .send("hello")
        .assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());
            Assert.assertEquals(InputHints.ACCEPTING_INPUT, ((Activity) activity).getInputHint());

        // Add a magic code to the adapter
            adapter.addUserToken(connectionName, activity.getChannelId(),
                                                 activity.getRecipient().getId(),
                                                 token,
                                                 magicCode);
        })
        .send(magicCode)
        .assertReply("Logged in.")
        .startTest()
        .join();
    }

    @Test
    public void OAuthPromptTimesOut_Message() {
        PromptTimeoutEndsDialogTest(MessageFactory.text("hi"));
    }

    @Test
    public void OAuthPromptTimesOut_TokenResponseEvent() {
        Activity activity = new Activity(ActivityTypes.EVENT);
        activity.setName(SignInConstants.TOKEN_RESPONSE_EVENT_NAME);
        activity.setValue(new TokenResponse(Channels.MSTEAMS, "connectionName", "token", null));
        PromptTimeoutEndsDialogTest(activity);
    }

    @Test
    public void OAuthPromptTimesOut_VerifyStateOperation() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName(SignInConstants.VERIFY_STATE_OPERATION_NAME);
        activity.setValue("888999");
        PromptTimeoutEndsDialogTest(activity);
    }

    @Test
    public void OAuthPromptTimesOut_TokenExchangeOperation() {
        Activity activity = new Activity(ActivityTypes.INVOKE);
        activity.setName(SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME);

        String connectionName = "myConnection";
        String exchangeToken = "exch123";

        TokenExchangeInvokeRequest tokenExchangeRequest = new TokenExchangeInvokeRequest();
        tokenExchangeRequest.setConnectionName(connectionName);
        tokenExchangeRequest.setToken(exchangeToken);

        activity.setValue(tokenExchangeRequest);

        PromptTimeoutEndsDialogTest(activity);
    }

    @Test
    public void OAuthPromptDoesNotDetectCodeInBeginDialog() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String token = "abc123";
        String magicCode = "888999";

        // Create new DialogSet
        DialogSet dialogs = new DialogSet(dialogState);

        OAuthPromptSettings settings = new OAuthPromptSettings();
        settings.setText("Please sign in");
        settings.setConnectionName(connectionName);
        settings.setTitle("Sign in");

        dialogs.add(new OAuthPrompt("OAuthPrompt", settings));

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            // Add a magic code to the adapter preemptively so that we can test if the
            // message that triggers BeginDialogAsync uses magic code detection
            adapter.addUserToken(connectionName, turnContext.getActivity().getChannelId(),
                                 turnContext.getActivity().getFrom().getId(), token, magicCode);

            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();

            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                // If magicCode is detected when prompting, this will end the dialog and
                // return the token in tokenResult
                DialogTurnResult tokenResult = dc.prompt("OAuthPrompt", new PromptOptions()).join();
                if (tokenResult.getResult() instanceof TokenResponse) {
                    throw new RuntimeException();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        // Call BeginDialogAsync by sending the magic code as the first message. It
        // SHOULD respond with an OAuthPrompt since we haven't authenticated yet
        new TestFlow(adapter, botCallbackHandler)
        .send(magicCode)
        .assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());
            Assert.assertEquals(InputHints.ACCEPTING_INPUT, ((Activity) activity).getInputHint());
        })
        .startTest()
        .join();
        }

    @Test
    public void OAuthPromptWithTokenExchangeInvoke() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String exchangeToken = "exch123";
        String token = "abc123";

        // Create new DialogSet
        DialogSet dialogs = new DialogSet(dialogState);

        OAuthPromptSettings settings = new OAuthPromptSettings();
        settings.setText("Please sign in");
        settings.setConnectionName(connectionName);
        settings.setTitle("Sign in");

        dialogs.add(new OAuthPrompt("OAuthPrompt", settings));

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.prompt("OAuthPrompt", new PromptOptions()).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if (results.getResult() instanceof TokenResponse) {
                    turnContext.sendActivity(MessageFactory.text("Logged in.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Failed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        TokenExchangeInvokeRequest value = new TokenExchangeInvokeRequest();
        value.setConnectionName(connectionName);
        value.setToken(exchangeToken);
        Activity activityToSend = new Activity(ActivityTypes.INVOKE);
        activityToSend.setName(SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME);
        activityToSend.setValue(Serialization.objectToTree(value));

        new TestFlow(adapter, botCallbackHandler)
        .send("hello")
        .assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());
            Assert.assertEquals(InputHints.ACCEPTING_INPUT, ((Activity) activity).getInputHint());

        // Add an exchangable token to the adapter
            adapter.addExchangeableToken(connectionName, activity.getChannelId(),
                                         activity.getRecipient().getId(), exchangeToken, token);
        })
        .send(activityToSend)
        .assertReply(a -> {
            Assert.assertEquals("invokeResponse", a.getType());
            InvokeResponse response = (InvokeResponse) ((Activity)a).getValue();
            Assert.assertNotNull(response);
            Assert.assertEquals(200, response.getStatus());
            TokenExchangeInvokeResponse body = (TokenExchangeInvokeResponse) response.getBody();
            Assert.assertEquals(connectionName, body.getConnectionName());
            Assert.assertNull(body.getFailureDetail());
        })
        .assertReply("Logged in.")
        .startTest()
        .join();
    }

    @Test
    public void OAuthPromptWithTokenExchangeFail() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String exchangeToken = "exch123";

        // Create new DialogSet
        DialogSet dialogs = new DialogSet(dialogState);

        OAuthPromptSettings settings = new OAuthPromptSettings();
        settings.setText("Please sign in");
        settings.setConnectionName(connectionName);
        settings.setTitle("Sign in");

        dialogs.add(new OAuthPrompt("OAuthPrompt", settings));

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.prompt("OAuthPrompt", new PromptOptions()).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if (results.getResult() instanceof TokenResponse) {
                    turnContext.sendActivity(MessageFactory.text("Logged in.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Failed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        TokenExchangeInvokeRequest value = new TokenExchangeInvokeRequest();
        value.setConnectionName(connectionName);
        value.setToken(exchangeToken);
        Activity activityToSend = new Activity(ActivityTypes.INVOKE);
        activityToSend.setName(SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME);
        activityToSend.setValue(Serialization.objectToTree(value));

        new TestFlow(adapter, botCallbackHandler)
        .send("hello")
        .assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());
            Assert.assertEquals(InputHints.ACCEPTING_INPUT, ((Activity) activity).getInputHint());
            // No exchangable token is added to the adapter
        })
        .send(activityToSend)
        .assertReply(a -> {
            Assert.assertEquals("invokeResponse", a.getType());
            InvokeResponse response = (InvokeResponse) ((Activity) a).getValue();
            Assert.assertNotNull(response);
            Assert.assertEquals(412, response.getStatus());
            TokenExchangeInvokeResponse body = (TokenExchangeInvokeResponse) response.getBody();
            Assert.assertEquals(connectionName, body.getConnectionName());
            Assert.assertNotNull(body.getFailureDetail());
        })
        .startTest()
        .join();
    }

    @Test
    public void OAuthPromptWithTokenExchangeNoBodyFails() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";

        // Create new DialogSet
        DialogSet dialogs = new DialogSet(dialogState);

        OAuthPromptSettings settings = new OAuthPromptSettings();
        settings.setText("Please sign in");
        settings.setConnectionName(connectionName);
        settings.setTitle("Sign in");

        dialogs.add(new OAuthPrompt("OAuthPrompt", settings));

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.prompt("OAuthPrompt", new PromptOptions()).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if (results.getResult() instanceof TokenResponse) {
                    turnContext.sendActivity(MessageFactory.text("Logged in.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Failed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        Activity activityToSend = new Activity(ActivityTypes.INVOKE);
        activityToSend.setName(SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME);
        new TestFlow(adapter, botCallbackHandler)
        .send("hello")
        .assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());
            Assert.assertEquals(InputHints.ACCEPTING_INPUT, ((Activity) activity).getInputHint());
            // No exchangable token is added to the adapter
        })
        .send(activityToSend)
        .assertReply(a -> {
            Assert.assertEquals("invokeResponse", a.getType());
            InvokeResponse response = (InvokeResponse) ((Activity) a).getValue();
            Assert.assertNotNull(response);
            Assert.assertEquals(400, response.getStatus());
            TokenExchangeInvokeResponse body = (TokenExchangeInvokeResponse) response.getBody();
            Assert.assertEquals(connectionName, body.getConnectionName());
            Assert.assertNotNull(body.getFailureDetail());
        })
        .startTest()
        .join();
    }

    @Test
    public void OAuthPromptWithTokenExchangeWrongConnectionNameFail() {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String exchangeToken = "exch123";

        // Create new DialogSet
        DialogSet dialogs = new DialogSet(dialogState);

        OAuthPromptSettings settings = new OAuthPromptSettings();
        settings.setText("Please sign in");
        settings.setConnectionName(connectionName);
        settings.setTitle("Sign in");

        dialogs.add(new OAuthPrompt("OAuthPrompt", settings));

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.prompt("OAuthPrompt", new PromptOptions()).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if (results.getResult() instanceof TokenResponse) {
                    turnContext.sendActivity(MessageFactory.text("Logged in.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Failed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        TokenExchangeInvokeRequest value = new TokenExchangeInvokeRequest();
        value.setConnectionName("beepboop");
        value.setToken(exchangeToken);
        Activity activityToSend = new Activity(ActivityTypes.INVOKE);
        activityToSend.setName(SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME);
        activityToSend.setValue(value);
        new TestFlow(adapter, botCallbackHandler)
        .send("hello")
        .assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());
            Assert.assertEquals(InputHints.ACCEPTING_INPUT, ((Activity) activity).getInputHint());
            // No exchangable token is added to the adapter
        })
        .send(activityToSend)
        .assertReply(a -> {
            Assert.assertEquals("invokeResponse", a.getType());
            InvokeResponse response = (InvokeResponse) ((Activity) a).getValue();
            Assert.assertNotNull(response);
            Assert.assertEquals(400, response.getStatus());
            TokenExchangeInvokeResponse body = (TokenExchangeInvokeResponse) response.getBody();
            Assert.assertEquals(connectionName, body.getConnectionName());
            Assert.assertNotNull(body.getFailureDetail());
        })
        .startTest()
        .join();
    }

    @Test
    public void TestAdapterTokenExchange() {
        ConversationState convoState = new ConversationState(new MemoryStorage());

        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String exchangeToken = "exch123";
        String token = "abc123";

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            String userId = "fred";
            adapter.addExchangeableToken(connectionName,
                                        turnContext.getActivity().getChannelId(),
                                        userId,
                                        exchangeToken,
                                        token);

            // Positive case: Token
            TokenExchangeRequest requestPositiveToken = new TokenExchangeRequest();
            requestPositiveToken.setToken(exchangeToken);
            TokenResponse result = adapter.exchangeToken(turnContext, connectionName, userId, requestPositiveToken).join();
            Assert.assertNotNull(result);
            Assert.assertEquals(token, result.getToken());
            Assert.assertEquals(connectionName, result.getConnectionName());

            // Positive case: URI
            TokenExchangeRequest requestPositiveURI = new TokenExchangeRequest();
            requestPositiveURI.setUri(exchangeToken);
            result = adapter.exchangeToken(turnContext, connectionName, userId, requestPositiveURI).join();
            Assert.assertNotNull(result);
            Assert.assertEquals(token, result.getToken());
            Assert.assertEquals(connectionName, result.getConnectionName());

            // Negative case: Token
            TokenExchangeRequest requestNegativeToken = new TokenExchangeRequest();
            requestNegativeToken.setToken("beeboop");
            result = adapter.exchangeToken(turnContext, connectionName, userId, requestNegativeToken).join();
            Assert.assertNull(result);

            // Negative case: URI
            TokenExchangeRequest requestNegativeURI = new TokenExchangeRequest();
            requestNegativeURI.setToken("beeboop");
            result = adapter.exchangeToken(turnContext, connectionName, userId, requestNegativeURI).join();
            Assert.assertNull(result);

            return CompletableFuture.completedFuture(null);
        };

        new TestFlow(adapter, botCallbackHandler)
        .send("hello")
        .startTest()
        .join();
    }

    private void OAuthPrompt(Storage storage) {
        ConversationState convoState = new ConversationState(storage);
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");
        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String token = "abc123";

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);
        OAuthPromptSettings oauthPromptSettings = new OAuthPromptSettings();
        oauthPromptSettings.setText("Please sign in");
        oauthPromptSettings.setConnectionName(connectionName);
        oauthPromptSettings.setTitle("Sign in");
        dialogs.add(new OAuthPrompt("OAuthPrompt", oauthPromptSettings));

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.prompt("OAuthPrompt", new PromptOptions());
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
                if (results.getResult() instanceof TokenResponse) {
                    turnContext.sendActivity(MessageFactory.text("Logged in.")).join();
                } else {
                    turnContext.sendActivity(MessageFactory.text("Failed.")).join();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        new TestFlow(adapter, botCallbackHandler).send("hello").assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());
            Assert.assertEquals(InputHints.ACCEPTING_INPUT, ((Activity) activity).getInputHint());

            // Prepare an EventActivity with a TokenResponse and send it to the
            // botCallbackHandler
            Activity eventActivity = createEventResponse(adapter, activity, connectionName, token);
            TurnContextImpl ctx = new TurnContextImpl(adapter, (Activity) eventActivity);
            botCallbackHandler.invoke(ctx);
        }).assertReply("Logged in.").startTest().join();
    }

    private void PromptTimeoutEndsDialogTest(Activity oauthPromptActivity) {
        ConversationState convoState = new ConversationState(new MemoryStorage());
        StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");
        TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

        String connectionName = "myConnection";
        String exchangeToken = "exch123";
        String magicCode = "888999";
        String token = "abc123";

        // Create new DialogSet.
        DialogSet dialogs = new DialogSet(dialogState);

        // Set timeout to zero, so the prompt will end immediately.
        OAuthPromptSettings settings = new OAuthPromptSettings();
        settings.setText("Please sign in");
        settings.setConnectionName(connectionName);
        settings.setTitle("Sign in");
        settings.setTimeout(0);
        dialogs.add(new OAuthPrompt("OAuthPrompt", settings));

        BotCallbackHandler botCallbackHandler = (turnContext) -> {
            DialogContext dc = dialogs.createContext(turnContext).join();

            DialogTurnResult results = dc.continueDialog().join();
            if (results.getStatus() == DialogTurnStatus.EMPTY) {
                dc.prompt("OAuthPrompt", new PromptOptions()).join();
            } else if (results.getStatus() == DialogTurnStatus.COMPLETE) {
            // If the TokenResponse comes back, the timeout did not occur.
                if (results.getResult() instanceof TokenResponse) {
                    turnContext.sendActivity("failed").join();
                } else {
                    turnContext.sendActivity("ended").join();
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        new TestFlow(adapter, botCallbackHandler)
        .send("hello")
        .assertReply(activity -> {
            Assert.assertTrue(((Activity) activity).getAttachments().size() == 1);
            Assert.assertEquals(OAuthCard.CONTENTTYPE, ((Activity) activity).getAttachments().get(0).getContentType());

            // Add a magic code to the adapter
            adapter.addUserToken(connectionName,
                                 activity.getChannelId(),
                                 activity.getRecipient().getId(),
                                 token,
                                 magicCode);

            // Add an exchangable token to the adapter
            adapter.addExchangeableToken(connectionName,
                                         activity.getChannelId(),
                                         activity.getRecipient().getId(),
                                         exchangeToken,
                                         token);
        })
        .send(oauthPromptActivity)
        .assertReply("ended")
        .startTest()
        .join();
    }

    private Activity createEventResponse(TestAdapter adapter, Activity activity, String connectionName, String token) {
        // add the token to the TestAdapter
        adapter.addUserToken(connectionName, activity.getChannelId(), activity.getRecipient().getId(), token, null);

        // send an event TokenResponse activity to the botCallback handler
        Activity eventActivity = ((Activity) activity).createReply();
        eventActivity.setType(ActivityTypes.EVENT);
        ChannelAccount from = eventActivity.getFrom();
        eventActivity.setFrom(eventActivity.getRecipient());
        eventActivity.setRecipient(from);
        eventActivity.setName(SignInConstants.TOKEN_RESPONSE_EVENT_NAME);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setConnectionName(connectionName);
        tokenResponse.setToken(token);
        eventActivity.setValue(tokenResponse);

        return eventActivity;
    }

    // private void OAuthPromptEndOnInvalidMessageSetting() {
    // var convoState = new ConversationState(new MemoryStorage());
    // var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";

    // // Create new DialogSet.
    // var dialogs = new DialogSet(dialogState);
    // dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text =
    // "Please sign in", ConnectionName = connectionName, Title = "Sign in",
    // EndOnInvalidMessage = true }));

    // BotCallbackHandler botCallbackHandler = (turnContext) -> {
    // var dc = dialogs.CreateContext(turnContext);

    // var results = dc.ContinueDialog();
    // if (results.Status == DialogTurnStatus.Empty) {
    // dc.Prompt("OAuthPrompt", new PromptOptions());
    // } else if (results.Status == DialogTurnStatus.Waiting) {
    // throw new InvalidOperationException("Test
    // OAuthPromptEndOnInvalidMessageSetting expected DialogTurnStatus.Complete");
    // } else if (results.Status == DialogTurnStatus.Complete) {
    // if (results.Result is TokenResponse) {
    // turnContext.SendActivity(MessageFactory.Text("Logged in."));
    // } else {
    // turnContext.SendActivity(MessageFactory.Text("Ended."));
    // }
    // }
    // };

    // new TestFlow(adapter, botCallbackHandler)
    // .send("hello")
    // .assertReply(activity -> {
    // Assert.Single(((Activity)activity).Attachments);
    // Assert.Equal(OAuthCard.ContentType,
    // ((Activity)activity).Attachments[0].ContentType);
    // })
    // .send("blah")
    // .assertReply("Ended.")
    // .startTest();
    // }
}
