// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.sample.simplerootbot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.skills.SkillConversationIdFactoryBase;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryOptions;
import com.microsoft.bot.builder.skills.SkillConversationReference;

/**
 * A {@link SkillConversationIdFactory} that uses an in memory
 * {@link Map{TKey,TValue}} to store and retrieve {@link ConversationReference}
 * instances.
 */
public class SkillConversationIdFactory extends SkillConversationIdFactoryBase {

    private final Map<String, SkillConversationReference> _conversationRefs =
        new HashMap<String, SkillConversationReference>();

    @Override
    public CompletableFuture<String> createSkillConversationId(SkillConversationIdFactoryOptions options) {
        SkillConversationReference skillConversationReference = new SkillConversationReference();
        skillConversationReference.setConversationReference(options.getActivity().getConversationReference());
        skillConversationReference.setOAuthScope(options.getFromBotOAuthScope());
        String key = String.format(
            "%s-%s-%s-%s-skillconvo",
            options.getFromBotId(),
            options.getBotFrameworkSkill().getAppId(),
            skillConversationReference.getConversationReference().getConversation().getId(),
            skillConversationReference.getConversationReference().getChannelId()
        );
        _conversationRefs.put(key, skillConversationReference);
        return CompletableFuture.completedFuture(key);
    }

    @Override
    public CompletableFuture<SkillConversationReference> getSkillConversationReference(String skillConversationId) {
        SkillConversationReference conversationReference = _conversationRefs.get(skillConversationId);
        return CompletableFuture.completedFuture(conversationReference);
    }

    @Override
    public CompletableFuture<Void> deleteConversationReference(String skillConversationId) {
        _conversationRefs.remove(skillConversationId);
        return CompletableFuture.completedFuture(null);
    }
}
