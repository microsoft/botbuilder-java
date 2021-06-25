// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the HeroCard methods work as expected.
 */
public class SerializationTest {

    @Test
    public void testGetAs() {
        Activity activity = createActivity();
        JsonNode activityNode = Serialization.objectToTree(activity);
        Activity resultActivity = Serialization.getAs(activityNode, Activity.class);
        Assert.assertEquals(activity.getId(), resultActivity.getId());
        Assert.assertEquals(activity.getFrom().getId(), resultActivity.getFrom().getId());
        Assert.assertEquals(activity.getConversation().getId(), resultActivity.getConversation().getId());
    }

    @Test
    public void testGetAsNull() {
        Activity resultActivity = Serialization.getAs(null, Activity.class);
        Assert.assertNull(resultActivity);
    }

    @Test
    public void testClone() {
        Activity activity = createActivity();
        Activity resultActivity = (Activity) Serialization.clone((Object) activity);
        Assert.assertEquals(activity.getId(), resultActivity.getId());
        Assert.assertEquals(activity.getFrom().getId(), resultActivity.getFrom().getId());
        Assert.assertEquals(activity.getConversation().getId(), resultActivity.getConversation().getId());
    }

    @Test
    public void testCloneNull() {
        Activity resultActivity = (Activity) Serialization.clone((Object) null);
        Assert.assertNull(resultActivity);
    }

    @Test
    public void testTreeToValue() {
        Activity activity = createActivity();
        JsonNode activityNode = Serialization.objectToTree(activity);
        Activity resultActivity = Serialization.treeToValue(activityNode, Activity.class);
        Assert.assertEquals(activity.getId(), resultActivity.getId());
        Assert.assertEquals(activity.getFrom().getId(), resultActivity.getFrom().getId());
        Assert.assertEquals(activity.getConversation().getId(), resultActivity.getConversation().getId());
    }

    @Test
    public void testFutureGetAs() {
        Activity activity = createActivity();
        JsonNode activityNode = Serialization.objectToTree(activity);
        Activity resultActivity = Serialization.futureGetAs(activityNode, Activity.class).join();
        Assert.assertEquals(activity.getId(), resultActivity.getId());
        Assert.assertEquals(activity.getFrom().getId(), resultActivity.getFrom().getId());
        Assert.assertEquals(activity.getConversation().getId(), resultActivity.getConversation().getId());
    }

    @Test
    public void testToString() throws IOException {
        Activity activity = createActivity();
        JsonNode activityNode = Serialization.objectToTree(activity);
        String resultString = Serialization.toString(activityNode);
        JsonNode jsonResult = Serialization.jsonToTree(resultString);
        Activity resultActivity = Serialization.treeToValue(jsonResult, Activity.class);
        Assert.assertEquals(activity.getId(), resultActivity.getId());
        Assert.assertEquals(activity.getFrom().getId(), resultActivity.getFrom().getId());
        Assert.assertEquals(activity.getConversation().getId(), resultActivity.getConversation().getId());
    }

    @Test
    public void testToStringSilent() throws IOException {
        Activity activity = createActivity();
        JsonNode activityNode = Serialization.objectToTree(activity);
        String resultString = Serialization.toStringSilent(activityNode);
        JsonNode jsonResult = Serialization.jsonToTree(resultString);
        Activity resultActivity = Serialization.treeToValue(jsonResult, Activity.class);
        Assert.assertEquals(activity.getId(), resultActivity.getId());
        Assert.assertEquals(activity.getFrom().getId(), resultActivity.getFrom().getId());
        Assert.assertEquals(activity.getConversation().getId(), resultActivity.getConversation().getId());
    }

    @Test
    public void testAsNodeString() {
        String testValue = "Hello world!";
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertEquals(node.asText(), testValue);
    }

    @Test
    public void testAsNodeInt() {
        int testValue = 42;
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertEquals(node.asInt(), testValue);
    }

    @Test
    public void testAsNodeLong() {
        long testValue = 42;
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertEquals(node.asLong(), testValue);
    }

    @Test
    public void testAsNodeFloat() {
        float testValue = 42.42f;
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertTrue(node.asDouble() == testValue);
    }

    @Test
    public void testAsNodeDouble() {
        double testValue = 42.42;
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertTrue(node.asDouble() == testValue);
    }

    @Test
    public void testAsNodeShort() {
        short testValue = 42;
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertEquals(node.asInt(), testValue);
    }

    @Test
    public void testAsNodeByte() {
        byte testValue = 42;
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertEquals(node.asInt(), testValue);
    }

    @Test
    public void testAsNodeBoolean() {
        boolean testValue = true;
        JsonNode node = Serialization.asNode(testValue);
        Assert.assertEquals(node.asBoolean(), testValue);
    }

    @Test
    public void testCreateObjectNode() {
        ObjectNode node = Serialization.createObjectNode();
        Assert.assertTrue(node instanceof ObjectNode);
    }

    @Test
    public void testCreateArraytNode() {
        ArrayNode node = Serialization.createArrayNode();
        Assert.assertTrue(node instanceof ArrayNode);
    }

    @Test
    public void testConvert() {
        String stringValue = "42";
        Integer result = Serialization.convert(stringValue, Integer.class);
        Assert.assertTrue(result == 42);
        Integer integerValue = 42;
        String stringResult = Serialization.convert(integerValue, String.class);
        Assert.assertTrue(stringResult.equals("42"));
    }


    private Activity createActivity() {
        ChannelAccount account1 = new ChannelAccount();
        account1.setId("ChannelAccount_Id_1");
        account1.setName("ChannelAccount_Name_1");
        account1.setProperties("TestName", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
        account1.setRole(RoleTypes.USER);

        ChannelAccount account2 = new ChannelAccount();
        account2.setId("ChannelAccount_Id_2");
        account2.setName("ChannelAccount_Name_2");
        account2.setProperties("TestName", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
        account2.setRole(RoleTypes.USER);

        ConversationAccount conversationAccount = new ConversationAccount();
        conversationAccount.setConversationType("a");
        conversationAccount.setId("123");
        conversationAccount.setIsGroup(true);
        conversationAccount.setName("Name");
        conversationAccount.setProperties("TestName", JsonNodeFactory.instance.objectNode().put("Name", "Value"));

        Activity activity = new Activity();
        activity.setId("123");
        activity.setFrom(account1);
        activity.setRecipient(account2);
        activity.setConversation(conversationAccount);
        activity.setChannelId("ChannelId123");
        // Intentionally oddly-cased to check that it isn't defaulted somewhere, but
        // tests stay in English
        activity.setLocale("en-uS");
        activity.setServiceUrl("ServiceUrl123");

        return activity;
    }
}
