// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.integration.spring;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.bot.builder.ChannelServiceHandler;
import com.microsoft.bot.connector.authentication.AuthenticationException;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.AttachmentData;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationResourceResponse;
import com.microsoft.bot.schema.ConversationsResult;
import com.microsoft.bot.schema.PagedMembersResult;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Transcript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A super.class for a skill controller.
 */
// Note: this class instanceof marked as abstract to prevent the ASP runtime from registering it as a controller.
public abstract class ChannelServiceController {

    /**
     * The slf4j Logger to use. Note that slf4j is configured by providing Log4j
     * dependencies in the POM, and corresponding Log4j configuration in the
     * 'resources' folder.
     */
    private Logger logger = LoggerFactory.getLogger(BotController.class);

    private final ChannelServiceHandler handler;

    /**
     * Initializes a new instance of the {@link ChannelServiceController}
     * class.
     *
     * @param handler  A {@link ChannelServiceHandler} that will handle
     *                 the incoming request.
     */
    protected ChannelServiceController(ChannelServiceHandler handler) {
        this.handler = handler;
    }

    /**
     * SendToConversation.
     *
     * @param conversationId  Conversation Id.
     * @param activity        Activity to send.
     * @param authHeader      Authentication header.
     *
     * @return                A ResourceResponse.
     */
    @PostMapping("v3/conversations/{conversationId}/activities")
    public CompletableFuture<ResponseEntity<ResourceResponse>> sendToConversation(
        @PathVariable String conversationId,
        @RequestBody Activity activity,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {

        return handler.handleSendToConversation(authHeader, conversationId, activity)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<ResourceResponse>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * ReplyToActivity.
     *
     * @param conversationId  Conversation Id.
     * @param activityId      activityId the reply is to (OPTONAL).
     * @param activity        Activity to send.
     * @param authHeader      Authentication header.
     *
     * @return                A ResourceResponse.
     */
    @PostMapping("v3/conversations/{conversationId}/activities/{activityId}")
    public CompletableFuture<ResponseEntity<ResourceResponse>> replyToActivity(
        @PathVariable String conversationId,
        @PathVariable String activityId,
        @RequestBody Activity activity,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {
        return  handler.handleReplyToActivity(authHeader, conversationId, activityId, activity)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<ResourceResponse>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * UpdateActivity.
     *
     * @param conversationId  Conversation Id.
     * @param activityId      activityId to update.
     * @param activity        replacement Activity.
     * @param authHeader      Authentication header.
     *
     * @return                A ResourceResponse.
     */
    @PutMapping("v3/conversations/{conversationId}/activities/{activityId}")
    public CompletableFuture<ResponseEntity<ResourceResponse>> updateActivity(
        @PathVariable String conversationId,
        @PathVariable String activityId,
        @RequestBody Activity activity,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
        ) {
        return  handler.handleUpdateActivity(authHeader, conversationId, activityId, activity)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<ResourceResponse>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * DeleteActivity.
     *
     * @param conversationId  Conversation Id.
     * @param activityId      activityId to delete.
     * @param authHeader      Authentication header.
     *
     * @return                A void result if successful.
     */
    @DeleteMapping("v3/conversations/{conversationId}/activities/{activityId}")
    public CompletableFuture<ResponseEntity<Void>> deleteActivity(
        @PathVariable String conversationId,
        @PathVariable String activityId,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
        ) {
        return handler.handleDeleteActivity(authHeader, conversationId, activityId)
        .handle((result, exception) -> {
            if (exception == null) {
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * GetActivityMembers.
     *
     * Markdown=Content\Methods\GetActivityMembers.getmd().
     *
     * @param conversationId  Conversation Id.
     * @param activityId      Activity Id.
     * @param authHeader      Authentication header.
     *
     * @return                A list of ChannelAccount.
     */
    @GetMapping("v3/conversations/{conversationId}/activities/{activityId}/members")
    public CompletableFuture<ResponseEntity<List<ChannelAccount>>> getActivityMembers(
        @PathVariable String conversationId,
        @PathVariable String activityId,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {
        return handler.handleGetActivityMembers(authHeader, conversationId, activityId)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<List<ChannelAccount>>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * CreateConversation.
     *
     * @param parameters  Parameters to create the conversation from.
     * @param authHeader  Authentication header.
     *
     * @return            A ConversationResourceResponse.
     */
    @PostMapping("v3/conversations")
    public CompletableFuture<ResponseEntity<ConversationResourceResponse>> createConversation(
        @RequestBody ConversationParameters parameters,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
        ) {
        return  handler.handleCreateConversation(authHeader, parameters)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<ConversationResourceResponse>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * GetConversations.
     *
     * @param conversationId     the conversation id to get conversations for.
     * @param continuationToken  skip or continuation token.
     * @param authHeader         Authentication header.
     *
     * @return                   A ConversationsResult.
     */
    @GetMapping("v3/conversations")
    public CompletableFuture<ResponseEntity<ConversationsResult>> getConversations(
        @RequestParam String conversationId,
        @RequestParam String continuationToken,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {
        return handler.handleGetConversations(authHeader, conversationId, continuationToken)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<ConversationsResult>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * GetConversationMembers.
     *
     * @param conversationId  Conversation Id.
     * @param authHeader      Authentication header.
     *
     * @return                A List of ChannelAccount.
     */
    @GetMapping("v3/conversations/{conversationId}/members")
    public CompletableFuture<ResponseEntity<List<ChannelAccount>>> getConversationMembers(
        @PathVariable String conversationId,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
        ) {
        return  handler.handleGetConversationMembers(authHeader, conversationId)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<List<ChannelAccount>>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * GetConversationPagedMembers.
     *
     * @param conversationId     Conversation Id.
     * @param pageSize           Suggested page size.
     * @param continuationToken  Continuation Token.
     * @param authHeader         Authentication header.
     *
     * @return                   A PagedMembersResult.
     */
    @GetMapping("v3/conversations/{conversationId}/pagedmembers")
    public CompletableFuture<ResponseEntity<PagedMembersResult>> getConversationPagedMembers(
        @PathVariable String conversationId,
        @RequestParam(name = "pageSize", defaultValue = "-1") int pageSize,
        @RequestParam(name = "continuationToken") String continuationToken,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {
        return  handler.handleGetConversationPagedMembers(authHeader, conversationId, pageSize, continuationToken)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<PagedMembersResult>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * DeleteConversationMember.
     *
     * @param conversationId  Conversation Id.
     * @param memberId        D of the member to delete from this
     *                        conversation.
     * @param authHeader      Authentication header.
     *
     * @return                A void result.
     */
    @DeleteMapping("v3/conversations/{conversationId}/members/{memberId}")
    public CompletableFuture<ResponseEntity<Void>> deleteConversationMember(
        @PathVariable String conversationId,
        @PathVariable String memberId,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
        ) {
         return handler.handleDeleteConversationMember(authHeader, conversationId, memberId)
         .handle((result, exception) -> {
            if (exception == null) {
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * SendConversationHistory.
     *
     * @param conversationId  Conversation Id.
     * @param history         Historic activities.
     * @param authHeader      Authentication header.
     *
     * @return                A ResourceResponse.
     */
    @PostMapping("v3/conversations/{conversationId}/activities/history")
     public CompletableFuture<ResponseEntity<ResourceResponse>> sendConversationHistory(
        @PathVariable String conversationId,
        @RequestBody Transcript history,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {
        return handler.handleSendConversationHistory(authHeader, conversationId, history)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<ResourceResponse>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * UploadAttachment.
     *
     * @param conversationId    Conversation Id.
     * @param attachmentUpload  Attachment data.
     * @param authHeader        Authentication header.
     *
     * @return                  A ResourceResponse.
     */
    @PostMapping("v3/conversations/{conversationId}/attachments")
    public CompletableFuture<ResponseEntity<ResourceResponse>> uploadAttachment(
        @PathVariable String conversationId,
        @RequestBody AttachmentData attachmentUpload,
        @RequestHeader(value = "Authorization", defaultValue = "") String authHeader
    ) {
        return handler.handleUploadAttachment(authHeader, conversationId, attachmentUpload)
        .handle((result, exception) -> {
            if (exception == null) {
                if (result != null) {
                    return new ResponseEntity<ResourceResponse>(
                        result,
                        HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            logger.error("Exception handling message", exception);

            if (exception instanceof CompletionException) {
                if (exception.getCause() instanceof AuthenticationException) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                } else {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }
}
