package com.microsoft.bot.schema;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Assert;
import org.junit.Test;

public class ActivityTest {
    @Test
    public void GetConversationReference() {
        Activity activity = createActivity();
        ConversationReference conversationReference = activity.getConversationReference();

        Assert.assertEquals(activity.getId(), conversationReference.getActivityId());
        Assert.assertEquals(activity.getFrom().getId(), conversationReference.getUser().getId());
        Assert.assertEquals(activity.getRecipient().getId(), conversationReference.getBot().getId());
        Assert.assertEquals(activity.getConversation().getId(), conversationReference.getConversation().getId());
        Assert.assertEquals(activity.getChannelId(), conversationReference.getChannelId());
        Assert.assertEquals(activity.getServiceUrl(), conversationReference.getServiceUrl());
    }

    @Test
    public void GetReplyConversationReference() {
        Activity activity = createActivity();

        ResourceResponse reply = new ResourceResponse() {{
            setId("1234");
        }};

        ConversationReference conversationReference = activity.getReplyConversationReference(reply);

        Assert.assertEquals(reply.getId(), conversationReference.getActivityId());
        Assert.assertEquals(activity.getFrom().getId(), conversationReference.getUser().getId());
        Assert.assertEquals(activity.getRecipient().getId(), conversationReference.getBot().getId());
        Assert.assertEquals(activity.getConversation().getId(), conversationReference.getConversation().getId());
        Assert.assertEquals(activity.getChannelId(), conversationReference.getChannelId());
        Assert.assertEquals(activity.getServiceUrl(), conversationReference.getServiceUrl());
    }

    @Test
    public void ApplyConversationReference_isIncoming() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference() {{
            setChannelId("cr_123");
            setServiceUrl("cr_serviceUrl");
            setConversation(new ConversationAccount(){{
                setId("cr_456");
            }});
            setUser(new ChannelAccount() {{
                setId("cr_abc");
            }});
            setBot(new ChannelAccount() {{
                setId("cr_def");
            }});
            setActivityId("cr_12345");
        }};

        activity.applyConversationReference(conversationReference, true);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getUser().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getBot().getId(), activity.getRecipient().getId());
        Assert.assertEquals(conversationReference.getActivityId(), activity.getId());
    }

    @Test
    public void ApplyConversationReference() {
        Activity activity = createActivity();

        ConversationReference conversationReference = new ConversationReference() {{
            setChannelId("123");
            setServiceUrl("serviceUrl");
            setConversation(new ConversationAccount(){{
                setId("456");
            }});
            setUser(new ChannelAccount() {{
                setId("abc");
            }});
            setBot(new ChannelAccount() {{
                setId("def");
            }});
            setActivityId("12345");
        }};

        activity.applyConversationReference(conversationReference, false);

        Assert.assertEquals(conversationReference.getChannelId(), activity.getChannelId());
        Assert.assertEquals(conversationReference.getServiceUrl(), activity.getServiceUrl());
        Assert.assertEquals(conversationReference.getConversation().getId(), activity.getConversation().getId());

        Assert.assertEquals(conversationReference.getBot().getId(), activity.getFrom().getId());
        Assert.assertEquals(conversationReference.getUser().getId(), activity.getRecipient().getId());
        Assert.assertEquals(conversationReference.getActivityId(), activity.getReplyToId());
    }

    @Test
    public void CreateTraceAllowsNullRecipient() {
        Activity activity = createActivity();
        activity.setRecipient(null);
        Activity trace = activity.createTrace("test");

        Assert.assertNull(trace.getFrom().getId());
    }

    private Activity createActivity() {
        ChannelAccount account1 = new ChannelAccount() {{
           setId("ChannelAccount_Id_1");
           setName("ChannelAccount_Name_1");
           setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
           setRole(RoleTypes.USER);
        }};

        ChannelAccount account2 = new ChannelAccount() {{
            setId("ChannelAccount_Id_2");
            setName("ChannelAccount_Name_2");
            setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
            setRole(RoleTypes.USER);
        }};

        ConversationAccount conversationAccount = new ConversationAccount() {{
            setConversationType("a");
            setId("123");
            setIsGroup(true);
            setName("Name");
            setProperties("Name", JsonNodeFactory.instance.objectNode().put("Name", "Value"));
        }};

        Activity activity = new Activity() {{
            setId("123");
            setFrom(account1);
            setRecipient(account2);
            setConversation(conversationAccount);
            setChannelId("ChannelId123");
            setServiceUrl("ServiceUrl123");
        }};

        return activity;
    }
}
