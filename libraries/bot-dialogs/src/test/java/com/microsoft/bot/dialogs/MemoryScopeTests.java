// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.BotState;
import com.microsoft.bot.builder.ComponentRegistration;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.memory.DialogStateManager;
import com.microsoft.bot.dialogs.memory.scopes.BotStateMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.ClassMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.ConversationMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.DialogClassMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.DialogContextMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.DialogMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.ThisMemoryScope;
import com.microsoft.bot.dialogs.memory.scopes.UserMemoryScope;

import org.javatuples.Pair;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MemoryScopeTests {

    @Rule
    public TestName testName = new TestName();
    public TestFlow CreateDialogContext(DialogTestFunction handler) {
        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference(testName.getMethodName(), "User1", "Bot"));
        adapter.useStorage(new MemoryStorage()).useBotState(new UserState(new MemoryStorage()))
                .useBotState(new ConversationState(new MemoryStorage()));
        DialogManager dm = new DialogManager(new LamdbaDialog(testName.getMethodName(), handler), null);
        return new TestFlow(adapter, (turnContext) -> {
            return dm.onTurn(turnContext).thenApply(dialogManagerResult -> null);
        }).sendConversationUpdate();
    }

    @Test
    public void SimpleMemoryScopesTest() {
        DialogTestFunction testFunction = dc -> {
            DialogStateManager dsm = dc.getState();
            for (MemoryScope memoryScope : dsm.getConfiguration().getMemoryScopes()) {
                if (!(memoryScope instanceof ThisMemoryScope || memoryScope instanceof DialogMemoryScope
                        || memoryScope instanceof ClassMemoryScope || memoryScope instanceof DialogClassMemoryScope
                        || memoryScope instanceof DialogContextMemoryScope)) {
                    Object memory = memoryScope.getMemory(dc);
                    Assert.assertNotNull(memory);
                    ObjectPath.setPathValue(memory, "test", 15);
                    memory = memoryScope.getMemory(dc);
                    Assert.assertEquals(15, (int) ObjectPath.getPathValue(memory, "test", Integer.class));
                    ObjectPath.setPathValue(memory, "test", 25);
                    memory = memoryScope.getMemory(dc);
                    Assert.assertEquals(25, (int) ObjectPath.getPathValue(memory, "test", Integer.class));
                    memory = memoryScope.getMemory(dc);
                    ObjectPath.setPathValue(memory, "source", "destination");
                    ObjectPath.setPathValue(memory, "{source}", 24);
                    Assert.assertEquals(24, (int) ObjectPath.getPathValue(memory, "{source}", Integer.class));
                    ObjectPath.removePathValue(memory, "{source}");
                    Assert.assertNull(ObjectPath.tryGetPathValue(memory, "{source}", Integer.class));
                    ObjectPath.removePathValue(memory, "source");
                    Assert.assertNull(ObjectPath.tryGetPathValue(memory, "{source}", Integer.class));
                }
            }
            return CompletableFuture.completedFuture(null);
        };
        CreateDialogContext(testFunction).startTest().join();
    }

    @Test
    public void BotStateMemoryScopeTest() {
        DialogTestFunction testFunction = dc -> {
            DialogStateManager dsm = dc.getState();
            Storage storage = dc.getContext().getTurnState().get(MemoryStorage.class);
            UserState userState = dc.getContext().getTurnState().get(UserState.class);
            ConversationState conversationState = dc.getContext().getTurnState().get(ConversationState.class);
            CustomState customState = new CustomState(storage);

            dc.getContext().getTurnState().add(customState);

            List<Pair<BotState, MemoryScope>> stateScopes = new ArrayList<Pair<BotState, MemoryScope>>();
            stateScopes.add(Pair.with(userState, new UserMemoryScope()));
            stateScopes.add(Pair.with(conversationState, new ConversationMemoryScope()));
            stateScopes.add(Pair.with(customState,
                            new BotStateMemoryScope<CustomState>(CustomState.class, "CustomState")));

            for (Pair<BotState, MemoryScope> stateScope : stateScopes) {
                final String name = "test-name";
                final String value = "test-value";
                stateScope.getValue0().createProperty(name).set(dc.getContext(), value).join();

                Object memory = stateScope.getValue1().getMemory(dc);

                Assert.assertEquals(value, ObjectPath.getPathValue(memory, name, String.class));
            }
            return CompletableFuture.completedFuture(null);
        };
        CreateDialogContext(testFunction).startTest().join();
    }

    public class CustomState extends BotState {
        public CustomState(Storage storage) {
            super(storage, "Not the name of the type");
        }

        @Override
        public String getStorageKey(TurnContext turnContext) {
            // TODO Auto-generated method stub
            return "botstate/custom/etc";
        }
    }
}
