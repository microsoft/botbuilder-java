// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.prompts.PromptOptions;
import com.microsoft.bot.dialogs.prompts.TextPrompt;

import org.junit.Assert;
import org.junit.Test;

public class ReplaceDialogTests {

    @Test
    public void ReplaceDialogNoBranch() {
        FirstDialog dialog = new FirstDialog();

        MemoryStorage storage = new MemoryStorage();
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);
        TestAdapter adapter = new TestAdapter()
            .useStorage(storage)
            .useBotState(userState, conversationState);
        DialogManager dialogManager = new DialogManager(dialog, null);
        new TestFlow(adapter,  (turnContext) -> {
            dialogManager.onTurn(turnContext).join();
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("prompt one")
        .send("hello")
        .assertReply("prompt two")
        .send("hello")
        .assertReply("prompt three")
        .send("hello")
        .assertReply("*** WaterfallDialog End ***")
        .startTest()
        .join();
    }

    @Test
    public void ReplaceDialogBranch() {
        FirstDialog dialog = new FirstDialog();

        MemoryStorage storage = new MemoryStorage();
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);
        TestAdapter adapter = new TestAdapter()
            .useStorage(storage)
            .useBotState(userState, conversationState);
        DialogManager dialogManager = new DialogManager(dialog, null);
        new TestFlow(adapter,  (turnContext) -> {
            dialogManager.onTurn(turnContext).join();
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .assertReply("prompt one")
        .send("hello")
        .assertReply("prompt two")
        .send("replace")
        .assertReply("*** WaterfallDialog End ***")
        .assertReply("prompt four")
        .send("hello")
        .assertReply("prompt five")
        .send("hello")
        .startTest()
        .join();
     }

    @Test
    public void ReplaceDialogTelemetryClientNotNull() {
        FirstDialog dialog = new FirstDialog();

        MemoryStorage storage = new MemoryStorage();
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);
        TestAdapter adapter = new TestAdapter()
            .useStorage(storage)
            .useBotState(userState, conversationState);
        DialogManager dialogManager = new DialogManager(dialog, null);
        new TestFlow(adapter,  (turnContext) -> {
            Assert.assertNotNull(dialog.getTelemetryClient());
            dialogManager.onTurn(turnContext).join();
            return CompletableFuture.completedFuture(null);
        })
        .send("hello")
        .startTest()
        .join();
    }

    private class FirstDialog extends ComponentDialog {
        public FirstDialog() {
            super("FirstDialog");
            WaterfallStep[] steps = new WaterfallStep[] {
                new ActionOne(),
                new ActionTwo(),
                new ReplaceAction(),
                new ActionThree(),
                new LastAction(),
            };

            addDialog(new TextPrompt("TextPrompt"));
            addDialog(new SecondDialog());
            addDialog(new WaterfallWithEndDialog("WaterfallWithEndDialog", steps));

            setInitialDialogId("WaterfallWithEndDialog");
        }

        private class ActionOne implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("prompt one"));
                return stepContext.prompt("TextPrompt", options);
            }
        }

        private class ActionTwo implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("prompt two"));
                return stepContext.prompt("TextPrompt", options);
            }
        }

        private class ReplaceAction implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                if (((String)stepContext.getResult()).equals("replace")) {
                    return  stepContext.replaceDialog("SecondDialog");
                } else {
                    return  stepContext.next(null);
                }
            }
        }
        private class ActionThree implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("prompt three"));
                return stepContext.prompt("TextPrompt", options);
            }
        }

        private class LastAction implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                return stepContext.endDialog();
            }
        }

        private class WaterfallWithEndDialog extends WaterfallDialog {
            private WaterfallWithEndDialog(String id, WaterfallStep[] steps) {
                super(id, Arrays.asList(steps));
            }

            @Override
            public CompletableFuture<Void> endDialog(TurnContext turnContext, DialogInstance instance,
                                                     DialogReason reason) {
                 turnContext.sendActivity(MessageFactory.text("*** WaterfallDialog End ***")).join();
                 return super.endDialog(turnContext, instance, reason);
            }
        }
    }

    private class SecondDialog extends ComponentDialog {
        private SecondDialog() {
            super("SecondDialog");
            WaterfallStep[] steps = new WaterfallStep[] {
                new ActionFour(),
                new ActionFive(),
                new LastAction(),
            };

            addDialog(new TextPrompt("TextPrompt"));
            addDialog(new WaterfallDialog("WaterfallDialog", Arrays.asList(steps)));

            setInitialDialogId("WaterfallDialog");
        }

        private class ActionFour implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("prompt four"));
                return stepContext.prompt("TextPrompt", options);
            }
        }

        private class ActionFive implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                PromptOptions options = new PromptOptions();
                options.setPrompt(MessageFactory.text("prompt five"));
                return stepContext.prompt("TextPrompt", options);
            }
        }

        private class LastAction implements WaterfallStep {
            @Override
            public CompletableFuture<DialogTurnResult> waterfallStep(WaterfallStepContext stepContext) {
                return  stepContext.endDialog();
            }

        }
    }
}

