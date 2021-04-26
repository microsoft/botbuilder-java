// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.TraceTranscriptLogger;
import com.microsoft.bot.builder.TranscriptLoggerMiddleware;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.dialogs.memory.DialogStateManager;
import com.microsoft.bot.dialogs.memory.DialogStateManagerConfiguration;
import com.microsoft.bot.dialogs.memory.PathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.AliasPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.AtAtPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.AtPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.DollarPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.HashPathResolver;
import com.microsoft.bot.dialogs.memory.pathresolvers.PercentPathResolver;
import com.microsoft.bot.dialogs.memory.scopes.MemoryScope;
import com.microsoft.bot.schema.ResultPair;

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

    @Test
    public void TestEntitiesRetrieval() {
        DialogTestFunction testFunction = dc -> {

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();

            String[] array = new String[] { "test1", "test2", "test3" };

            String[] array2 = new String[] { "testx", "testy", "testz" };

            String[][] arrayarray = new String[][] { array2, array };

            JsonNode arrayNode = mapper.valueToTree(array);
            JsonNode arrayArrayNode = mapper.valueToTree(arrayarray);

            dc.getState().setValue("turn.recognized.entities.single", arrayNode);
            dc.getState().setValue("turn.recognized.entities.double", arrayArrayNode);

            Assert.assertEquals("test1", dc.getState().getValue("@single", new String(), String.class));
            Assert.assertEquals("testx", dc.getState().getValue("@double", new String(), String.class));
            Assert.assertEquals("test1",
                    dc.getState().getValue("turn.recognized.entities.single.First()", new String(), String.class));
            Assert.assertEquals("testx",
                    dc.getState().getValue("turn.recognized.entities.double.First()", new String(), String.class));

            // arrayarray = new JArray();
            ArrayNode secondArray = mapper.createArrayNode();
            ArrayNode array1Node = mapper.createArrayNode();
            ObjectNode node1 = mapper.createObjectNode();
            node1.put("name", "test1");
            ObjectNode node2 = mapper.createObjectNode();
            node2.put("name", "test2");
            ObjectNode node3 = mapper.createObjectNode();
            node3.put("name", "test3");
            array1Node.addAll(Arrays.asList(node1, node2, node3));

            ArrayNode array2Node = mapper.createArrayNode();
            ObjectNode node1a = mapper.createObjectNode();
            node1a.put("name", "testx");
            ObjectNode node2a = mapper.createObjectNode();
            node2a.put("name", "testy");
            ObjectNode node3a = mapper.createObjectNode();
            node3a.put("name", "testz");
            array2Node.addAll(Arrays.asList(node1a, node2a, node3a));
            secondArray.addAll(Arrays.asList(array2Node, array1Node));
            dc.getState().setValue("turn.recognized.entities.single", array1Node);
            dc.getState().setValue("turn.recognized.entities.double", secondArray);

            Assert.assertEquals("test1", dc.getState().getValue("@single.name", new String(), String.class));
            Assert.assertEquals("test1",
                    dc.getState().getValue("turn.recognized.entities.single.First().name", new String(), String.class));
            Assert.assertEquals("testx", dc.getState().getValue("@double.name", new String(), String.class));
            Assert.assertEquals("testx",
                    dc.getState().getValue("turn.recognized.entities.double.First().name", new String(), String.class));
            return CompletableFuture.completedFuture(null);
        };

        createDialogContext(testFunction).startTest().join();
    }

    private Foo getFoo() {
        return new Foo("Tom", 15, true, new Bar("bob", 122, false));
    }

    @Test
    public void TestComplexValuePaths() {
        DialogTestFunction testFunction = dc -> {
            // complex type paths
            dc.getState().setValue("UseR.fOo", getFoo());
            Assert.assertEquals("bob", dc.getState().getValue("user.foo.SuBname.name", "", String.class));

            // complex type paths
            dc.getState().setValue("ConVerSation.FOo", getFoo());
            Assert.assertEquals("bob", dc.getState().getValue("conversation.foo.SuBname.name", "", String.class));

            // complex type paths
            dc.getState().setValue("TurN.fOo", getFoo());
            Assert.assertEquals("bob", dc.getState().getValue("TuRN.foo.SuBname.name", "", String.class));
            return CompletableFuture.completedFuture(null);
        };
        createDialogContext(testFunction).startTest().join();
    }

    @Test
    public void TestComplexPathExpressions() {
        DialogTestFunction testFunction = dc -> {
            // complex type paths
            dc.getState().setValue("user.name", "joe");
            dc.getState().setValue("conversation.stuff[user.name]", "test");
            dc.getState().setValue("conversation.stuff['frank']", "test2");
            dc.getState().setValue("conversation.stuff[\"susan\"]", "test3");
            dc.getState().setValue("conversation.stuff['Jo.Bob']", "test4");
            Assert.assertEquals("test", dc.getState().getValue("conversation.stuff.joe", "", String.class));
            Assert.assertEquals("test", dc.getState().getValue("conversation.stuff[user.name]", "", String.class));
            Assert.assertEquals("test2", dc.getState().getValue("conversation.stuff['frank']", "", String.class));
            Assert.assertEquals("test3", dc.getState().getValue("conversation.stuff[\"susan\"]", "", String.class));
            Assert.assertEquals("test4", dc.getState().getValue("conversation.stuff[\"Jo.Bob\"]", "", String.class));
            return CompletableFuture.completedFuture(null);
        };
        createDialogContext(testFunction).startTest().join();
    }

    @Test
    public void TestGetValue() {
        DialogTestFunction testFunction = dc -> {
            // complex type paths
            dc.getState().setValue("user.name.first", "joe");
            Assert.assertEquals("joe", dc.getState().getValue("user.name.first", "", String.class));

            Assert.assertNull(dc.getState().getValue("user.xxx", null, String.class));
            Assert.assertEquals("default", dc.getState().getValue("user.xxx", "default", String.class));

            for (String key : dc.getState().keySet()) {
                if (key.equals("dialogContext")) {
                    Object expected = dc.getState().get(key);
                    Object actual = dc.getState().getValue(key, null, Object.class);
                    Assert.assertTrue(expected.equals(actual));
                }
            }
            return CompletableFuture.completedFuture(null);
        };
        createDialogContext(testFunction).startTest().join();
    }

    @Test
    public void TestTryGetValueWithWrongType() {
        createDialogContext(dc -> {
            dc.getState().setValue("user.name.first", "joe");
            Assert.assertFalse(dc.getState().tryGetValue("user.name.first", Integer.class).getLeft());
            ResultPair<String> result = dc.getState().tryGetValue("user.name.first", String.class);
            Assert.assertTrue(result.getLeft());
            Assert.assertEquals("joe", result.getRight());

            dc.getState().setValue("user.age", 19);
            ResultPair<String> result2 = dc.getState().tryGetValue("user.age", String.class);
            Assert.assertTrue(result2.getLeft());
            Assert.assertEquals("19", result2.getRight());
            ResultPair<Integer> result3 = dc.getState().tryGetValue("user.age", Integer.class);
            Assert.assertTrue(result3.getLeft());
            Assert.assertTrue(19 == result3.getRight());

            dc.getState().setValue("user.salary", "10000");
            ResultPair<String> result4 = dc.getState().tryGetValue("user.salary", String.class);
            Assert.assertTrue(result4.getLeft());
            Assert.assertEquals("10000", result4.getRight());
            ResultPair<Integer> result5 = dc.getState().tryGetValue("user.salary", Integer.class);
            Assert.assertTrue(result5.getLeft());
            Assert.assertTrue(10000 == result5.getRight());

            dc.getState().setValue("user.foo", getFoo());
            ResultPair<String> result6 = dc.getState().tryGetValue("user.foo", String.class);
            Assert.assertFalse(result6.getLeft());
            ResultPair<Foo> result7 = dc.getState().tryGetValue("user.foo", Foo.class);
            Assert.assertTrue(result7.getLeft());
            ResultPair<Map> result8 = dc.getState().tryGetValue("user.foo", Map.class);
            Assert.assertTrue(result8.getLeft());
            ResultPair<Bar> result9 = dc.getState().tryGetValue("user.foo", Bar.class);
            Assert.assertTrue(result9.getLeft());
            return CompletableFuture.completedFuture(null);
        }).startTest().join();
    }

    @Test
    public void TestGetValueT() {
        createDialogContext(dc -> {
            // complex type paths
            dc.getState().setValue("UseR.fOo", getFoo());
            Assert.assertEquals("bob", dc.getState().getValue("user.foo", null, Foo.class).getSubname().getName());

            // complex type paths
            dc.getState().setValue("ConVerSation.FOo", getFoo());
            Assert.assertEquals("bob",
                    dc.getState().getValue("conversation.foo", null, Foo.class).getSubname().getName());

            // complex type paths
            dc.getState().setValue("TurN.fOo", getFoo());
            Assert.assertEquals("bob", dc.getState().getValue("turn.foo", null, Foo.class).getSubname().getName());
            return CompletableFuture.completedFuture(null);
        }).startTest().join();
    }

    @Test
    public void TestSetValue_RootScope() {
        createDialogContext(dc -> {
            try {
                dc.getState().setValue(null, 13);
                fail("Should have thrown with null memory scope");
            } catch (IllegalArgumentException err) {
                Assert.assertTrue(err.getMessage().contains("path"));
            }

            try {
                // complex type paths
                dc.getState().setValue("xxx", 13);
                fail("Should have thrown with unknown memory scope");
            } catch (Exception err) {
                Assert.assertTrue(err.getMessage().contains("does not match memory scope"));
            }
            return CompletableFuture.completedFuture(null);

        }).startTest().join();
    }

    @Test
    public void TestRemoveValue_RootScope() {
        createDialogContext(dc -> {
            try {
                dc.getState().removeValue(null);
                fail("Should have thrown with null memory scope");
            } catch (IllegalArgumentException err) {
                Assert.assertTrue(err.getMessage().toLowerCase().contains("path"));
            }

            try {
                dc.getState().removeValue("user");
                fail("Should have thrown UnsupportedOperationException");
            } catch (UnsupportedOperationException ex) {
            }

            try {
                dc.getState().removeValue("xxx");
                fail("Should have thrown UnsupportedOperationException");
            } catch (UnsupportedOperationException ex) {
                System.out.println(ex.getMessage());
            }
            return CompletableFuture.completedFuture(null);
        }).startTest().join();
    }

    @Test
    public void TestHashResolver() {
        createDialogContext(dc -> {
            // test HASH
            dc.getState().setValue("turn.recognized.intents.test", "intent1");
            dc.getState().setValue("#test2", "intent2");

            Assert.assertEquals("intent1", dc.getState().getValue("turn.recognized.intents.test", "", String.class));
            Assert.assertEquals("intent1", dc.getState().getValue("#test", "", String.class));
            Assert.assertEquals("intent2", dc.getState().getValue("turn.recognized.intents.test2", "", String.class));
            Assert.assertEquals("intent2", dc.getState().getValue("#test2", "", String.class));
            return CompletableFuture.completedFuture(null);
        }).startTest().join();
    }

    @Test
    public void TestEntityResolvers() {
        createDialogContext(dc -> {
            // test @ and @@
            String[] testEntities = new String[] { "entity1", "entity2" };
            String[] testEntities2 = new String[] { "entity3", "entity4" };
            dc.getState().setValue("turn.recognized.entities.test", testEntities);
            dc.getState().setValue("@@test2", testEntities2);

            Assert.assertEquals(testEntities[0],
                    dc.getState().getValue("turn.recognized.entities.test[0]", "", String.class));
            Assert.assertEquals(testEntities[0], dc.getState().getValue("@test", "", String.class));
            Assert.assertArrayEquals(testEntities,
                    dc.getState().getValue("turn.recognized.entities.test", null, String[].class));
            Assert.assertArrayEquals(testEntities, dc.getState().getValue("@@test", null, String[].class));

            Assert.assertEquals(testEntities2[0],
                    dc.getState().getValue("turn.recognized.entities.test2[0]", "", String.class));
            Assert.assertEquals(testEntities2[0], dc.getState().getValue("@test2", "", String.class));
            Assert.assertArrayEquals(testEntities2,
                    dc.getState().getValue("turn.recognized.entities.test2", null, String[].class));
            Assert.assertArrayEquals(testEntities2, dc.getState().getValue("@@test2", null, String[].class));
            return CompletableFuture.completedFuture(null);
        }).startTest().join();
    }

    @Test
    public void TestDollarScope() {
        createFlow(new D1Dialog(), null, null, false).sendConversationUpdate()
            // d1
            .assertReply("dialog")
            .assertReply("dialog")
            .assertReply("dialog2")
            .assertReply("dialog2")
            .assertReply("10")

            // d2
            .assertReply("bbb")
            .assertReply("123")
            .assertReply("20")
            .assertReply("bbb")
            .startTest().join();
    }

    @Test
    public void TestNestedContainerDialogs() {
         createFlow(new NestedContainerDialog(), null, null, false)
            .sendConversationUpdate()
            .assertReply("testDialog")
            .assertReply("testDialog")
            .assertReply("nested d1")
            .assertReply("nested d1")
            .assertReply("testDialog")
            .assertReply("testDialog")
            .assertReply("nested d2")
            .startTest()
            .join();
    }

    @Test
    public void TestExpressionSet() {
         createDialogContext(dc -> {
            dc.getState().setValue("turn.x.y.z", null);
            Assert.assertNull(dc.getState().getValue("turn.x.y.z", null, Object.class));
            return CompletableFuture.completedFuture(null);
        }).startTest();
    }

    @Test
    public void TestConversationResetOnException() {
        MemoryStorage storage = new MemoryStorage();
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);

        TestAdapter adapter = new TestAdapter()
            .useStorage(storage)
            .useBotState(userState, conversationState)
            .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        adapter.setOnTurnError((turnContext, exception) -> {
             conversationState.delete(turnContext);
             return turnContext.sendActivity(exception.getMessage()).thenApply(result -> null);
        });

        DialogManager dm = new DialogManager(new TestDialog(), null);

         new TestFlow((TestAdapter) adapter, (turnContext) -> {
            return dm.onTurn(turnContext).thenApply(result -> null);
        })
        .send("yo1")
        .assertReply("unknown")
        .send("yo2")
        .assertReply("havedata")
        .send("throw")
        .assertReply("java.lang.RuntimeException: throwing")
        .send("yo3")
        .assertReply("unknown")
        .send("yo4")
        .assertReply("havedata")
        .startTest()
        .join();
    }

    @Test
    public void TestConversationResetOnExpiration() {
        MemoryStorage storage = new MemoryStorage();
        UserState userState = new UserState(storage);
        ConversationState conversationState = new ConversationState(storage);

        TestAdapter adapter = new TestAdapter()
            .useStorage(storage)
            .useBotState(userState, conversationState);

        adapter.setOnTurnError((turnContext, exception) -> {
            conversationState.delete(turnContext);
            return turnContext.sendActivity(exception.getMessage()).thenApply(result -> null);
       });

       DialogManager dm = new DialogManager(new TestDialog(), null);
       dm.setExpireAfter(1000);

         new TestFlow((TestAdapter)adapter, (turnContext) -> {
            return dm.onTurn(turnContext).thenApply(result -> null);
        })
        .send("yo")
        .assertReply("unknown")
        .send("yo")
        .assertReply("havedata")
        .delay(1100)
        .send("yo")
        .assertReply("unknown", "Should have expired conversation and ended up with yo->unknown")
        .send("yo")
        .assertReply("havedata")
        .send("yo")
        .assertReply("havedata")
        .startTest()
        .join();
    }

    @Test
    public void TestChangeTracking() {
        createDialogContext(dc -> {

            DialogStateManager state = dc.getState();
            List<String> pathList = new ArrayList<String>();
            pathList.add("dialog.user.first");
            pathList.add("dialog.user.last");
            List<String> dialogPaths = state.trackPaths(pathList);

            state.setValue("dialog.eventCounter", 0);
            Assert.assertFalse(state.anyPathChanged(0, dialogPaths));

            state.setValue("dialog.eventCounter", 1);
            state.setValue("dialog.foo", 3);
            Assert.assertFalse(state.anyPathChanged(0, dialogPaths));

            state.setValue("dialog.eventCounter", 2);
            state.setValue("dialog.user.first", "bart");
            Assert.assertTrue(state.anyPathChanged(1, dialogPaths));

            state.setValue("dialog.eventCounter", 3);
            Map<String, Object> testMap = new HashMap<String, Object>();
            testMap.put("first", "tom");
            testMap.put("last", "starr");
            state.setValue("dialog.user", testMap);
            Assert.assertTrue(state.anyPathChanged(2, dialogPaths));

            state.setValue("dialog.eventCounter", 4);
            Assert.assertFalse(state.anyPathChanged(3, dialogPaths));
            return CompletableFuture.completedFuture(null);
        }).startTest().join();
    }

    @Test
    public void TestMemoryScope_PathResolver_Registration() {
        final String key = "testKey";
        String fullKey = String.format("test.%s", key);
        String shortKey = String.format("^^%s", key);
        final String testValue = "testValue";

         createDialogContext(dc -> {
            DialogStateManager state = dc.getState();
            state.setValue(fullKey, testValue);
            Assert.assertEquals(testValue, state.getValue(fullKey, "", String.class));
            Assert.assertEquals(testValue, state.getStringValue(shortKey, ""));
            return CompletableFuture.completedFuture(null);
        }).startTest().join();
    }

    class TestDialog extends Dialog {

        public TestDialog() {
            super("TestDialog");
        }

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
            String data = dc.getState().getValue("conversation.test", "unknown", String.class);
            dc.getContext().sendActivity(data).join();
            dc.getState().setValue("conversation.test", "havedata");
            return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.WAITING));
        }

        @Override
        public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
            switch (dc.getContext().getActivity().getText()) {
                case "throw":
                    throw new RuntimeException("throwing");
                case "end":
                    return dc.endDialog();
                default:
            }

            String data = dc.getState().getValue("conversation.test", "unknown", String.class);
            dc.getContext().sendActivity(data).join();
            return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.WAITING));
        }
    }

    public class NestedContainerDialog extends ComponentDialog implements DialogDependencies {
        public NestedContainerDialog() {
        super("NestedContainerDialog");
            addDialog(new NestedContainerDialog1());
            addDialog(new NestedContainerDialog2());
        }

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
            dc.getState().setValue("$name", "testDialog");
            String nameResult = dc.getState().getValue("$name", "", String.class);
            dc.getContext().sendActivity(nameResult).join();
            nameResult = dc.getState().getValue("dialog.name", "", String.class);
            dc.getContext().sendActivity(nameResult).join();
            return dc.beginDialog("d1");
        }

        @Override
        public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
            if (((String) result).equals("d2")) {
                return dc.endDialog();
            }

            String nameResult = dc.getState().getValue("$name", "", String.class);
            dc.getContext().sendActivity(nameResult).join();
            nameResult = dc.getState().getValue("dialog.name", "", String.class);
            dc.getContext().sendActivity(nameResult).join();
            return dc.beginDialog("d2");
        }

        @Override
        public List<Dialog> getDependencies() {
            return new ArrayList<Dialog>(getDialogs().getDialogs());
        }
    }

    public class NestedContainerDialog2 extends ComponentDialog {
        public NestedContainerDialog2() {
            super("d2");
        }

    @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext outerDc, Object options) {
            outerDc.getState().setValue("$name", "d2");
            String nameResult = outerDc.getState().getValue("$name", "", String.class);
            outerDc.getContext().sendActivity(String.format("nested %s", nameResult));
            return outerDc.endDialog(getId());
        }
    }

    public class NestedContainerDialog1 extends ComponentDialog {
        public NestedContainerDialog1() {
            super("d1");
        }

    @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
            dc.getState().setValue("$name", "d1");
            String nameResult = dc.getState().getStringValue("$name", "");
            dc.getContext().sendActivity(String.format("nested %s", nameResult)).join();
            nameResult = dc.getState().getValue("dialog.name", "", String.class);
            dc.getContext().sendActivity(String.format("nested %s", nameResult)).join();
            return dc.endDialog(getId());
        }
    }

    public class D1Dialog extends ComponentDialog implements DialogDependencies {
        public D1Dialog() {
            super("d1");
            addDialog(new D2Dialog());
        }

        public int MaxValue = 10;

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
            dc.getState().setValue("dialog.xyz", "dialog");
            dc.getContext().sendActivity(dc.getState().getValue("dialog.xyz", "", String.class)).join();
            dc.getContext().sendActivity(dc.getState().getValue("$xyz", "", String.class)).join();
            dc.getState().setValue("$aaa", "dialog2");
            dc.getContext().sendActivity(dc.getState().getValue("dialog.aaa", "", String.class)).join();
            dc.getContext().sendActivity(dc.getState().getStringValue("$aaa", "")).join();
            dc.getContext().sendActivity(dc.getState().getStringValue("%MaxValue", "")).join();
            String json = "{ \"test\" :  123 }";
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = null;
            try {
                jsonNode = objectMapper.readTree(json);
            } catch (JsonProcessingException e) {
                jsonNode = null;
            }
            return dc.beginDialog("d2", jsonNode);
        }

        @Override
        public List<Dialog> getDependencies() {
            return new ArrayList<Dialog>(getDialogs().getDialogs());
        }

        @Override
        public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {
            dc.getState().setValue("$xyz", result);
            dc.getContext().sendActivity(dc.getState().getStringValue("$xyz", "")).join();
            return dc.endDialog(result);
        }
    }

    public class D2Dialog extends Dialog {
        public D2Dialog() {
            super("d2");
        }

        public int MaxValue = 20;

        @Override
        public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
            dc.getState().setValue("dialog.options", options);
            dc.getState().setValue("$bbb", "bbb");
            dc.getContext().sendActivity(dc.getState().getValue("$bbb", "", String.class)).join();
            dc.getContext().sendActivity(dc.getState().getValue("dialog.options.test", "", String.class)).join();
            dc.getContext().sendActivity(dc.getState().getValue("%MaxValue", "", String.class)).join();
            return dc.endDialog(dc.getState().getValue("$bbb", "", String.class));
        }
    }

    private TestFlow createFlow(Dialog dialog, ConversationState convoState, UserState userState, boolean sendTrace) {
        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference(name.getMethodName(), "testuser", "testBot"), sendTrace)
                        .useStorage(new MemoryStorage()).useBotState(new UserState(new MemoryStorage()))
                        .useBotState(convoState != null ? convoState : new ConversationState(new MemoryStorage()))
                        .use(new TranscriptLoggerMiddleware(new TraceTranscriptLogger()));

        DialogManager dm = new DialogManager(dialog, null);

        return new TestFlow(adapter, (turnContext -> {
            dm.onTurn(turnContext).join();
            return CompletableFuture.completedFuture(null);
        }));
    }

    private TestFlow createDialogContext(DialogTestFunction handler) {
        TestAdapter adapter = new TestAdapter(
                TestAdapter.createConversationReference(name.getMethodName(), "User1", "Bot"))
                        .useStorage(new MemoryStorage()).useBotState(new UserState(new MemoryStorage()))
                        .useBotState(new ConversationState(new MemoryStorage()));

        DialogManager dm = new DialogManager(new LamdbaDialog(name.getMethodName(), handler), name.getMethodName());
        dm.getInitialTurnState().add(new MemoryScope[] { new TestMemoryScope() });
        dm.getInitialTurnState().add(new PathResolver[] { new DoubleCaratPathResolver() });
        return new TestFlow(adapter, (turnContext -> {
            dm.onTurn(turnContext).join();
            return CompletableFuture.completedFuture(null);
        })).sendConversationUpdate();
    }

    protected class DoubleCaratPathResolver extends AliasPathResolver {
        public DoubleCaratPathResolver() {
            super("^^", "test.", "");
        }
    }

    protected class TestMemoryScope extends MemoryScope {

        public TestMemoryScope() {
            super("test", false);
        }

        @Override
        public Object getMemory(DialogContext dc) {
            if (dc == null) {
                throw new IllegalArgumentException("dialogContext cannot be null");
            }
            Object result = dc.getContext().getTurnState().get(ScopePath.TURN);
            if (result == null) {
                result = new HashMap<String, Object>();
                dc.getContext().getTurnState().add(ScopePath.TURN, result);
            }
            return result;
        }

        @Override
        public void setMemory(DialogContext dc, Object memory) {
            if (dc == null) {
                throw new IllegalArgumentException("dialogContext cannot be null");
            }
            dc.getContext().getTurnState().add(ScopePath.TURN, memory);
        }

    }
}
