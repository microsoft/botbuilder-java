// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.AttachmentLayoutTypes;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.bot.schema.SuggestedActions;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains utility methods for various message types a bot can return.
 * <p>
 * Create and send a message. <code>
 * Activity message = MessageFactory.text("Hello World");
 * conext.sendActivity(message);
 * </code>
 *
 * <p>
 * The following apply to message actions in general. See the channel's
 * documentation for limits imposed upon the contents of the text of the message
 * to send.
 * </p>
 *
 * <p>
 * To control various characteristics of your bot's speech such as voice, rate,
 * volume, pronunciation, and pitch, specify test to speak in Speech Synthesis
 * Markup Language (SSML) format.
 * </p>
 *
 * <p>
 * Channels decide how each card action manifests in their user experience. In
 * most cases, the cards are clickable. In others, they may be selected by
 * speech input. In cases where the channel does not offer an interactive
 * activation experience (e.g., when interacting over SMS), the channel may not
 * support activation whatsoever. The decision about how to render actions is
 * controlled by normative requirements elsewhere in this document (e.g. within
 * the card format, or within the suggested actions definition).
 * </p>
 */
public final class MessageFactory {
    private MessageFactory() {

    }

    /**
     * Returns a simple text message.
     *
     * @param text The text of the message to send.
     * @return A message activity containing the text.
     */
    public static Activity text(String text) {
        return text(text, null, null);
    }

    /**
     * Returns a simple text message.
     *
     * @param text      The text of the message to send.
     * @param ssml      Optional, text to be spoken by your bot on a speech-enabled
     *                  channel.
     * @param inputHint Optional, indicates whether your bot is accepting,
     *                  expecting, or ignoring user input after the message is
     *                  delivered to the client. Default is
     *                  {@link InputHints#ACCEPTING_INPUT}.
     * @return A message activity containing the text.
     */
    public static Activity text(String text, String ssml, InputHints inputHint) {
        Activity activity = Activity.createMessageActivity();
        setTextAndSpeech(activity, text, ssml, inputHint);
        return activity;
    }

    /**
     * Returns a message that includes a set of suggested actions and optional text.
     *
     * <code>
     * // Create the activity and add suggested actions.
     * Activity  activity = MessageFactory.suggestedActions(
     * new String[] { "red", "green", "blue" },
     * "Choose a color");
     * <p>
     * // Send the activity as a reply to the user.
     * context.sendActivity(activity);
     * </code>
     *
     * @param actions The text of the actions to create.
     * @param text    Optional. The text of the message to send.
     * @return A message activity containing the suggested actions.
     */
    public static Activity suggestedActions(List<String> actions, String text) {
        return suggestedActions(actions, text, null, null);
    }

    /**
     * Returns a message that includes a set of suggested actions and optional text.
     *
     * <code>
     * // Create the activity and add suggested actions.
     * Activity  activity = MessageFactory.suggestedActions(
     * new String[] { "red", "green", "blue" },
     * "Choose a color");
     * <p>
     * // Send the activity as a reply to the user.
     * context.sendActivity(activity);
     * </code>
     *
     * @param actions   The text of the actions to create.
     * @param text      Optional. The text of the message to send.
     * @param ssml      Optional, text to be spoken by your bot on a speech-enable
     *                  channel.
     * @param inputHint Optional, indicates whether your bot is accepting,
     *                  expecting, or ignoring user input after the message is
     *                  delivered to the client. Default is
     *                  {@link InputHints#ACCEPTING_INPUT}.
     * @return A message activity containing the suggested actions.
     */
    public static Activity suggestedActions(
        List<String> actions,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }

        List<CardAction> cardActions = new ArrayList<>();
        for (String action : actions) {
            CardAction cardAction = new CardAction();
            cardAction.setType(ActionTypes.IM_BACK);
            cardAction.setValue(action);
            cardAction.setTitle(action);
            cardActions.add(cardAction);
        }

