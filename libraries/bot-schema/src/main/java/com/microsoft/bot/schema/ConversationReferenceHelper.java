/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import java.util.UUID;

public class ConversationReferenceHelper {
    private ConversationReference reference;

    public ConversationReferenceHelper(ConversationReference withReference) {
        this.reference = withReference;
    }

    /**
     * Creates {@link Activity} from conversation reference as it is posted to bot.
     */
    public Activity getPostToBotMessage() {
        Activity activity = new Activity();
        activity.setType(ActivityTypes.MESSAGE);
        activity.setId(UUID.randomUUID().toString());
        activity.setRecipient(new ChannelAccount(
            reference.getBot().getId(),
            reference.getBot().getName()));
        activity.setChannelId(reference.getChannelId());
        activity.setServiceUrl(reference.getServiceUrl());
        activity.setConversation(new ConversationAccount(
            reference.getConversation().isGroup(),
            reference.getConversation().getId(),
            reference.getConversation().getName()));
        activity.setFrom(new ChannelAccount(
            reference.getUser().getId(),
            reference.getUser().getName()));

        return activity;
    }

    /**
     * Creates {@link Activity} from conversation reference that can be posted to user as reply.
     */
    public Activity getPostToUserMessage() {
        Activity msg = this.getPostToBotMessage();

        // swap from and recipient
        msg.setFrom(msg.getRecipient());
        msg.setRecipient(msg.getFrom());

        return msg;
    }
}


