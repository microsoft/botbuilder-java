// Copyright (c) Microsoft Corporation. All rights reserved
// Licensed under the MT License

package com.microsoft.bot.dialogs.prompts;

import java.util.concurrent.CompletionException;

import com.microsoft.bot.builder.AutoSaveStateMiddleware;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnContextImpl;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogSet;
import com.microsoft.bot.dialogs.DialogState;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationAccount;

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

    // @Test
    // public void OAuthPromptWithDefaultTypeHandlingForStorage() {
    // OAuthPrompt(new MemoryStorage());
    // }

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
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            OAuthPrompt prompt = new OAuthPrompt("abc", new OAuthPromptSettings(), null);
            ConversationState convoState = new ConversationState(new MemoryStorage());
            StatePropertyAccessor<DialogState> dialogState = convoState.createProperty("dialogState");

            TestAdapter adapter = new TestAdapter()
            .use(new AutoSaveStateMiddleware(convoState));

            // Create new DialogSet.
            DialogSet dialogs = new DialogSet(dialogState);
            dialogs.add(prompt);
            ConversationAccount conversation = new ConversationAccount();
            conversation.setId("123");
            TurnContextImpl tc = new TurnContextImpl(adapter, new Activity(ActivityTypes.MESSAGE) {
                {
                    setConversation(conversation);
                    setChannelId("test");
                }
            });

            DialogContext dc =  dialogs.createContext(tc).join();

            prompt.beginDialog(dc).join();
        });
    }

    // @Test
    // public void OAuthPromptWithNoneTypeHandlingForStorage() {
    // OAuthPrompt(new MemoryStorage(new JsonSerializer() { TypeNameHandling = TypeNameHandling.None }));
    // }

    // @Test
    // public void OAuthPromptWithMagicCode() {
    // var convoState = new ConversationState(new MemoryStorage());
    // var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";
    // var token = "abc123";
    // var magicCode = "888999";

    // // Create new DialogSet.
    // var dialogs = new DialogSet(dialogState);
    // dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in" }));

    // BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    // var dc =  dialogs.CreateContext(turnContext);

    // var results =  dc.ContinueDialog(cancellationToken);
    // if (results.Status == DialogTurnStatus.Empty) {
    // dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    // } else if (results.Status == DialogTurnStatus.Complete) {
    // if (results.Result is TokenResponse) {
    // turnContext.SendActivity(MessageFactory.Text("Logged in."));
    // } else {
    // turnContext.SendActivity(MessageFactory.Text("Failed."));
    // }
    // }
    // };

    // new TestFlow(adapter, botCallbackHandler)
    // .send("hello")
    // .assertReply(activity -> {
    // Assert.Single(((Activity)activity).Attachments);
    // Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);

    // Assert.Equal(InputHints.AcceptingInput, ((Activity)activity).InputHint);

    // // Add a magic code to the adapter
    // adapter.AddUserToken(connectionName, activity.ChannelId, activity.Recipient.Id, token, magicCode);
    // })
    // .send(magicCode)
    // .assertReply("Logged in.")
    // .startTest();
    // }

    // @Test
    // public void OAuthPromptTimesOut_Message() {
    //     PromptTimeoutEndsDialogTest(MessageFactory.Text("hi"));
    // }

    // @Test
    // public void OAuthPromptTimesOut_TokenResponseEvent() {
    // var activity = new Activity() { Type = ActivityTypes.Event, Name = SignInConstants.TokenResponseEventName };
    // activity.Value = JObject.FromObject(new TokenResponse(Channels.Msteams, "connectionName", "token", null));
    // PromptTimeoutEndsDialogTest(activity);
    // }

    // @Test
    // public void OAuthPromptTimesOut_VerifyStateOperation() {
    // var activity = new Activity() { Type = ActivityTypes.Invoke, Name = SignInConstants.VerifyStateOperationName };
    // activity.Value = JObject.FromObject(new { state = "888999" });

    // PromptTimeoutEndsDialogTest(activity);
    // }

    // @Test
    // public void OAuthPromptTimesOut_TokenExchangeOperation() {
    // var activity = new Activity() { Type = ActivityTypes.Invoke, Name = SignInConstants.TokenExchangeOperationName };

    // var connectionName = "myConnection";
    // var exchangeToken = "exch123";

    // activity.Value = JObject.FromObject(new TokenExchangeInvokeRequest() {
    // ConnectionName = connectionName,
    // Token = exchangeToken
    // });

    // PromptTimeoutEndsDialogTest(activity);
    // }

    // @Test
    // public void OAuthPromptDoesNotDetectCodeInBeginDialog() {
    // var convoState = new ConversationState(new MemoryStorage());
    // var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";
    // var token = "abc123";
    // var magicCode = "888999";

    // // Create new DialogSet
    // var dialogs = new DialogSet(dialogState);
    // dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in" }));

    // BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    // // Add a magic code to the adapter preemptively so that we can test if the message that triggers BeginDialogAsync uses magic code detection
    // adapter.AddUserToken(connectionName, turnContext.Activity.ChannelId, turnContext.Activity.From.Id, token, magicCode);

    // var dc =  dialogs.CreateContext(turnContext);

    // var results =  dc.ContinueDialog(cancellationToken);

    // if (results.Status == DialogTurnStatus.Empty) {
    // // If magicCode is detected when prompting, this will end the dialog and return the token in tokenResult
    // var tokenResult =  dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    // if (tokenResult.Result is TokenResponse) {
    // throw new XunitException();
    // }
    // }
    // };

    // // Call BeginDialogAsync by sending the magic code as the first message. It SHOULD respond with an OAuthPrompt since we haven't authenticated yet
    // new TestFlow(adapter, botCallbackHandler)
    // .send(magicCode)
    // .assertReply(activity -> {
    // Assert.Single(((Activity)activity).Attachments);
    // Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);

    // Assert.Equal(InputHints.AcceptingInput, ((Activity)activity).InputHint);
    // })
    // .startTest();
    // }

    // @Test
    // public void OAuthPromptWithTokenExchangeInvoke() {
    // var convoState = new ConversationState(new MemoryStorage());
    // var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";
    // var exchangeToken = "exch123";
    // var token = "abc123";

    // // Create new DialogSet.
    // var dialogs = new DialogSet(dialogState);
    // dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in" }));

    // BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    // var dc =  dialogs.CreateContext(turnContext);

    // var results =  dc.ContinueDialog(cancellationToken);
    // if (results.Status == DialogTurnStatus.Empty) {
    // dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    // } else if (results.Status == DialogTurnStatus.Complete) {
    // if (results.Result is TokenResponse) {
    // turnContext.SendActivity(MessageFactory.Text("Logged in."));
    // } else {
    // turnContext.SendActivity(MessageFactory.Text("Failed."));
    // }
    // }
    // };

    // new TestFlow(adapter, botCallbackHandler)
    // .send("hello")
    // .assertReply(activity -> {
    // Assert.Single(((Activity)activity).Attachments);
    // Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);
    // Assert.Equal(InputHints.AcceptingInput, ((Activity)activity).InputHint);

    // // Add an exchangable token to the adapter
    // adapter.AddExchangeableToken(connectionName, activity.ChannelId, activity.Recipient.Id, exchangeToken, token);
    // })
    // .send(new Activity() {
    // Type = ActivityTypes.Invoke,
    // Name = SignInConstants.TokenExchangeOperationName,
    // Value = JObject.FromObject(new TokenExchangeInvokeRequest() {
    // ConnectionName = connectionName,
    // Token = exchangeToken
    // })
    // })
    // .assertReply(a -> {
    // Assert.Equal("invokeResponse", a.Type);
    // var response = ((Activity)a).Value as InvokeResponse;
    // Assert.NotNull(response);
    // Assert.Equal(200, response.Status);
    // var body = response.Body as TokenExchangeInvokeResponse;
    // Assert.Equal(connectionName, body.ConnectionName);
    // Assert.Null(body.FailureDetail);
    // })
    // .assertReply("Logged in.")
    // .startTest();
    // }

    // @Test
    // public void OAuthPromptWithTokenExchangeFail() {
    // var convoState = new ConversationState(new MemoryStorage());
    // var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";
    // var exchangeToken = "exch123";

    // // Create new DialogSet.
    // var dialogs = new DialogSet(dialogState);
    // dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in" }));

    // BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    // var dc =  dialogs.CreateContext(turnContext);

    // var results =  dc.ContinueDialog(cancellationToken);
    // if (results.Status == DialogTurnStatus.Empty) {
    // dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    // } else if (results.Status == DialogTurnStatus.Complete) {
    // if (results.Result is TokenResponse) {
    // turnContext.SendActivity(MessageFactory.Text("Logged in."));
    // } else {
    // turnContext.SendActivity(MessageFactory.Text("Failed."));
    // }
    // }
    // };

    // new TestFlow(adapter, botCallbackHandler)
    // .send("hello")
    // .assertReply(activity -> {
    // Assert.Single(((Activity)activity).Attachments);
    // Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);
    // Assert.Equal(InputHints.AcceptingInput, ((Activity)activity).InputHint);

    // // No exchangable token is added to the adapter
    // })
    // .send(new Activity() {
    // Type = ActivityTypes.Invoke,
    // Name = SignInConstants.TokenExchangeOperationName,
    // Value = JObject.FromObject(new TokenExchangeInvokeRequest() {
    // ConnectionName = connectionName,
    // Token = exchangeToken
    // })
    // })
    // .assertReply(a -> {
    // Assert.Equal("invokeResponse", a.Type);
    // var response = ((Activity)a).Value as InvokeResponse;
    // Assert.NotNull(response);
    // Assert.Equal(412, response.Status);
    // var body = response.Body as TokenExchangeInvokeResponse;
    // Assert.Equal(connectionName, body.ConnectionName);
    // Assert.NotNull(body.FailureDetail);
    // })
    // .startTest();
    // }

    // @Test
    // public void OAuthPromptWithTokenExchangeNoBodyFails() {
    // var convoState = new ConversationState(new MemoryStorage());
    // var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";

    // // Create new DialogSet.
    // var dialogs = new DialogSet(dialogState);
    // dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in" }));

    // BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    // var dc =  dialogs.CreateContext(turnContext);

    // var results =  dc.ContinueDialog(cancellationToken);
    // if (results.Status == DialogTurnStatus.Empty) {
    // dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    // } else if (results.Status == DialogTurnStatus.Complete) {
    // if (results.Result is TokenResponse) {
    // turnContext.SendActivity(MessageFactory.Text("Logged in."));
    // } else {
    // turnContext.SendActivity(MessageFactory.Text("Failed."));
    // }
    // }
    // };

    // new TestFlow(adapter, botCallbackHandler)
    // .send("hello")
    // .assertReply(activity -> {
    // Assert.Single(((Activity)activity).Attachments);
    // Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);
    // Assert.Equal(InputHints.AcceptingInput, ((Activity)activity).InputHint);

    // // No exchangable token is added to the adapter
    // })
    // .send(new Activity() {
    // Type = ActivityTypes.Invoke,
    // Name = SignInConstants.TokenExchangeOperationName,

    // // send no body
    // })
    // .assertReply(a -> {
    // Assert.Equal("invokeResponse", a.Type);
    // var response = ((Activity)a).Value as InvokeResponse;
    // Assert.NotNull(response);
    // Assert.Equal(400, response.Status);
    // var body = response.Body as TokenExchangeInvokeResponse;
    // Assert.Equal(connectionName, body.ConnectionName);
    // Assert.NotNull(body.FailureDetail);
    // })
    // .startTest();
    // }

    // @Test
    // public void OAuthPromptWithTokenExchangeWrongConnectionNameFail() {
    // var convoState = new ConversationState(new MemoryStorage());
    // var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";
    // var exchangeToken = "exch123";

    // // Create new DialogSet.
    // var dialogs = new DialogSet(dialogState);
    // dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in" }));

    // BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    // var dc =  dialogs.CreateContext(turnContext);

    // var results =  dc.ContinueDialog(cancellationToken);
    // if (results.Status == DialogTurnStatus.Empty) {
    // dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    // } else if (results.Status == DialogTurnStatus.Complete) {
    // if (results.Result is TokenResponse) {
    // turnContext.SendActivity(MessageFactory.Text("Logged in."));
    // } else {
    // turnContext.SendActivity(MessageFactory.Text("Failed."));
    // }
    // }
    // };

    // new TestFlow(adapter, botCallbackHandler)
    // .send("hello")
    // .assertReply(activity -> {
    // Assert.Single(((Activity)activity).Attachments);
    // Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);
    // Assert.Equal(InputHints.AcceptingInput, ((Activity)activity).InputHint);

    // // No exchangable token is added to the adapter
    // })
    // .send(new Activity() {
    // Type = ActivityTypes.Invoke,
    // Name = SignInConstants.TokenExchangeOperationName,
    // Value = JObject.FromObject(new TokenExchangeInvokeRequest() {
    // ConnectionName = "beepboop",
    // Token = exchangeToken
    // })
    // })
    // .assertReply(a -> {
    // Assert.Equal("invokeResponse", a.Type);
    // var response = ((Activity)a).Value as InvokeResponse;
    // Assert.NotNull(response);
    // Assert.Equal(400, response.Status);
    // var body = response.Body as TokenExchangeInvokeResponse;
    // Assert.Equal(connectionName, body.ConnectionName);
    // Assert.NotNull(body.FailureDetail);
    // })
    // .startTest();
    // }

    // @Test
    // public void TestAdapterTokenExchange() {
    // var convoState = new ConversationState(new MemoryStorage());

    // var adapter = new TestAdapter()
    // .Use(new AutoSaveStateMiddleware(convoState));

    // var connectionName = "myConnection";
    // var exchangeToken = "exch123";
    // var token = "abc123";

    // BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    // var userId = "fred";
    // adapter.AddExchangeableToken(connectionName, turnContext.Activity.ChannelId, userId, exchangeToken, token);

    // // Positive case: Token
    // var result =  adapter.ExchangeToken(turnContext, connectionName, userId, new TokenExchangeRequest() { Token = exchangeToken });
    // Assert.NotNull(result);
    // Assert.Equal(token, result.Token);
    // Assert.Equal(connectionName, result.ConnectionName);

    //             // Positive case: URI
    //             result =  adapter.ExchangeToken(turnContext, connectionName, userId, new TokenExchangeRequest() { Uri = exchangeToken });
    //             Assert.NotNull(result);
    //             Assert.Equal(token, result.Token);
    //             Assert.Equal(connectionName, result.ConnectionName);

    //             // Negative case: Token
    //             result =  adapter.ExchangeToken(turnContext, connectionName, userId, new TokenExchangeRequest() { Token = "beeboop" });
    //             Assert.Null(result);

    //             // Negative case: URI
    //             result =  adapter.ExchangeToken(turnContext, connectionName, userId, new TokenExchangeRequest() { Uri = "beeboop" });
    //             Assert.Null(result);
    //         };

    //          new TestFlow(adapter, botCallbackHandler)
    //         .send("hello")
    //         .startTest();
    //     }

    // private void OAuthPrompt(IStorage storage) {
    //         var convoState = new ConversationState(storage);
    //         var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //         var adapter = new TestAdapter()
    //             .Use(new AutoSaveStateMiddleware(convoState));

    //         var connectionName = "myConnection";
    //         var token = "abc123";

    //         // Create new DialogSet.
    //         var dialogs = new DialogSet(dialogState);
    //         dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in" }));

    //         BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    //             var dc =  dialogs.CreateContext(turnContext);

    //             var results =  dc.ContinueDialog(cancellationToken);
    //             if (results.Status == DialogTurnStatus.Empty) {
    //                  dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    //             } else if (results.Status == DialogTurnStatus.Complete) {
    //                 if (results.Result is TokenResponse) {
    //                      turnContext.SendActivity(MessageFactory.Text("Logged in."));
    //                 } else {
    //                      turnContext.SendActivity(MessageFactory.Text("Failed."));
    //                 }
    //             }
    //         };

    //          new TestFlow(adapter, botCallbackHandler)
    //         .send("hello")
    //         .assertReply(activity -> {
    //             Assert.Single(((Activity)activity).Attachments);
    //             Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);

    //             Assert.Equal(InputHints.AcceptingInput, ((Activity)activity).InputHint);

    //             // Prepare an EventActivity with a TokenResponse and send it to the botCallbackHandler
    //             var eventActivity = CreateEventResponse(adapter, activity, connectionName, token);
    //             var ctx = new TurnContext(adapter, (Activity)eventActivity);
    //             botCallbackHandler(ctx, CancellationToken.None);
    //         })
    //         .assertReply("Logged in.")
    //         .startTest();
    //     }

    // private void PromptTimeoutEndsDialogTest(IActivity oauthPromptActivity) {
    //         var convoState = new ConversationState(new MemoryStorage());
    //         var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //         var adapter = new TestAdapter()
    //             .Use(new AutoSaveStateMiddleware(convoState));

    //         var connectionName = "myConnection";
    //         var exchangeToken = "exch123";
    //         var magicCode = "888999";
    //         var token = "abc123";

    //         // Create new DialogSet.
    //         var dialogs = new DialogSet(dialogState);

    //         // Set timeout to zero, so the prompt will end immediately.
    //         dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in", Timeout = 0 }));

    //         BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    //             var dc =  dialogs.CreateContext(turnContext);

    //             var results =  dc.ContinueDialog(cancellationToken);
    //             if (results.Status == DialogTurnStatus.Empty) {
    //                  dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    //             } else if (results.Status == DialogTurnStatus.Complete) {
    //                 // If the TokenResponse comes back, the timeout did not occur.
    //                 if (results.Result is TokenResponse) {
    //                      turnContext.SendActivity("failed": cancellationToken);
    //                 } else {
    //                      turnContext.SendActivity("ended": cancellationToken);
    //                 }
    //             }
    //         };

    //          new TestFlow(adapter, botCallbackHandler)
    //         .send("hello")
    //         .assertReply(activity -> {
    //             Assert.Single(((Activity)activity).Attachments);
    //             Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);

    //             // Add a magic code to the adapter
    //             adapter.AddUserToken(connectionName, activity.ChannelId, activity.Recipient.Id, token, magicCode);

    //             // Add an exchangable token to the adapter
    //             adapter.AddExchangeableToken(connectionName, activity.ChannelId, activity.Recipient.Id, exchangeToken, token);
    //         })
    //         .send(oauthPromptActivity)
    //         .assertReply("ended")
    //         .startTest();
    //     }

    // private Activity CreateEventResponse(TestAdapter adapter, IActivity activity, String connectionName, String token) {
    //         // add the token to the TestAdapter
    //         adapter.AddUserToken(connectionName, activity.ChannelId, activity.Recipient.Id, token);

    //         // send an event TokenResponse activity to the botCallback handler
    //         var eventActivity = ((Activity)activity).CreateReply();
    //         eventActivity.Type = ActivityTypes.Event;
    //         var from = eventActivity.From;
    //         eventActivity.From = eventActivity.Recipient;
    //         eventActivity.Recipient = from;
    //         eventActivity.Name = SignInConstants.TokenResponseEventName;
    //         eventActivity.Value = JObject.FromObject(new TokenResponse() {
    //             ConnectionName = connectionName,
    //             Token = token,
    //         });

    //         return eventActivity;
    //     }

    // private void OAuthPromptEndOnInvalidMessageSetting() {
    //         var convoState = new ConversationState(new MemoryStorage());
    //         var dialogState = convoState.CreateProperty<DialogState>("dialogState");

    //         var adapter = new TestAdapter()
    //             .Use(new AutoSaveStateMiddleware(convoState));

    //         var connectionName = "myConnection";

    //         // Create new DialogSet.
    //         var dialogs = new DialogSet(dialogState);
    //         dialogs.Add(new OAuthPrompt("OAuthPrompt", new OAuthPromptSettings() { Text = "Please sign in", ConnectionName = connectionName, Title = "Sign in", EndOnInvalidMessage = true }));

    //         BotCallbackHandler botCallbackHandler =  (turnContext) -> {
    //             var dc =  dialogs.CreateContext(turnContext);

    //             var results =  dc.ContinueDialog(cancellationToken);
    //             if (results.Status == DialogTurnStatus.Empty) {
    //                  dc.Prompt("OAuthPrompt", new PromptOptions(): cancellationToken);
    //             } else if (results.Status == DialogTurnStatus.Waiting) {
    //                 throw new InvalidOperationException("Test OAuthPromptEndOnInvalidMessageSetting expected DialogTurnStatus.Complete");
    //             } else if (results.Status == DialogTurnStatus.Complete) {
    //                 if (results.Result is TokenResponse) {
    //                      turnContext.SendActivity(MessageFactory.Text("Logged in."));
    //                 } else {
    //                      turnContext.SendActivity(MessageFactory.Text("Ended."));
    //                 }
    //             }
    //         };

    //          new TestFlow(adapter, botCallbackHandler)
    //         .send("hello")
    //         .assertReply(activity -> {
    //             Assert.Single(((Activity)activity).Attachments);
    //             Assert.Equal(OAuthCard.ContentType, ((Activity)activity).Attachments[0].ContentType);
    //         })
    //         .send("blah")
    //         .assertReply("Ended.")
    //         .startTest();
    //     }
}
