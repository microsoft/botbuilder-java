// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.SendActivitiesHandler;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.TraceTranscriptLogger;
import com.microsoft.bot.builder.TranscriptLoggerMiddleware;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.prompts.PromptOptions;
import com.microsoft.bot.dialogs.prompts.TextPrompt;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.ResultPair;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class DialogManagerTests {

    // An App D for a parent bot.
    private final String _parentBotId = UUID.randomUUID().toString();

    // An App D for a skill bot.
    private final String _skillBotId = UUID.randomUUID().toString();

    // Captures an EndOfConversation if it was sent to help with assertions.
    private Activity _eocSent;

    // Property to capture the DialogManager turn results and do assertions.
    private DialogManagerResult _dmTurnResult;

    /**
     * Enum to handle different skill test cases.
     */
    public enum SkillFlowTestCase {
        /**
         * DialogManager is executing on a root bot with no skills (typical standalone
         * bot).
         */
        RootBotOnly,

        /**
         * DialogManager is executing on a root bot handling replies from a skill.
         */
        RootBotConsumingSkill,

        /**
         * DialogManager is executing in a skill that is called from a root and calling
         * another skill.
         */
        MiddleSkill,

        /**
         * DialogManager is executing in a skill that is called from a parent (a root or
         * another skill) but doesn't call another skill.
         */
        LeafSkill
    }

    @Test
    public void DialogManager_ConversationState_PersistedAcrossTurns() {
        String firstConversationId = UUID.randomUUID().toString();
        MemoryStorage storage = new MemoryStorage();

        Dialog adaptiveDialog = CreateTestDialog("conversation.name");

        CreateFlow(adaptiveDialog, storage, firstConversationId)
        .send("hi")
        .assertReply("Hello, what is your name?")
        .send("Carlos")
        .assertReply("Hello Carlos, nice to meet you!")
        .send("hi")
        .assertReply("Hello Carlos, nice to meet you!")
        .startTest()
        .join();
    }

    @Test
    public void DialogManager_AlternateProperty() {
        String firstConversationId = UUID.randomUUID().toString();
        MemoryStorage storage = new MemoryStorage();

        Dialog adaptiveDialog = CreateTestDialog("conversation.name");

        CreateFlow(adaptiveDialog, storage, firstConversationId, "dialogState", null, null)
        .send("hi")
        .assertReply("Hello, what is your name?")
        .send("Carlos")
        .assertReply("Hello Carlos, nice to meet you!")
        .send("hi")
        .assertReply("Hello Carlos, nice to meet you!")
        .startTest()
        .join();
    }

    @Test
    public void DialogManager_ConversationState_ClearedAcrossConversations() {
        String firstConversationId = UUID.randomUUID().toString();
        String secondConversationId = UUID.randomUUID().toString();
        MemoryStorage storage = new MemoryStorage();

        Dialog adaptiveDialog = CreateTestDialog("conversation.name");

        CreateFlow(adaptiveDialog, storage, firstConversationId)
        .send("hi")
        .assertReply("Hello, what is your name?")
        .send("Carlos")
        .assertReply("Hello Carlos, nice to meet you!")
        .startTest()
        .join();

        CreateFlow(adaptiveDialog, storage, secondConversationId)
        .send("hi")
        .assertReply("Hello, what is your name?")
        .send("John")
        .assertReply("Hello John, nice to meet you!")
        .startTest()
        .join();
    }

    @Test
    public void DialogManager_UserState_PersistedAcrossConversations() {
        String firstConversationId = UUID.randomUUID().toString();
        String secondConversationId = UUID.randomUUID().toString();
        MemoryStorage storage = new MemoryStorage();

        Dialog adaptiveDialog = CreateTestDialog("user.name");

        CreateFlow(adaptiveDialog, storage, firstConversationId)
        .send("hi")
        .assertReply("Hello, what is your name?")
        .send("Carlos")
        .assertReply("Hello Carlos, nice to meet you!")
        .startTest()
        .join();

        CreateFlow(adaptiveDialog, storage, secondConversationId)
        .send("hi")
        .assertReply("Hello Carlos, nice to meet you!")
        .startTest()
        .join();
    }

    @Test
    public void
    DialogManager_UserState_NestedDialogs_PersistedAcrossConversations() {
        String firstConversationId = UUID.randomUUID().toString();
        String secondConversationId = UUID.randomUUID().toString();
        MemoryStorage storage = new MemoryStorage();

        Dialog outerAdaptiveDialog = CreateTestDialog("user.name");

        ComponentDialog componentDialog = new ComponentDialog(null);
        componentDialog.addDialog(outerAdaptiveDialog);

        CreateFlow(componentDialog, storage, firstConversationId)
        .send("hi")
        .assertReply("Hello, what is your name?")
        .send("Carlos")
        .assertReply("Hello Carlos, nice to meet you!")
        .startTest()
        .join();

        CreateFlow(componentDialog, storage, secondConversationId)
        .send("hi")
        .assertReply("Hello Carlos, nice to meet you!")
        .startTest()
        .join();
    }

    // @Test
    // public CompletableFuture<Void> DialogManager_OnErrorEvent_Leaf() {
    //      TestUtilities.RunTestScript();
    // }

    // @Test
    // public CompletableFuture<Void> DialogManager_OnErrorEvent_Parent() {
    //      TestUtilities.RunTestScript();
    // }

    // @Test
    // public CompletableFuture<Void> DialogManager_OnErrorEvent_Root() {
    //      TestUtilities.RunTestScript();
    // }

    // @Test
    // public CompletableFuture<Void> DialogManager_DialogSet() {
    //     var storage = new MemoryStorage();
    //     var convoState = new ConversationState(storage);
    //     var userState = new UserState(storage);

    //     var adapter = new TestAdapter();
    //     adapter
    //         .UseStorage(storage)
    //         .UseBotState(userState, convoState)
    //         .Use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger(traceActivity: false)));

    //     var rootDialog = new AdaptiveDialog() {
    //         Triggers new ArrayList<OnCondition>() {
    //             new OnBeginDialog() {
    //                 Actions new ArrayList<Dialog>() {
    //                     new SetProperty() {
    //                         Property = "conversation.dialogId",
    //                         Value = "test"
    //                     },
    //                     new BeginDialog() {
    //                         Dialog = "=conversation.dialogId"
    //                     },
    //                     new BeginDialog() {
    //                         Dialog = "test"
    //                     }
    //                 }
    //             }
    //         }
    //     };

    //     var dm = new DialogManager(rootDialog);
    //     dm.Dialogs.Add(new SimpleDialog() { Id = "test" });

    //      new TestFlow(adapter,  (turnContext) -> {
    //          dm.OnTurn(turnContext: cancellationToken);
    //     })
    //         .SendConversationUpdate()
    //             .assertReply("simple")
    //             .assertReply("simple")
    //         .startTest();
    // }

    // @Test
    // public CompletableFuture<Void> DialogManager_ContainerRegistration() {
    //     var root = new AdaptiveDialog("root") {
    //         Triggers new ArrayList<OnCondition> {
    //             new OnBeginDialog() {
    //                 Actions new ArrayList<Dialog> { new AdaptiveDialog("inner") }
    //             }
    //         }
    //     };

    //     var storage = new MemoryStorage();
    //     var convoState = new ConversationState(storage);
    //     var userState = new UserState(storage);

    //     var adapter = new TestAdapter();
    //     adapter
    //         .UseStorage(storage)
    //         .UseBotState(userState, convoState);

    //     // The inner adaptive dialog should be registered on the DialogManager after OnTurn
    //     var dm = new DialogManager(root);

    //      new TestFlow(adapter,  (turnContext) -> {
    //          dm.OnTurn(turnContext: cancellationToken);
    //     })
    //         .SendConversationUpdate()
    //         .startTest();

    //     Assert.NotNull(dm.Dialogs.Find("inner"));
    // }

    // @Test
    // public CompletableFuture<Void> DialogManager_ContainerRegistration_DoubleNesting() {
    //     // Create the following dialog tree
    //     // Root (adaptive) -> inner (adaptive) -> innerinner(adaptive) -> helloworld (SendActivity)
    //     var root = new AdaptiveDialog("root") {
    //         Triggers new ArrayList<OnCondition> {
    //             new OnBeginDialog() {
    //                 Actions new ArrayList<Dialog>  {
    //                     new AdaptiveDialog("inner") {
    //                         Triggers new ArrayList<OnCondition> {
    //                             new OnBeginDialog() {
    //                                 Actions new ArrayList<Dialog> {
    //                                     new AdaptiveDialog("innerinner") {
    //                                         Triggers new ArrayList<OnCondition>() {
    //                                             new OnBeginDialog() {
    //                                                 Actions new ArrayList<Dialog>() {
    //                                                     new SendActivity("helloworld")
    //                                                 }
    //                                             }
    //                                         }
    //                                     }
    //                                 }
    //                             }
    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //     };

    //     var storage = new MemoryStorage();
    //     var convoState = new ConversationState(storage);
    //     var userState = new UserState(storage);

    //     var adapter = new TestAdapter();
    //     adapter
    //         .UseStorage(storage)
    //         .UseBotState(userState, convoState);

    //     // The inner adaptive dialog should be registered on the DialogManager after OnTurn
    //     var dm = new DialogManager(root);

    //      new TestFlow(adapter,  (turnContext) -> {
    //          dm.OnTurn(turnContext: cancellationToken);
    //     })
    //         .SendConversationUpdate()
    //         .startTest();

    //     // Top level containers should be registered
    //     Assert.NotNull(dm.Dialogs.Find("inner"));

    //     // Mid level containers should be registered
    //     Assert.NotNull(dm.Dialogs.Find("innerinner"));

    //     // Leaf nodes / non-contaners should not be registered
    //     Assert.DoesNotContain(dm.Dialogs.GetDialogs(), d -> d.GetType() == typeof(SendActivity));
    // }

    // public CompletableFuture<Void> HandlesBotAndSkillsTestCases(SkillFlowTestCase testCase, boolean shouldSendEoc) {
    //     var firstConversationId = Guid.NewGuid().toString();
    //     var storage = new MemoryStorage();

    //     var adaptiveDialog = CreateTestDialog(property: "conversation.name");
    //      CreateFlow(adaptiveDialog, storage, firstConversationId, testCase: testCase, locale: "en-GB").send("Hi")
    //         .assertReply("Hello, what is your name?")
    //         .send("SomeName")
    //         .assertReply("Hello SomeName, nice to meet you!")
    //         .startTest();

    //     Assert.Equal(DialogTurnStatus.Complete, _dmTurnResult.TurnResult.Status);

    //     if (shouldSendEoc) {
    //         Assert.NotNull(_eocSent);
    //         Assert.Equal(ActivityTypes.EndOfConversation, _eocSent.Type);
    //         Assert.Equal("SomeName", _eocSent.Value);
    //         Assert.Equal("en-GB", _eocSent.Locale);
    //     } else {
    //         Assert.Null(_eocSent);
    //     }
    // }

    // @Test
    // public CompletableFuture<Void> SkillHandlesEoCFromParent() {
    //     var firstConversationId = Guid.NewGuid().toString();
    //     var storage = new MemoryStorage();

    //     var adaptiveDialog = CreateTestDialog(property: "conversation.name");

    //     var eocActivity = new Activity(ActivityTypes.EndOfConversation);

    //      CreateFlow(adaptiveDialog, storage, firstConversationId, testCase: SkillFlowTestCase.LeafSkill)
    //         .send("hi")
    //         .assertReply("Hello, what is your name?")
    //         .send(eocActivity)
    //         .startTest();

    //     Assert.Equal(DialogTurnStatus.Cancelled, _dmTurnResult.TurnResult.Status);
    // }

    // @Test
    // public CompletableFuture<Void> SkillHandlesRepromptFromParent() {
    //     var firstConversationId = Guid.NewGuid().toString();
    //     var storage = new MemoryStorage();

    //     var adaptiveDialog = CreateTestDialog(property: "conversation.name");

    //     var repromptEvent = new Activity(ActivityTypes.Event) { Name = DialogEvents.RepromptDialog };

    //      CreateFlow(adaptiveDialog, storage, firstConversationId, testCase: SkillFlowTestCase.LeafSkill)
    //         .send("hi")
    //         .assertReply("Hello, what is your name?")
    //         .send(repromptEvent)
    //         .assertReply("Hello, what is your name?")
    //         .startTest();

    //     Assert.Equal(DialogTurnStatus.Waiting, _dmTurnResult.TurnResult.Status);
    // }

    // @Test
    // public CompletableFuture<Void> SkillShouldReturnEmptyOnRepromptWithNoDialog() {
    //     var firstConversationId = Guid.NewGuid().toString();
    //     var storage = new MemoryStorage();

    //     var adaptiveDialog = CreateTestDialog(property: "conversation.name");

    //     var repromptEvent = new Activity(ActivityTypes.Event) { Name = DialogEvents.RepromptDialog };

    //      CreateFlow(adaptiveDialog, storage, firstConversationId, testCase: SkillFlowTestCase.LeafSkill)
    //         .send(repromptEvent)
    //         .startTest();

    //     Assert.Equal(DialogTurnStatus.Empty, _dmTurnResult.TurnResult.Status);
    // }

    private Dialog CreateTestDialog(String property) {
        return new AskForNameDialog(property.replace(".", ""), property);
    }

    private TestFlow CreateFlow(Dialog dialog, Storage storage, String conversationId) {
        return this.CreateFlow(dialog, storage, conversationId, null, SkillFlowTestCase.RootBotOnly, null);
    }

    private TestFlow CreateFlow(Dialog dialog, Storage storage, String conversationId, String dialogStateProperty,
            SkillFlowTestCase testCase, String locale) {
        if (testCase == null) {
            testCase = SkillFlowTestCase.RootBotOnly;
        }
        ConversationState convoState = new ConversationState(storage);
        UserState userState = new UserState(storage);

        TestAdapter adapter = new TestAdapter(
            TestAdapter.createConversationReference(conversationId, "User1", "Bot"));
        adapter.useStorage(storage)
            .useBotState(userState, convoState)
            .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        if (!StringUtils.isBlank(locale)) {
            adapter.setLocale(locale);
        }
        final SkillFlowTestCase finalTestCase = testCase;
        DialogManager dm = new DialogManager(dialog, dialogStateProperty);
        return new TestFlow(adapter,  (turnContext) -> {
            if (finalTestCase != SkillFlowTestCase.RootBotOnly) {
                throw new NotImplementedException("CreateFlow is only capable of RootBotOnly test for now.");
                // Create a skill ClaimsIdentity and put it in TurnState so SkillValidation.IsSkillClaim() returns true.
                // ClaimsIdentity claimsIdentity = new ClaimsIdentity();
                // claimsIdentity.AddClaim(new Claim(AuthenticationConstants.VersionClaim, "2.0"));
                // claimsIdentity.AddClaim(new Claim(AuthenticationConstants.AudienceClaim, _skillBotId));
                // claimsIdentity.AddClaim(new Claim(AuthenticationConstants.AuthorizedParty, _parentBotId));
                // turnContext.TurnState.Add(BotAdapter.BotIdentityKey, claimsIdentity);

                // if (testCase == SkillFlowTestCase.RootBotConsumingSkill) {
                //     // Simulate the SkillConversationReference with a channel OAuthScope stored in TurnState.
                //     // This emulates a response coming to a root bot through SkillHandler.
                //     turnContext.TurnState.Add(SkillHandler.SkillConversationReferenceKey, new SkillConversationReference { OAuthScope = AuthenticationConstants.ToChannelFromBotOAuthScope });
                // }

                // if (testCase == SkillFlowTestCase.MiddleSkill) {
                //     // Simulate the SkillConversationReference with a parent Bot D stored in TurnState.
                //     // This emulates a response coming to a skill from another skill through SkillHandler.
                //     turnContext.TurnState.Add(SkillHandler.SkillConversationReferenceKey, new SkillConversationReference { OAuthScope = _parentBotId });
                // }
            }

            turnContext.onSendActivities(new TestSendActivities());

            // Capture the last DialogManager turn result for assertions.
            _dmTurnResult =  dm.onTurn(turnContext).join();

            return CompletableFuture.completedFuture(null);
        });
    }

    class TestSendActivities implements SendActivitiesHandler {
        @Override
        public CompletableFuture<ResourceResponse[]> invoke(TurnContext context, List<Activity> activities,
                Supplier<CompletableFuture<ResourceResponse[]>> next) {
            for (Activity activity : activities) {
                if (activity.getType().equals(ActivityTypes.END_OF_CONVERSATION)) {
                    _eocSent = activity;
                    break;
                }
            }
            return next.get();
        }
    }

    private class AskForNameDialog extends ComponentDialog implements DialogDependencies {
        private final String property;

        private AskForNameDialog(String id, String property) {
            super(id);
            addDialog(new TextPrompt("prompt"));
            this.property = property;
        }

        @Override
        public List<Dialog> getDependencies() {
            return new ArrayList<Dialog>(getDialogs().getDialogs());
        }

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext outerDc, Object options) {

            ResultPair<String> value = outerDc.getState().tryGetValue(property, String.class);
            if (value.getLeft()) {
                outerDc.getContext().sendActivity(String.format("Hello %s, nice to meet you!", value.getRight()));
                return outerDc.endDialog(value.getRight());
            }

            PromptOptions pOptions = new PromptOptions();
            Activity prompt = new Activity(ActivityTypes.MESSAGE);
            prompt.setText("Hello, what is your name?");
            Activity retryPrompt = new Activity(ActivityTypes.MESSAGE);
            retryPrompt.setText("Hello, what is your name?");
            pOptions.setPrompt(prompt);
            pOptions.setRetryPrompt(retryPrompt);

            return  outerDc.beginDialog("prompt", pOptions);
        }

        @Override
        public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext outerDc,
                                                        DialogReason reason, Object result) {
            outerDc.getState().setValue(property, result);
            outerDc.getContext().sendActivity(String.format("Hello %s, nice to meet you!", result)).join();
            return  outerDc.endDialog(result);
        }
    }
}
