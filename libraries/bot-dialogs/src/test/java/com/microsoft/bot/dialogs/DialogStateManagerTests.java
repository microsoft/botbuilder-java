package com.microsoft.bot.dialogs;

import java.util.function.BiFunction;

import com.google.common.base.Function;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;

import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Test for the DialogStateManager.
 */
public class DialogStateManagerTests {

    @Rule public TestName name = new TestName();

    private TestFlow createDialogContext(BiFunction<DialogContext, Function, TestFlow> tf) {
        TestAdapter adapter = new TestAdapter(
                    TestAdapter.createConversationReference(name.getMethodName(), "User1", "Bot"));
        //     adapter.UseStorage(new MemoryStorage())
        //     .UseBotState(new UserState(new MemoryStorage()))
        //     .UseBotState(new ConversationState(new MemoryStorage()));

        DialogManager dm = new DialogManager(new LamdaDialog(handler), null);
        dm.InitialTurnState.Set(new ResourceExplorer());
        return new TestFlow(adapter, dm.OnTurnAsync).SendConversationUpdate();
    }
}
