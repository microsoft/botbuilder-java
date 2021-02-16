// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.schema.ConversationReference;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Defines the interface of a factory that is used to create unique conversation
 * IDs for skill conversations.
 */
public abstract class SkillConversationIdFactoryBase {

    /**
     * Creates a conversation ID for a skill conversation super. on the
     * caller's {@link ConversationReference} .
     *
     * @param conversationReference  The skill's caller  {@link ConversationReference} .
     *
     * @return   A unique conversation ID used to communicate with the
     *           skill.
     *
     * It should be possible to use the returned String on a request URL and it
     * should not contain special characters.
     */
    public CompletableFuture<String> createSkillConversationId(ConversationReference conversationReference) {
        throw new NotImplementedException("createSkillConversationId");
    }

    /**
     * Creates a conversation id for a skill conversation.
     *
     * @param options  A {@link SkillConversationIdFactoryOptions}
     *                 instance containing parameters for creating the conversation ID.
     *
     * @return   A unique conversation ID used to communicate with the skill.
     *
     * It should be possible to use the returned String on a request URL and it
     * should not contain special characters.
     */
    public CompletableFuture<String> createSkillConversationId(SkillConversationIdFactoryOptions options) {
        throw new NotImplementedException("createSkillConversationId");
    }

    /**
     * Gets the {@link ConversationReference} created using
     * {@link
     * CreateSkillConversationId(Microsoft#getBot()#getSchema()#getConversatio
     * Reference(),System#getThreading()#getCancellationToken())} for a
     * skillConversationId.
     *
     * @param skillConversationId  A skill conversationId created using {@link
     *                             CreateSkillConversationId(Microsoft#getBot()#getSchema()#getConversatio
     *                             Reference(),System#getThreading()#getCancellationToken())} .
     *
     * @return   The caller's {@link ConversationReference} for a skillConversationId. null if not found.
     */
    public CompletableFuture<ConversationReference> getConversationReference(String skillConversationId) {
        throw new NotImplementedException("getConversationReference");
    }

    /**
     * Gets the {@link SkillConversationReference} used during {@link
     * CreateSkillConversationId(SkillConversationIdFactoryOptions,System#getT
     * reading()#getCancellationToken())} for a skillConversationId.
     *
     * @param skillConversationId  A skill conversationId created using {@link
     *                             CreateSkillConversationId(SkillConversationIdFactoryOptions,System#getT
     *                             reading()#getCancellationToken())} .
     *
     * @return   The caller's {@link ConversationReference} for a skillConversationId, with originatingAudience.
     *           Null if not found.
     */
    public CompletableFuture<SkillConversationReference> getSkillConversationReference(String skillConversationId) {
        throw new NotImplementedException("getSkillConversationReference");
    }

    /**
     * Deletes a {@link ConversationReference} .
     *
     * @param skillConversationId  A skill conversationId created using {@link
     *                             CreateSkillConversationId(SkillConversationIdFactoryOptions,System#getT
     *                             reading()#getCancellationToken())} .
     *
     * @return   A {@link CompletableFuture} representing the asynchronous operation.
     */
    public abstract CompletableFuture<Void> deleteConversationReference(String skillConversationId);
}

