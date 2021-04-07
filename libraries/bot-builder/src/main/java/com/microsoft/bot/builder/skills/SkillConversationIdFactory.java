// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.ConversationReference;

import org.apache.commons.lang3.StringUtils;

/**
 * A {@link SkillConversationIdFactory} that uses an in memory
 * {@link Map{TKey,TValue}} to store and retrieve {@link ConversationReference}
 * instances.
 */
public class SkillConversationIdFactory extends SkillConversationIdFactoryBase {

    private Storage storage;

    /**
     * Creates an instance of a SkillConversationIdFactory.
     *
     * @param storage A storage instance for the factory.
     */
    public SkillConversationIdFactory(Storage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null.");
        }
        this.storage = storage;
    }

    /**
     * Creates a conversation id for a skill conversation.
     *
     * @param options A {@link SkillConversationIdFactoryOptions} instance
     *                containing parameters for creating the conversation ID.
     *
     * @return A unique conversation ID used to communicate with the skill.
     *
     *         It should be possible to use the returned String on a request URL and
     *         it should not contain special characters.
     */
    @Override
    public CompletableFuture<String> createSkillConversationId(SkillConversationIdFactoryOptions options) {
        if (options == null) {
            Async.completeExceptionally(new IllegalArgumentException("options cannot be null."));
        }
        ConversationReference conversationReference = options.getActivity().getConversationReference();
        String skillConversationId = UUID.randomUUID().toString();

        SkillConversationReference skillConversationReference = new SkillConversationReference();
        skillConversationReference.setConversationReference(conversationReference);
        skillConversationReference.setOAuthScope(options.getFromBotOAuthScope());
        Map<String, Object> skillConversationInfo = new HashMap<String, Object>();
        skillConversationInfo.put(skillConversationId, skillConversationReference);
        return storage.write(skillConversationInfo)
                .thenCompose(result -> CompletableFuture.completedFuture(skillConversationId));
    }

    /**
     * Gets the {@link SkillConversationReference} created using
     * {@link SkillConversationIdFactory#createSkillConversationId} for a
     * skillConversationId.
     *
     * @param skillConversationId A skill conversationId created using
     *                            {@link SkillConversationIdFactory#createSkillConversationId}.
     *
     * @return The caller's {@link ConversationReference} for a skillConversationId.
     *         null if not found.
     */
    @Override
    public CompletableFuture<SkillConversationReference> getSkillConversationReference(String skillConversationId) {
        if (StringUtils.isAllBlank(skillConversationId)) {
            Async.completeExceptionally(new IllegalArgumentException("skillConversationId cannot be null."));
        }

        return storage.read(new String[] {skillConversationId}).thenCompose(skillConversationInfo -> {
            if (skillConversationInfo.size() > 0) {
                return CompletableFuture
                        .completedFuture((SkillConversationReference) skillConversationInfo.get(skillConversationId));
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
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
    @Override
    public CompletableFuture<Void> deleteConversationReference(String skillConversationId) {
        return storage.delete(new String[] {skillConversationId});
    }
}
