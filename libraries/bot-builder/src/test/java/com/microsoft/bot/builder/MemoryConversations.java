// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.Conversations;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.AttachmentData;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.ConversationsResult;
import com.microsoft.bot.schema.PagedMembersResult;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Transcript;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.NotImplementedException;

public class MemoryConversations implements Conversations {
    private List<Activity> sentActivities = new ArrayList<>();

    public List<Activity> getSentActivities() {
        return sentActivities;
    }

    @Override
    public CompletableFuture<ConversationsResult> getConversations() {
        return notImplemented("getConversations");
    }

    @Override
    public CompletableFuture<ConversationsResult> getConversations(String continuationToken) {
        return notImplemented("getConversations");
    }

    @Override
    public CompletableFuture<ConversationResourceResponse> createConversation(
        ConversationParameters parameters
    ) {
        return notImplemented("createConversation");
    }

    @Override
    public CompletableFuture<ResourceResponse> sendToConversation(
        String conversationId,
        Activity activity
    ) {
        sentActivities.add(activity);
        ResourceResponse response = new ResourceResponse();
        response.setId(activity.getId());
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public CompletableFuture<ResourceResponse> updateActivity(
        String conversationId, String activityId,
        Activity activity
    ) {
        return notImplemented("updateActivity");
    }

    @Override
    public CompletableFuture<ResourceResponse> replyToActivity(
        String conversationId, String activityId,
        Activity activity
    ) {
        sentActivities.add(activity);
        ResourceResponse response = new ResourceResponse();
        response.setId(activity.getId());
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public CompletableFuture<Void> deleteActivity(String conversationId, String activityId) {
        return notImplemented("deleteActivity");
    }

    @Override
    public CompletableFuture<List<ChannelAccount>> getConversationMembers(
        String conversationId
    ) {
        return notImplemented("getConversationMembers");
    }

    @Override
    public CompletableFuture<ChannelAccount> getConversationMember(
        String userId, String conversationId
    ) {
        return notImplemented("getConversationMember");
    }

    @Override
    public CompletableFuture<Void> deleteConversationMember(
        String conversationId, String memberId
    ) {
        return notImplemented("deleteConversationMember");
    }

    @Override
    public CompletableFuture<List<ChannelAccount>> getActivityMembers(
        String conversationId, String activityId
    ) {
        return notImplemented("getActivityMembers");
    }

    @Override
    public CompletableFuture<ResourceResponse> uploadAttachment(
        String conversationId, AttachmentData attachmentUpload
    ) {
        return notImplemented("uploadAttachment");
    }

    @Override
    public CompletableFuture<ResourceResponse> sendConversationHistory(
        String conversationId, Transcript history
    ) {
        return notImplemented("sendConversationHistory");
    }

    @Override
    public CompletableFuture<PagedMembersResult> getConversationPagedMembers(
        String conversationId
    ) {
        return notImplemented("getConversationPagedMembers");
    }

    @Override
    public CompletableFuture<PagedMembersResult> getConversationPagedMembers(
        String conversationId,
        String continuationToken
    ) {
        return notImplemented("getConversationPagedMembers");
    }

    protected <T> CompletableFuture<T> notImplemented(String message) {
        CompletableFuture<T> result = new CompletableFuture<>();
        result.completeExceptionally(
            new NotImplementedException(message)
        );
        return result;
    }
}
