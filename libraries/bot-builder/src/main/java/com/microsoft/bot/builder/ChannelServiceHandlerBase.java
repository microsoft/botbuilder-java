// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.AttachmentData;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.ConversationsResult;
import com.microsoft.bot.schema.PagedMembersResult;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Transcript;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for Bot Framework protocol implementation.
 */
public abstract class ChannelServiceHandlerBase {

    /**
     * Sends an activity to the end of a conversation.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     * @param activity        The activity to send.
     *
     * @return   A {@link CompletableFuture {TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<ResourceResponse> handleSendToConversation(
        String authHeader,
        String conversationId,
        Activity activity) {

        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onSendToConversation(claimsIdentity, conversationId, activity);
        });
    }

    /**
     * Sends a reply to an activity.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     * @param activityId      The activity Id the reply is to.
     * @param activity        The activity to send.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<ResourceResponse> handleReplyToActivity(
        String authHeader,
        String conversationId,
        String activityId,
        Activity activity) {

        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onReplyToActivity(claimsIdentity, conversationId, activityId, activity);
        });
    }

    /**
     * Edits a previously sent existing activity.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     * @param activityId      The activity Id to update.
     * @param activity        The replacement activity.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<ResourceResponse> handleUpdateActivity(
        String authHeader,
        String conversationId,
        String activityId,
        Activity activity) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onUpdateActivity(claimsIdentity, conversationId, activityId, activity);
        });
    }

    /**
     * Deletes an existing activity.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     * @param activityId      The activity Id.
     *
     * @return   A {@link CompletableFuture} representing the result of
     *           the asynchronous operation.
     */
    public CompletableFuture<Void> handleDeleteActivity(String authHeader, String conversationId, String activityId) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onDeleteActivity(claimsIdentity, conversationId, activityId);
        });
    }

    /**
     * Enumerates the members of an activity.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     * @param activityId      The activity Id.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<List<ChannelAccount>> handleGetActivityMembers(
        String authHeader,
        String conversationId,
        String activityId) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onGetActivityMembers(claimsIdentity, conversationId, activityId);
        });
    }

    /**
     * Create a new Conversation.
     *
     * @param authHeader  The authentication header.
     * @param parameters  Parameters to create the conversation from.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<ConversationResourceResponse> handleCreateConversation(
        String authHeader,
        ConversationParameters parameters) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onCreateConversation(claimsIdentity, parameters);
        });
    }

    /**
     * Lists the Conversations in which the bot has participated.
     *
     * @param authHeader         The authentication header.
     * @param conversationId     The conversation Id.
     * @param continuationToken  A skip or continuation token.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<ConversationsResult> handleGetConversations(
        String authHeader,
        String conversationId,
        String continuationToken) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onGetConversations(claimsIdentity, conversationId, continuationToken);
        });
    }

    /**
     * Enumerates the members of a conversation.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<List<ChannelAccount>> handleGetConversationMembers(
        String authHeader,
        String conversationId) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onGetConversationMembers(claimsIdentity, conversationId);
        });
    }

    /**
     * Enumerates the members of a conversation one page at a time.
     *
     * @param authHeader         The authentication header.
     * @param conversationId     The conversation Id.
     * @param pageSize           Suggested page size.
     * @param continuationToken  A continuation token.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<PagedMembersResult> handleGetConversationPagedMembers(
        String authHeader,
        String conversationId,
        Integer pageSize,
        String continuationToken) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onGetConversationPagedMembers(claimsIdentity, conversationId, pageSize, continuationToken);
        });
    }

    /**
     * Deletes a member from a conversation.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     * @param memberId        Id of the member to delete from this
     *                        conversation.
     *
     * @return   A {@link CompletableFuture} representing the
     *           asynchronous operation.
     */
    public CompletableFuture<Void> handleDeleteConversationMember(
        String authHeader,
        String conversationId,
        String memberId) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onDeleteConversationMember(claimsIdentity, conversationId, memberId);
        });
    }

    /**
     * Uploads the historic activities of the conversation.
     *
     * @param authHeader      The authentication header.
     * @param conversationId  The conversation Id.
     * @param transcript      Transcript of activities.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<ResourceResponse> handleSendConversationHistory(
        String authHeader,
        String conversationId,
        Transcript transcript) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onSendConversationHistory(claimsIdentity, conversationId, transcript);
        });
    }

    /**
     * Stores data in a compliant store when dealing with enterprises.
     *
     * @param authHeader        The authentication header.
     * @param conversationId    The conversation Id.
     * @param attachmentUpload  Attachment data.
     *
     * @return   A {@link CompletableFuture{TResult}} representing the
     *           result of the asynchronous operation.
     */
    public CompletableFuture<ResourceResponse> handleUploadAttachment(
        String authHeader,
        String conversationId,
        AttachmentData attachmentUpload) {
        return authenticate(authHeader).thenCompose(claimsIdentity -> {
            return onUploadAttachment(claimsIdentity, conversationId, attachmentUpload);
        });
    }

    /**
     * Helper to authenticate the header token and extract the claims.
     * @param authHeader The auth header containing JWT token.
     * @return A {@link ClaimsIdentity} representing the claims associated with given header.
     */
    protected abstract CompletableFuture<ClaimsIdentity> authenticate(String authHeader);

    /**
     * SendToConversation() API for Skill.
     *
     * This method allows you to send an activity to the end of a conversation.
     * This is slightly different from ReplyToActivity(). *
     * SendToConversation(conversationId) - will append the activity to the end
     * of the conversation according to the timestamp or semantics of the
     * channel. * ReplyToActivity(conversationId,ActivityId) - adds the
     * activity as a reply to another activity, if the channel supports it. If
     * the channel does not support nested replies, ReplyToActivity falls back
     * to SendToConversation. Use ReplyToActivity when replying to a specific
     * activity in the conversation. Use SendToConversation in all other cases.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  conversationId.
     * @param activity        Activity to send.
     *
     * @return Task for a resource response.
     */
    protected CompletableFuture<ResourceResponse> onSendToConversation(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        Activity activity) {
        throw new NotImplementedException("onSendToConversation is not implemented");
    }

    /**
     * OnReplyToActivity() API.
     *
     * Override this method allows to reply to an Activity. This is slightly
     * different from SendToConversation(). *
     * SendToConversation(conversationId) - will append the activity to the end
     * of the conversation according to the timestamp or semantics of the
     * channel. * ReplyToActivity(conversationId,ActivityId) - adds the
     * activity as a reply to another activity, if the channel supports it. If
     * the channel does not support nested replies, ReplyToActivity falls back
     * to SendToConversation. Use ReplyToActivity when replying to a specific
     * activity in the conversation. Use SendToConversation in all other cases.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation D.
     * @param activityId      activityId the reply is to (OPTIONAL).
     * @param activity        Activity to send.
     *
     * @return Task for a resource response.
     */
    protected CompletableFuture<ResourceResponse> onReplyToActivity(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String activityId,
        Activity activity) {
        throw new NotImplementedException("onReplyToActivity is not implemented");
    }

    /**
     * OnUpdateActivity() API.
     *
     * Override this method to edit a previously sent existing activity. Some
     * channels allow you to edit an existing activity to reflect the new state
     * of a bot conversation. For example, you can remove buttons after someone
     * has clicked "Approve" button.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation D.
     * @param activityId      activityId to update.
     * @param activity        replacement Activity.
     *
     * @return Task for a resource response.
     */
    protected CompletableFuture<ResourceResponse> onUpdateActivity(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String activityId,
        Activity activity) {
        throw new NotImplementedException("onUpdateActivity is not implemented");
    }

    /**
     * OnDeleteActivity() API.
     *
     * Override this method to Delete an existing activity. Some channels allow
     * you to delete an existing activity, and if successful this method will
     * remove the specified activity.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation D.
     * @param activityId      activityId to delete.
     *
     * @return Task for a resource response.
     */
    protected CompletableFuture<Void> onDeleteActivity(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String activityId) {
        throw new NotImplementedException("onDeleteActivity is not implemented");
    }

    /**
     * OnGetActivityMembers() API.
     *
     * Override this method to enumerate the members of an activity. This REST
     * API takes a ConversationId and a ActivityId, returning an array of
     * ChannelAccount Objects representing the members of the particular
     * activity in the conversation.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation D.
     * @param activityId      Activity D.
     *
     * @return Task with result.
     */
    protected CompletableFuture<List<ChannelAccount>> onGetActivityMembers(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String activityId) {
        throw new NotImplementedException("onGetActivityMembers is not implemented");
    }

    /**
     * CreateConversation() API.
     *
     * Override this method to create a new Conversation. POST to this method
     * with a * Bot being the bot creating the conversation * IsGroup set to
     * true if this is not a direct message (default instanceof false) * Array
     * containing the members to include in the conversation The return value
     * is a ResourceResponse which contains a conversation D which is suitable
     * for use in the message payload and REST API URIs. Most channels only
     * support the semantics of bots initiating a direct message conversation.
     * An example of how to do that would be: var resource =
     * connector.getconversations().CreateConversation(new
     * ConversationParameters(){ Bot = bot, members = new ChannelAccount[] {
     * new ChannelAccount("user1") } );
     * connect.getConversations().OnSendToConversation(resource.getId(), new
     * Activity() ... ) ; end.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param parameters      Parameters to create the conversation
     *                        from.
     *
     * @return Task for a conversation resource response.
     */
    protected CompletableFuture<ConversationResourceResponse> onCreateConversation(
        ClaimsIdentity claimsIdentity,
        ConversationParameters parameters) {
        throw new NotImplementedException("onCreateConversation is not implemented");
    }

    /**
     * OnGetConversations() API for Skill.
     *
     * Override this method to list the Conversations in which this bot has
     * participated. GET from this method with a skip token The return value is
     * a ConversationsResult, which contains an array of ConversationMembers
     * and a skip token. If the skip token is not empty, then there are further
     * values to be returned. Call this method again with the returned token to
     * get more values. Each ConversationMembers Object contains the D of the
     * conversation and an array of ChannelAccounts that describe the members
     * of the conversation.
     *
     * @param claimsIdentity     claimsIdentity for the bot, should have
     *                           AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId     conversationId.
     * @param continuationToken  skip or continuation token.
     *
     * @return Task for ConversationsResult.
     */
    protected CompletableFuture<ConversationsResult> onGetConversations(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String continuationToken) {
        throw new NotImplementedException("onGetConversations is not implemented");
    }

    /**
     * GetConversationMembers() API for Skill.
     *
     * Override this method to enumerate the members of a conversation. This
     * REST API takes a ConversationId and returns an array of ChannelAccount
     * Objects representing the members of the conversation.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation ID.
     *
     * @return Task for a response.
     */
    protected CompletableFuture<List<ChannelAccount>> onGetConversationMembers(
        ClaimsIdentity claimsIdentity,
        String conversationId) {
        throw new NotImplementedException("onGetConversationMembers is not implemented");
    }

    /**
     * GetConversationPagedMembers() API for Skill.
     *
     * Override this method to enumerate the members of a conversation one page
     * at a time. This REST API takes a ConversationId. Optionally a pageSize
     * and/or continuationToken can be provided. It returns a
     * PagedMembersResult, which contains an array of ChannelAccounts
     * representing the members of the conversation and a continuation token
     * that can be used to get more values. One page of ChannelAccounts records
     * are returned with each call. The number of records in a page may vary
     * between channels and calls. The pageSize parameter can be used as a
     * suggestion. If there are no additional results the response will not
     * contain a continuation token. If there are no members in the
     * conversation the Members will be empty or not present in the response. A
     * response to a request that has a continuation token from a prior request
     * may rarely return members from a previous request.
     *
     * @param claimsIdentity     claimsIdentity for the bot, should have
     *                           AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId     Conversation ID.
     * @param pageSize           Suggested page size.
     * @param continuationToken  Continuation Token.
     *
     * @return Task for a response.
     */
    protected CompletableFuture<PagedMembersResult> onGetConversationPagedMembers(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        Integer pageSize,
        String continuationToken) {
        throw new NotImplementedException("onGetConversationPagedMembers is not implemented");
    }

    /**
     * DeleteConversationMember() API for Skill.
     *
     * Override this method to deletes a member from a conversation. This REST
     * API takes a ConversationId and a memberId (of type String) and removes
     * that member from the conversation. If that member was the last member of
     * the conversation, the conversation will also be deleted.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation ID.
     * @param memberId        ID of the member to delete from this
     *                        conversation.
     *
     * @return Task.
     */
    protected CompletableFuture<Void> onDeleteConversationMember(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        String memberId) {
        throw new NotImplementedException("onDeleteConversationMember is not implemented");
    }

    /**
     * SendConversationHistory() API for Skill.
     *
     * Override this method to this method allows you to upload the historic
     * activities to the conversation. Sender must ensure that the historic
     * activities have unique ids and appropriate timestamps. The ids are used
     * by the client to deal with duplicate activities and the timestamps are
     * used by the client to render the activities in the right order.
     *
     * @param claimsIdentity  claimsIdentity for the bot, should have
     *                        AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId  Conversation ID.
     * @param transcript      Transcript of activities.
     *
     * @return Task for a resource response.
     */
    protected CompletableFuture<ResourceResponse> onSendConversationHistory(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        Transcript transcript) {
        throw new NotImplementedException("onSendConversationHistory is not implemented");
    }

    /**
     * UploadAttachment() API.
     *
     * Override this method to store data in a compliant store when dealing
     * with enterprises. The response is a ResourceResponse which contains an
     * AttachmentId which is suitable for using with the attachments API.
     *
     * @param claimsIdentity    claimsIdentity for the bot, should have
     *                          AudienceClaim, AppIdClaim and ServiceUrlClaim.
     * @param conversationId    Conversation ID.
     * @param attachmentUpload  Attachment data.
     *
     * @return Task with result.
     */
    protected CompletableFuture<ResourceResponse> onUploadAttachment(
        ClaimsIdentity claimsIdentity,
        String conversationId,
        AttachmentData attachmentUpload) {
        throw new NotImplementedException("onUploadAttachment is not implemented");
    }
}