        return suggestedCardActions(cardActions, text, ssml, inputHint);
    }

    /**
     * Returns a message that includes a set of suggested actions and optional text.
     *
     * @param actions The card actions to include.
     * @param text    Optional, the text of the message to send.
     * @return A message activity that contains the suggested actions.
     */
    public static Activity suggestedCardActions(List<CardAction> actions, String text) {
        return suggestedCardActions(actions, text, null, null);
    }

    /**
     * Returns a message that includes a set of suggested actions and optional text.
     *
     * @param actions   The card actions to include.
     * @param text      Optional, the text of the message to send.
     * @param ssml      Optional, text to be spoken by your bot on a speech-enable
     *                  channel.
     * @param inputHint Optional, indicates whether your bot is accepting,
     *                  expecting, or ignoring user input after the message is
     *                  delivered to the client. Default is
     *                  {@link InputHints#ACCEPTING_INPUT}.
     * @return A message activity that contains the suggested actions.
     */
    public static Activity suggestedCardActions(
        List<CardAction> actions,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }

        Activity activity = Activity.createMessageActivity();
        setTextAndSpeech(activity, text, ssml, inputHint);

        activity.setSuggestedActions(new SuggestedActions(actions.toArray(new CardAction[0])));

        return activity;
    }

    /**
     * Returns a message activity that contains an attachment.
     *
     * @param attachment Attachment to include in the message.
     * @return A message activity containing the attachment.
     */
    public static Activity attachment(Attachment attachment) {
        return attachment(attachment, null, null, null);
    }

    /**
     * Returns a message activity that contains an attachment.
     *
     * @param attachments Attachments to include in the message.
     * @return A message activity containing the attachment.
     */
    public static Activity attachment(List<Attachment> attachments) {
        return attachment(attachments, null, null, null);
    }

    /**
     * Returns a message activity that contains an attachment.
     *
     * @param attachment Attachment to include in the message.
     * @param text       Optional, the text of the message to send.
     * @return A message activity containing the attachment.
     */
    public static Activity attachment(Attachment attachment, String text) {
        return attachment(attachment, text, null, null);
    }

    /**
     * Returns a message activity that contains an attachment.
     *
     * @param attachment Attachment to include in the message.
     * @param text       Optional, the text of the message to send.
     * @param ssml       Optional, text to be spoken by your bot on a speech-enable
     *                   channel.
     * @param inputHint  Optional, indicates whether your bot is accepting,
     *                   expecting, or ignoring user input after the message is
     *                   delivered to the client. Default is
     *                   {@link InputHints#ACCEPTING_INPUT}.
     * @return A message activity containing the attachment.
     */
    public static Activity attachment(
        Attachment attachment,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        if (attachment == null) {
            throw new IllegalArgumentException("attachment cannot be null");
        }

        return attachment(Collections.singletonList(attachment), text, ssml, inputHint);
    }

    /**
     * Returns a message activity that contains an attachment.
     *
     * @param attachments Attachments to include in the message.
     * @param text        Optional, the text of the message to send.
     * @param ssml        Optional, text to be spoken by your bot on a speech-enable
     *                    channel.
     * @param inputHint   Optional, indicates whether your bot is accepting,
     *                    expecting, or ignoring user input after the message is
     *                    delivered to the client. Default is
     *                    {@link InputHints#ACCEPTING_INPUT}.
     * @return A message activity containing the attachment.
     */
    public static Activity attachment(
        List<Attachment> attachments,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        if (attachments == null) {
            throw new IllegalArgumentException("attachments cannot be null");
        }

        return attachmentActivity(AttachmentLayoutTypes.LIST, attachments, text, ssml, inputHint);
    }

    /**
     * Returns a message activity that contains a collection of attachments, in a
     * list.
     *
     * @param attachments Attachments to include in the message.
     * @param text        Optional, the text of the message to send.
     * @return A message activity containing the attachment.
     */
    public static Activity carousel(List<Attachment> attachments, String text) {
        return carousel(attachments, text, null, null);
    }

    /**
     * Returns a message activity that contains a collection of attachments, in a
     * list.
     *
     * @param attachments Attachments to include in the message.
     * @param text        Optional, the text of the message to send.
     * @param ssml        Optional, text to be spoken by your bot on a speech-enable
     *                    channel.
     * @param inputHint   Optional, indicates whether your bot is accepting,
     *                    expecting, or ignoring user input after the message is
     *                    delivered to the client. Default is
     *                    {@link InputHints#ACCEPTING_INPUT}.
     * @return A message activity containing the attachment.
     */
    public static Activity carousel(
        List<Attachment> attachments,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        if (attachments == null) {
            throw new IllegalArgumentException("attachments cannot be null");
        }

        return attachmentActivity(
            AttachmentLayoutTypes.CAROUSEL, attachments, text, ssml, inputHint
        );
    }

    /**
     * Returns a message activity that contains a single image or video.
     *
     * @param url         The URL of the image or video to send.
     * @param contentType The MIME type of the image or video.
     * @return A message activity containing the attachment.
     */
    public static Activity contentUrl(String url, String contentType) {
        return contentUrl(url, contentType, null, null, null, null);
    }

    /**
     * Returns a message activity that contains a single image or video.
     *
     * @param url         The URL of the image or video to send.
     * @param contentType The MIME type of the image or video.
     * @param name        Optional, the name of the image or video file.
     * @param text        Optional, the text of the message to send.
     * @param ssml        Optional, text to be spoken by your bot on a speech-enable
     *                    channel.
     * @param inputHint   Optional, indicates whether your bot is accepting,
     *                    expecting, or ignoring user input after the message is
     *                    delivered to the client. Default is
     *                    {@link InputHints#ACCEPTING_INPUT}.
     * @return A message activity containing the attachment.
     */
    public static Activity contentUrl(
        String url,
        String contentType,
        String name,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url cannot be null or empty");
        }

        if (StringUtils.isEmpty(contentType)) {
            throw new IllegalArgumentException("contentType cannot be null or empty");
        }

        Attachment attachment = new Attachment();
        attachment.setContentType(contentType);
        attachment.setContentUrl(url);
        attachment.setName(StringUtils.isEmpty(name) ? null : name);

        return attachmentActivity(
            AttachmentLayoutTypes.LIST, Collections.singletonList(attachment), text, ssml, inputHint
        );
    }

    private static Activity attachmentActivity(
        AttachmentLayoutTypes attachmentLayout,
        List<Attachment> attachments,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        Activity activity = Activity.createMessageActivity();
        activity.setAttachmentLayout(attachmentLayout);
        activity.setAttachments(attachments);
        setTextAndSpeech(activity, text, ssml, inputHint);
        return activity;
    }

    private static void setTextAndSpeech(
        Activity activity,
        String text,
        String ssml,
        InputHints inputHint
    ) {
        activity.setText(StringUtils.isEmpty(text) ? null : text);
        activity.setSpeak(StringUtils.isEmpty(ssml) ? null : ssml);
        activity.setInputHint(inputHint == null ? InputHints.ACCEPTING_INPUT : inputHint);
    }
}
