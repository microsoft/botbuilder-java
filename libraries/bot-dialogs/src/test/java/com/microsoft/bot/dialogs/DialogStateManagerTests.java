package com.microsoft.bot.dialogs;

import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.memory.DialogStateManagerConfiguration;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Test for the DialogStateManager.
 */
public class DialogStateManagerTests {

    @Rule public TestName name = new TestName();

    @Test
    public void testMemoryScopeNullChecks() {
        DialogTestFunction testFunction = dialogContext -> {
            DialogStateManagerConfiguration configuration = dialogContext.getState().getConfiguration();
            for (MemoryScope scope : configuration.getMemoryScopes()) {
                try {
                    scope.getMemory(null);
                    fail(String.format("Should have thrown exception with null for getMemory %s",
                        scope.getClass().getName()));
                } catch (Exception ex) {
                }
                try {
                    scope.setMemory(null, new Object());
                    fail(String.format("Should have thrown exception with null for setMemory %s",
                        scope.getClass().getName()));
                } catch (Exception ex) {

                }
            }
            return CompletableFuture.completedFuture(null);
        };

        createDialogContext(testFunction).startTest().join();
    }

    private TestFlow createDialogContext(DialogTestFunction handler) {
        TestAdapter adapter = new TestAdapter(
            TestAdapter.createConversationReference(name.getMethodName(), "User1", "Bot"))
            .useStorage(new MemoryStorage())
            .useBotState(new UserState(new MemoryStorage()))
            .useBotState(new ConversationState(new MemoryStorage()));

        DialogManager dm = new DialogManager(new LamdbaDialog(name.getMethodName(), handler), name.getMethodName());
        //dm.getInitialTurnState().add(new ResourceExplorer());
        return new TestFlow(adapter, (turnContext -> {
            dm.onTurn(turnContext);
            return CompletableFuture.completedFuture(null);
        })).sendConverationUpdate();
    }
}
