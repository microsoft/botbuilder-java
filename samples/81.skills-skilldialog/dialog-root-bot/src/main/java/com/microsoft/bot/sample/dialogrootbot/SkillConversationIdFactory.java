// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.sample.dialogrootbot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.Storage;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryBase;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryOptions;
import com.microsoft.bot.builder.skills.SkillConversationReference;
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

    public SkillConversationIdFactory(Storage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null.");
        }
        this.storage = storage;
    }

    @Override
    public CompletableFuture<String> createSkillConversationId(SkillConversationIdFactoryOptions options) {
        if (options == null) {
            Async.completeExceptionally(new IllegalArgumentException("options cannot be null."));
        }
        ConversationReference conversationReference = options.getActivity().getConversationReference();
        String skillConversationId = String.format(
            "%s-%s-%s-skillconvo",
            conversationReference.getConversation().getId(),
            options.getBotFrameworkSkill().getId(),
            conversationReference.getChannelId()
        );

        SkillConversationReference skillConversationReference = new SkillConversationReference();
        skillConversationReference.setConversationReference(conversationReference);
        skillConversationReference.setOAuthScope(options.getFromBotOAuthScope());
        Map<String, Object> skillConversationInfo = new HashMap<String, Object>();
        skillConversationInfo.put(skillConversationId, skillConversationReference);
        return storage.write(skillConversationInfo)
            .thenCompose(result -> CompletableFuture.completedFuture(skillConversationId));
    }

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

    @Override
    public CompletableFuture<Void> deleteConversationReference(String skillConversationId) {
        return storage.delete(new String[] {skillConversationId});
    }
}
