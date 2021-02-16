// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.skills;

import com.microsoft.bot.schema.ConversationReference;

/**
 * A conversation reference type for skills.
 */
public class SkillConversationReference {

    private ConversationReference conversationReference;

    private String oAuthScope;

    /**
     * Gets the conversation reference.
     * @return the ConversationReference value as a getConversationReference().
     */
    public ConversationReference getConversationReference() {
        return this.conversationReference;
    }

    /**
     * Sets the conversation reference.
     * @param withConversationReference The ConversationReference value.
     */
    public void setConversationReference(ConversationReference withConversationReference) {
        this.conversationReference = withConversationReference;
    }

    /**
     * Gets the OAuth scope.
     * @return the OAuthScope value as a String.
     */
    public String getOAuthScope() {
        return this.oAuthScope;
    }

    /**
     * Sets the OAuth scope.
     * @param withOAuthScope The OAuthScope value.
     */
    public void setOAuthScope(String withOAuthScope) {
        this.oAuthScope = withOAuthScope;
    }

}
