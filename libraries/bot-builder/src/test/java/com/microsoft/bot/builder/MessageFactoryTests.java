// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.builder.adapters.TestAdapter;
import com.microsoft.bot.builder.adapters.TestFlow;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.AttachmentLayoutTypes;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.InputHints;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MessageFactoryTests {
    @Test
    public void NullText() {
        Activity message = MessageFactory.text(null);
        Assert.assertNull(
            "Message Text is not null. Null must have been passed through.",
            message.getText()
        );
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
    }

    @Test
    public void TextOnly() {
        String messageText = UUID.randomUUID().toString();
        Activity message = MessageFactory.text(messageText);
        Assert.assertEquals("Message Text does not match", messageText, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
    }

    @Test
    public void TextAndSSML() {
        String messageText = UUID.randomUUID().toString();
        String ssml = "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" version=1.0\"><p><s xml:lang=en-US\"><voice name=Bot gender=neutral age=2>Bots are <emphasis>Awesome</emphasis>.</voice></s></p></speak>";
        Activity message = MessageFactory.text(messageText, ssml, null);
        Assert.assertEquals("Message Text is not an empty String", messageText, message.getText());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertEquals(
            "InputHint is not AcceptingInput",
            InputHints.ACCEPTING_INPUT,
            message.getInputHint()
        );
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
    }

    @Test
    public void SuggestedActionText() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;
        List<String> textActions = Arrays.asList("one", "two");

        Activity message = MessageFactory.suggestedActions(textActions, text, ssml, inputHint);
        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertNotNull(message.getSuggestedActions());
        Assert.assertNotNull(message.getSuggestedActions().getActions());
        Assert.assertTrue(message.getSuggestedActions().getActions().size() == 2);
        Assert.assertEquals("one", message.getSuggestedActions().getActions().get(0).getValue());
        Assert.assertEquals("one", message.getSuggestedActions().getActions().get(0).getTitle());
        Assert.assertEquals(
            message.getSuggestedActions().getActions().get(0).getType(),
            ActionTypes.IM_BACK
        );
        Assert.assertEquals("two", message.getSuggestedActions().getActions().get(1).getValue());
        Assert.assertEquals("two", message.getSuggestedActions().getActions().get(1).getTitle());
        Assert.assertTrue(
            message.getSuggestedActions().getActions().get(1).getType() == ActionTypes.IM_BACK
        );
    }

    @Test
    public void SuggestedActionEnumerable() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;
        Set<String> textActions = new HashSet<>(Arrays.asList("one", "two", "three"));

        Activity message = MessageFactory.suggestedActions(
            new ArrayList<>(textActions),
            text,
            ssml,
            inputHint
        );
        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertNotNull(message.getSuggestedActions());
        Assert.assertNotNull(message.getSuggestedActions().getActions());
        Assert.assertTrue(
            "The message's suggested actions have the wrong set of values.",
            textActions.containsAll(
                message.getSuggestedActions().getActions().stream().map(CardAction::getValue).collect(Collectors.toList())
            )
        );
        Assert.assertTrue(
            "The message's suggested actions have the wrong set of titles.",
            textActions.containsAll(
                message.getSuggestedActions().getActions().stream().map(CardAction::getTitle).collect(Collectors.toList())
            )
        );
        Assert.assertTrue(
            "The message's suggested actions are of the wrong action type.",
            message.getSuggestedActions().getActions().stream().allMatch(
                action -> action.getType() == ActionTypes.IM_BACK
            )
        );
    }

    @Test
    public void SuggestedActionCardAction() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;

        String cardActionValue = UUID.randomUUID().toString();
        String cardActionTitle = UUID.randomUUID().toString();

        CardAction ca = new CardAction();
        ca.setType(ActionTypes.IM_BACK);
        ca.setValue(cardActionValue);
        ca.setTitle(cardActionTitle);

        List<CardAction> cardActions = Collections.singletonList(ca);

        Activity message = MessageFactory.suggestedCardActions(cardActions, text, ssml, inputHint);

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertNotNull(message.getSuggestedActions());
        Assert.assertNotNull(message.getSuggestedActions().getActions());
        Assert.assertTrue(message.getSuggestedActions().getActions().size() == 1);
        Assert.assertEquals(
            cardActionValue,
            message.getSuggestedActions().getActions().get(0).getValue()
        );
        Assert.assertEquals(
            cardActionTitle,
            message.getSuggestedActions().getActions().get(0).getTitle()
        );
        Assert.assertTrue(
            message.getSuggestedActions().getActions().get(0).getType() == ActionTypes.IM_BACK
        );
    }

    @Test
    public void SuggestedActionCardActionUnordered() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;

        String cardValue1 = UUID.randomUUID().toString();
        String cardTitle1 = UUID.randomUUID().toString();

        CardAction cardAction1 = new CardAction();
        cardAction1.setType(ActionTypes.IM_BACK);
        cardAction1.setValue(cardValue1);
        cardAction1.setTitle(cardTitle1);

        String cardValue2 = UUID.randomUUID().toString();
        String cardTitle2 = UUID.randomUUID().toString();

        CardAction cardAction2 = new CardAction();
        cardAction2.setType(ActionTypes.IM_BACK);
        cardAction2.setValue(cardValue2);
        cardAction2.setTitle(cardTitle2);

        List<CardAction> cardActions = Arrays.asList(cardAction1, cardAction2);
        Set<String> values = new HashSet<>(Arrays.asList(cardValue1, cardValue2));
        Set<String> titles = new HashSet<>(Arrays.asList(cardTitle1, cardTitle2));

        Activity message = MessageFactory.suggestedCardActions(cardActions, text, ssml, inputHint);

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertNotNull(message.getSuggestedActions());
        Assert.assertNotNull(message.getSuggestedActions().getActions());
        Assert.assertTrue(message.getSuggestedActions().getActions().size() == 2);
        Assert.assertTrue(
            "The message's suggested actions have the wrong set of values.",
            values.containsAll(
                message.getSuggestedActions().getActions().stream().map(CardAction::getValue).collect(Collectors.toList())
            )
        );
        Assert.assertTrue(
            "The message's suggested actions have the wrong set of titles.",
            titles.containsAll(
                message.getSuggestedActions().getActions().stream().map(CardAction::getTitle).collect(Collectors.toList())
            )
        );
        Assert.assertTrue(
            "The message's suggested actions are of the wrong action type.",
            message.getSuggestedActions().getActions().stream().allMatch(
                action -> action.getType() == ActionTypes.IM_BACK
            )
        );

    }

    @Test
    public void AttachmentSingle() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;

        String attachmentName = UUID.randomUUID().toString();
        Attachment a = new Attachment();
        a.setName(attachmentName);

        Activity message = MessageFactory.attachment(a, text, ssml, inputHint);

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertTrue("Incorrect Attachment Count", message.getAttachments().size() == 1);
        Assert.assertEquals(
            "Incorrect Attachment Name",
            message.getAttachments().get(0).getName(),
            attachmentName
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void AttachmentNull() {
        Activity message = MessageFactory.attachment(null, null);
        Assert.fail("Exception not thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void AttachmentMultipleNull() {
        Activity message = MessageFactory.attachment((List<Attachment>) null, null, null, null);
        Assert.fail("Exception not thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void CarouselNull() {
        Activity message = MessageFactory.carousel(null, null);
        Assert.fail("Exception not thrown");
    }

    @Test
    public void CarouselTwoAttachments() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;

        String attachmentName = UUID.randomUUID().toString();
        Attachment attachment1 = new Attachment();
        attachment1.setName(attachmentName);

        String attachmentName2 = UUID.randomUUID().toString();
        Attachment attachment2 = new Attachment();
        attachment2.setName(attachmentName2);

        List<Attachment> multipleAttachments = Arrays.asList(attachment1, attachment2);
        Activity message = MessageFactory.carousel(multipleAttachments, text, ssml, inputHint);

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertTrue(message.getAttachmentLayout() == AttachmentLayoutTypes.CAROUSEL);
        Assert.assertTrue("Incorrect Attachment Count", message.getAttachments().size() == 2);
        Assert.assertEquals(
            "Incorrect Attachment1 Name",
            message.getAttachments().get(0).getName(),
            attachmentName
        );
        Assert.assertEquals(
            "Incorrect Attachment2 Name",
            message.getAttachments().get(1).getName(),
            attachmentName2
        );
    }

    @Test
    public void CarouselUnorderedAttachments() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;

        String attachmentName1 = UUID.randomUUID().toString();
        Attachment attachment1 = new Attachment();
        attachment1.setName(attachmentName1);

        String attachmentName2 = UUID.randomUUID().toString();
        Attachment attachment2 = new Attachment();
        attachment2.setName(attachmentName2);

        Set<Attachment> multipleAttachments = new HashSet<>(
            Arrays.asList(attachment1, attachment2)
        );
        Activity message = MessageFactory.carousel(
            new ArrayList<>(multipleAttachments),
            text,
            ssml,
            inputHint
        );

        Set<String> names = new HashSet<>(Arrays.asList(attachmentName1, attachmentName2));

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertTrue(message.getAttachmentLayout() == AttachmentLayoutTypes.CAROUSEL);
        Assert.assertTrue("Incorrect Attachment Count", message.getAttachments().size() == 2);
        Assert.assertTrue(
            "Incorrect set of attachment names.",
            names.containsAll(message.getAttachments().stream().map(Attachment::getName).collect(Collectors.toList()))
        );
    }

    @Test
    public void AttachmentMultiple() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;

        String attachmentName = UUID.randomUUID().toString();
        Attachment a = new Attachment();
        a.setName(attachmentName);

        String attachmentName2 = UUID.randomUUID().toString();
        Attachment a2 = new Attachment();
        a2.setName(attachmentName2);

        List<Attachment> multipleAttachments = Arrays.asList(a, a2);
        Activity message = MessageFactory.attachment(multipleAttachments, text, ssml, inputHint);

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertTrue(message.getAttachmentLayout() == AttachmentLayoutTypes.LIST);
        Assert.assertTrue("Incorrect Attachment Count", message.getAttachments().size() == 2);
        Assert.assertEquals(
            "Incorrect Attachment1 Name",
            message.getAttachments().get(0).getName(),
            attachmentName
        );
        Assert.assertEquals(
            "Incorrect Attachment2 Name",
            message.getAttachments().get(1).getName(),
            attachmentName2
        );
    }

    @Test
    public void AttachmentMultipleUnordered() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;

        String attachmentName1 = UUID.randomUUID().toString();
        Attachment attachment1 = new Attachment();
        attachment1.setName(attachmentName1);

        String attachmentName2 = UUID.randomUUID().toString();
        Attachment attachment2 = new Attachment();
        attachment2.setName(attachmentName2);

        Set<Attachment> multipleAttachments = new HashSet<>(
            Arrays.asList(attachment1, attachment2)
        );
        Activity message = MessageFactory.attachment(
            new ArrayList<>(multipleAttachments),
            text,
            ssml,
            inputHint
        );

        Set<String> names = new HashSet<>(Arrays.asList(attachmentName1, attachmentName2));

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertSame(message.getAttachmentLayout(), AttachmentLayoutTypes.LIST);
        Assert.assertEquals("Incorrect Attachment Count", 2, message.getAttachments().size());
        Assert.assertTrue(
            "Incorrect set of attachment names.",
            names.containsAll(message.getAttachments().stream().map(Attachment::getName).collect(Collectors.toList()))
        );
    }

    @Test
    public void ContentUrl() {
        String text = UUID.randomUUID().toString();
        String ssml = UUID.randomUUID().toString();
        InputHints inputHint = InputHints.EXPECTING_INPUT;
        String uri = "https://" + UUID.randomUUID().toString();
        String contentType = "image/jpeg";
        String name = UUID.randomUUID().toString();

        Activity message = MessageFactory.contentUrl(uri, contentType, name, text, ssml, inputHint);

        Assert.assertEquals("Message Text does not match", text, message.getText());
        Assert.assertEquals("Incorrect Activity Type", ActivityTypes.MESSAGE, message.getType());
        Assert.assertEquals("InputHint does not match", inputHint, message.getInputHint());
        Assert.assertEquals("ssml text is incorrect", ssml, message.getSpeak());
        Assert.assertEquals(1, message.getAttachments().size());
        Assert.assertEquals(
            "Incorrect Attachment1 Name",
            message.getAttachments().get(0).getName(),
            name
        );
        Assert.assertSame(
            "Incorrect contentType",
            message.getAttachments().get(0).getContentType(),
            contentType
        );
        Assert.assertEquals("Incorrect Uri", message.getAttachments().get(0).getContentUrl(), uri);
    }

    @Test
    public void ValidateIMBackWithText() {
        TestAdapter adapter = new TestAdapter();

        BotCallbackHandler replyWithimBackBack = turnContext -> {
            if (StringUtils.equals(turnContext.getActivity().getText(), "test")) {
                CardAction card = new CardAction();
                card.setType(ActionTypes.IM_BACK);
                card.setText("red");
                card.setTitle("redTitle");
                Activity activity = MessageFactory.suggestedCardActions(
                    Collections.singletonList(card),
                    "Select color"
                );

                turnContext.sendActivity(activity).join();
            }
            return CompletableFuture.completedFuture(null);
        };

        Consumer<Activity> validateIMBack = activity -> {
            Assert.assertTrue(activity.isType(ActivityTypes.MESSAGE));
            Assert.assertEquals("Select color", activity.getText());
            Assert.assertEquals(
                "Incorrect Count",
                1,
                activity.getSuggestedActions().getActions().size()
            );
            Assert.assertSame(
                "Incorrect Action Type",
                activity.getSuggestedActions().getActions().get(0).getType(),
                ActionTypes.IM_BACK
            );
            Assert.assertEquals(
                "incorrect text",
                activity.getSuggestedActions().getActions().get(0).getText(),
                "red"
            );
            Assert.assertEquals(
                "incorrect text",
                activity.getSuggestedActions().getActions().get(0).getTitle(),
                "redTitle"
            );
        };

        new TestFlow(adapter, replyWithimBackBack).send("test").assertReply(
            validateIMBack,
            "IMBack Did not validate"
        ).startTest().join();
    }

    @Test
    public void ValidateIMBackWithNoTest() {
        TestAdapter adapter = new TestAdapter();

        BotCallbackHandler replyWithimBackBack = turnContext -> {
            if (StringUtils.equals(turnContext.getActivity().getText(), "test")) {
                CardAction card = new CardAction();
                card.setType(ActionTypes.IM_BACK);
                card.setText("red");
                card.setTitle("redTitle");
                Activity activity = MessageFactory.suggestedCardActions(
                    Collections.singletonList(card),
                    null
                );

                turnContext.sendActivity(activity).join();
            }
            return CompletableFuture.completedFuture(null);
        };

        Consumer<Activity> validateIMBack = activity -> {
            Assert.assertTrue(activity.isType(ActivityTypes.MESSAGE));
            Assert.assertNull(activity.getText());
            Assert.assertEquals(
                "Incorrect Count",
                1,
                activity.getSuggestedActions().getActions().size()
            );
            Assert.assertSame(
                "Incorrect Action Type",
                activity.getSuggestedActions().getActions().get(0).getType(),
                ActionTypes.IM_BACK
            );
            Assert.assertEquals(
                "incorrect text",
                activity.getSuggestedActions().getActions().get(0).getText(),
                "red"
            );
            Assert.assertEquals(
                "incorrect text",
                activity.getSuggestedActions().getActions().get(0).getTitle(),
                "redTitle"
            );
        };

        new TestFlow(adapter, replyWithimBackBack).send("test").assertReply(
            validateIMBack,
            "IMBack Did not validate"
        ).startTest().join();
    }
}
