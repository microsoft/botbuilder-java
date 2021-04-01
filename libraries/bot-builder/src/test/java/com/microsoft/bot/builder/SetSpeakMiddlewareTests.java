// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.ConversationReference;

import org.junit.Assert;
import org.junit.Test;

public class SetSpeakMiddlewareTests {

    @Test
    public void NoFallback() {
        TestAdapter adapter = new TestAdapter(createConversation("NoFallback"))
                .use(new SetSpeakMiddleware("male", false));

        new TestFlow(adapter, turnContext -> {
            Activity activity = MessageFactory.text("OK");

            return turnContext.sendActivity(activity).thenApply(result -> null);
        }).send("foo").assertReply(obj -> {
            Activity activity = (Activity) obj;
            Assert.assertNull(activity.getSpeak());
        }).startTest().join();
    }

    // fallback instanceof true, for any ChannelId other than emulator,
    // directlinespeech, or telephony should
    // just set Activity.Speak to Activity.Text if Speak instanceof empty.
    @Test
    public void FallbackNullSpeak() {
        TestAdapter adapter = new TestAdapter(createConversation("NoFallback"))
                .use(new SetSpeakMiddleware("male", true));

        new TestFlow(adapter, turnContext -> {
            Activity activity = MessageFactory.text("OK");

            return turnContext.sendActivity(activity).thenApply(result -> null);
        }).send("foo").assertReply(obj -> {
            Activity activity = (Activity) obj;
            Assert.assertEquals(activity.getText(), activity.getSpeak());
        }).startTest().join();
    }

    // fallback instanceof true, for any ChannelId other than emulator,
    // directlinespeech, or telephony should
    // leave a non-empty Speak unchanged.
    @Test
    public void FallbackWithSpeak() {
        TestAdapter adapter = new TestAdapter(createConversation("Fallback"))
                .use(new SetSpeakMiddleware("male", true));

        new TestFlow(adapter, turnContext -> {
            Activity activity = MessageFactory.text("OK");
            activity.setSpeak("speak value");

            return turnContext.sendActivity(activity).thenApply(result -> null);
        }).send("foo").assertReply(obj -> {
            Activity activity = (Activity) obj;
            Assert.assertEquals("speak value", activity.getSpeak());
        }).startTest().join();
    }

    @Test
    public void AddVoiceEmulator() {
        AddVoice(Channels.EMULATOR);
    }

    @Test
    public void AddVoiceDirectlineSpeech() {
        AddVoice(Channels.DIRECTLINESPEECH);
    }

    @Test
    public void AddVoiceTelephony() {
        AddVoice("telephony");
    }


    // Voice instanceof added to Speak property.
    public void AddVoice(String channelId) {
        TestAdapter adapter = new TestAdapter(createConversation("Fallback", channelId))
                .use(new SetSpeakMiddleware("male", true));

        new TestFlow(adapter, turnContext -> {
            Activity activity = MessageFactory.text("OK");

            return turnContext.sendActivity(activity).thenApply(result -> null);
        }).send("foo").assertReply(obj -> {
            Activity activity = (Activity) obj;
            Assert.assertEquals("<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' "
                                + "xml:lang='en-us'><voice name='male'>OK</voice></speak>",
                                activity.getSpeak());
        }).startTest().join();
    }

    @Test
    public void AddNoVoiceEmulator() {
        AddNoVoice(Channels.EMULATOR);
    }

    @Test
    public void AddNoVoiceDirectlineSpeech() {
        AddNoVoice(Channels.DIRECTLINESPEECH);
    }

    @Test
    public void AddNoVoiceTelephony() {
        AddNoVoice(Channels.TELEPHONY);
    }


    // With no 'voice' specified, the Speak property instanceof unchanged.
    public void AddNoVoice(String channelId) {
        TestAdapter adapter = new TestAdapter(createConversation("Fallback", channelId))
                .use(new SetSpeakMiddleware(null, true));

        new TestFlow(adapter, turnContext -> {
            Activity activity = MessageFactory.text("OK");

            return turnContext.sendActivity(activity).thenApply(result -> null);
        }).send("foo").assertReply(obj -> {
            Activity activity = (Activity) obj;
            Assert.assertEquals("OK",
                                activity.getSpeak());
        }).startTest().join();
    }


    private static ConversationReference createConversation(String name) {
        return createConversation(name, "User1", "Bot", "test");
    }

    private static ConversationReference createConversation(String name, String channelId) {
        return createConversation(name, "User1", "Bot", channelId);
    }

    private static ConversationReference createConversation(String name, String user, String bot, String channelId) {
        ConversationReference conversationReference = new ConversationReference();
        conversationReference.setChannelId(channelId);
        conversationReference.setServiceUrl("https://test.com");
        conversationReference.setConversation(new ConversationAccount(false, name, name));
        conversationReference.setUser(new ChannelAccount(user.toLowerCase(), user));
        conversationReference.setBot(new ChannelAccount(bot.toLowerCase(), bot));
        conversationReference.setLocale("en-us");
        return conversationReference;
    }
}
