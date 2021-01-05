package com.microsoft.bot.dialogs;

import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryConnectorClient;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.memory.DialogStateManager;
import com.microsoft.bot.dialogs.memory.DialogStateManagerConfiguration;
import com.microsoft.bot.dialogs.memory.PathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.AtAtPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.AtPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.DollarPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.HashPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.PercentPathResolver;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.Assert;

/**
 * Test for the DialogStateManager.
 */
public class DialogStateManagerTests {

    @Rule
    public TestName name = new TestName();

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

    @Test
    public void testPathResolverNullChecks() {
        DialogsComponentRegistration registration = new DialogsComponentRegistration();

        for (PathResolver resolver : registration.getPathResolvers()) {
            try {
                resolver.transformPath(null);
                fail(String.format(
                        "Should have thrown exception with null for matches()" + resolver.getClass().getName()));

            } catch (Exception ex) {

            }
        }
    }

    @Test
    public void testMemorySnapshot() {
        DialogTestFunction testFunction = dialogContext -> {
            JsonNode snapshot = dialogContext.getState().getMemorySnapshot();
            DialogStateManager dsm = new DialogStateManager(dialogContext, null);
            for (MemoryScope memoryScope : dsm.getConfiguration().getMemoryScopes()) {
                if (memoryScope.getIncludeInSnapshot()) {
                    Assert.assertNotNull(snapshot.get(memoryScope.getName()));
                } else {
                    Assert.assertNull(snapshot.get(memoryScope.getName()));
                }
            }
            return CompletableFuture.completedFuture(null);
        };

        createDialogContext(testFunction).startTest().join();
    }

    @Test
    public void testPathResolverTransform() {
        // dollar tests
        Assert.assertEquals("$", new DollarPathResolver().transformPath("$"));
        Assert.assertEquals("$23", new DollarPathResolver().transformPath("$23"));
        Assert.assertEquals("$$", new DollarPathResolver().transformPath("$$"));
        Assert.assertEquals("dialog.foo", new DollarPathResolver().transformPath("$foo"));
        Assert.assertEquals("dialog.foo.bar", new DollarPathResolver().transformPath("$foo.bar"));
        Assert.assertEquals("dialog.foo.bar[0]", new DollarPathResolver().transformPath("$foo.bar[0]"));

        // hash tests
        Assert.assertEquals("#", new HashPathResolver().transformPath("#"));
        Assert.assertEquals("#23", new HashPathResolver().transformPath("#23"));
        Assert.assertEquals("##", new HashPathResolver().transformPath("##"));
        Assert.assertEquals("turn.recognized.intents.foo", new HashPathResolver().transformPath("#foo"));
        Assert.assertEquals("turn.recognized.intents.foo.bar", new HashPathResolver().transformPath("#foo.bar"));
        Assert.assertEquals("turn.recognized.intents.foo.bar[0]", new HashPathResolver().transformPath("#foo.bar[0]"));

        // @ test
        Assert.assertEquals("@", new AtPathResolver().transformPath("@"));
        Assert.assertEquals("@23", new AtPathResolver().transformPath("@23"));
        Assert.assertEquals("@@foo", new AtPathResolver().transformPath("@@foo"));
        Assert.assertEquals("turn.recognized.entities.foo.first()", new AtPathResolver().transformPath("@foo"));
        Assert.assertEquals("turn.recognized.entities.foo.first().bar", new AtPathResolver().transformPath("@foo.bar"));

        // @@ teest
        Assert.assertEquals("@@", new AtAtPathResolver().transformPath("@@"));
        Assert.assertEquals("@@23", new AtAtPathResolver().transformPath("@@23"));
        Assert.assertEquals("@@@@", new AtAtPathResolver().transformPath("@@@@"));
        Assert.assertEquals("turn.recognized.entities.foo", new AtAtPathResolver().transformPath("@@foo"));

        // % config tests
        Assert.assertEquals("%", new PercentPathResolver().transformPath("%"));
        Assert.assertEquals("%23", new PercentPathResolver().transformPath("%23"));
        Assert.assertEquals("%%", new PercentPathResolver().transformPath("%%"));
        Assert.assertEquals("class.foo", new PercentPathResolver().transformPath("%foo"));
        Assert.assertEquals("class.foo.bar", new PercentPathResolver().transformPath("%foo.bar"));
        Assert.assertEquals("class.foo.bar[0]", new PercentPathResolver().transformPath("%foo.bar[0]"));
    }

    @Test
    public void testSimpleValues() {
        DialogTestFunction testFunction = dc -> {
            // simple value types
            dc.getState().setValue("UseR.nuM", 15);
            dc.getState().setValue("uSeR.NuM", 25);
            Assert.assertEquals(25, (int) dc.getState().getValue("user.num", 0, Integer.class));

            dc.getState().setValue("UsEr.StR", "string1");
            dc.getState().setValue("usER.STr", "string2");
            Assert.assertEquals("string2", dc.getState().getValue("USer.str", "", String.class));

            // simple value types
            dc.getState().setValue("ConVErsation.nuM", 15);
            dc.getState().setValue("ConVErSation.NuM", 25);
            Assert.assertEquals(25, (int) dc.getState().getValue("conversation.num", 0, Integer.class));

            dc.getState().setValue("ConVErsation.StR", "string1");
            dc.getState().setValue("CoNVerSation.STr", "string2");
            Assert.assertEquals("string2", dc.getState().getValue("conversation.str", "", String.class));

            // simple value types
            dc.getState().setValue("tUrn.nuM", 15);
            dc.getState().setValue("turN.NuM", 25);
            Assert.assertEquals(25, (int) dc.getState().getValue("turn.num", 0, Integer.class));

            dc.getState().setValue("tuRn.StR", "string1");
            dc.getState().setValue("TuRn.STr", "string2");
            Assert.assertEquals("string2", dc.getState().getValue("turn.str", "", String.class));

            return CompletableFuture.completedFuture(null);
        };

        createDialogContext(testFunction).startTest().join();
    }

    private TestFlow createDialogContext(DialogTestFunction handler) {
        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference(name.getMethodName(), "User1", "Bot"))
                        .useStorage(new MemoryStorage()).useBotState(new UserState(new MemoryStorage()))
                        .useBotState(new ConversationState(new MemoryStorage()));

        DialogManager dm = new DialogManager(new LamdbaDialog(name.getMethodName(), handler), name.getMethodName());
        // dm.getInitialTurnState().add(new ResourceExplorer());
        return new TestFlow(adapter, (turnContext -> {
            dm.onTurn(turnContext);
            return CompletableFuture.completedFuture(null);
        })).sendConverationUpdate();
    }
}
