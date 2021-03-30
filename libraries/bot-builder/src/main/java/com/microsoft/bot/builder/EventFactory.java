// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.ConversationAccount;
import com.microsoft.bot.schema.Entity;
import com.microsoft.bot.schema.HandoffEventNames;
import com.microsoft.bot.schema.Transcript;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains utility methods for creating various event types.
 */
public final class EventFactory {

    private EventFactory() {

    }

    /**
     * Create handoff initiation event.
     *
     * @param turnContext    turn context.
     * @param handoffContext agent hub-specific context.
     *
     * @return handoff event.
     */
    public static Activity createHandoffInitiation(TurnContext turnContext, Object handoffContext) {
        return createHandoffInitiation(turnContext, handoffContext, null);
    }


    /**
     * Create handoff initiation event.
     *
     * @param turnContext    turn context.
     * @param handoffContext agent hub-specific context.
     * @param transcript     transcript of the conversation.
     *
     * @return handoff event.
     */
    public static Activity createHandoffInitiation(TurnContext turnContext, Object handoffContext,
            Transcript transcript) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null.");
        }

        Activity handoffEvent = createHandoffEvent(HandoffEventNames.INITIATEHANDOFF, handoffContext,
                turnContext.getActivity().getConversation());

        handoffEvent.setFrom(turnContext.getActivity().getFrom());
        handoffEvent.setRelatesTo(turnContext.getActivity().getConversationReference());
        handoffEvent.setReplyToId(turnContext.getActivity().getId());
        handoffEvent.setServiceUrl(turnContext.getActivity().getServiceUrl());
        handoffEvent.setChannelId(turnContext.getActivity().getChannelId());

        if (transcript != null) {
            Attachment attachment = new Attachment();
            attachment.setContent(transcript);
            attachment.setContentType("application/json");
            attachment.setName("Transcript");
            handoffEvent.getAttachments().add(attachment);
        }

        return handoffEvent;
    }


    /**
     * Create handoff status event.
     *
     * @param conversation Conversation being handed over.
     * @param state        State, possible values are: "accepted", "failed",
     *                     "completed".
     *
     * @return handoff event.
     */
    public static Activity createHandoffStatus(ConversationAccount conversation, String state) {
        return createHandoffStatus(conversation, state, null);
    }

    /**
     * Create handoff status event.
     *
     * @param conversation Conversation being handed over.
     * @param state        State, possible values are: "accepted", "failed",
     *                     "completed".
     * @param message      Additional message for failed handoff.
     *
     * @return handoff event.
     */
    public static Activity createHandoffStatus(ConversationAccount conversation, String state, String message) {
        if (conversation == null) {
            throw new IllegalArgumentException("conversation cannot be null.");
        }

        if (state == null) {
            throw new IllegalArgumentException("state cannot be null.");
        }

        ObjectNode handoffContext = JsonNodeFactory.instance.objectNode();
        handoffContext.set("state", JsonNodeFactory.instance.textNode(state));
        if (StringUtils.isNotBlank(message)) {
            handoffContext.set("message", JsonNodeFactory.instance.textNode(message));
        }

        Activity handoffEvent = createHandoffEvent(HandoffEventNames.HANDOFFSTATUS, handoffContext, conversation);
        return handoffEvent;
    }

    private static Activity createHandoffEvent(String name, Object value, ConversationAccount conversation) {
        Activity handoffEvent = Activity.createEventActivity();

        handoffEvent.setName(name);
        handoffEvent.setValue(value);
        handoffEvent.setId(UUID.randomUUID().toString());
        handoffEvent.setTimestamp(OffsetDateTime.now(ZoneId.of("UTC")));
        handoffEvent.setConversation(conversation);
        handoffEvent.setAttachments(new ArrayList<Attachment>());
        handoffEvent.setEntities(new ArrayList<Entity>());
        return handoffEvent;
    }
}
