// CHECKSTYLE:OFF
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.inspection;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InspectionMiddleware extends InterceptionMiddleware {
    private static final String COMMAND = "/INSPECT";

    private InspectionState inspectionState;
    private UserState userState;
    private ConversationState conversationState;
    private MicrosoftAppCredentials credentials;

    public InspectionMiddleware(InspectionState withInspectionState) {
        this(withInspectionState, null, null, null);
    }

    public InspectionMiddleware(
        InspectionState withInspectionState,
        UserState withUserState,
        ConversationState withConversationState,
        MicrosoftAppCredentials withCredentials
    ) {
        super(LoggerFactory.getLogger(InspectionMiddleware.class));

        inspectionState = withInspectionState;
        userState = withUserState;
        conversationState = withConversationState;
        credentials = withCredentials != null ? withCredentials : MicrosoftAppCredentials.empty();
    }

    public CompletableFuture<Boolean> processCommand(TurnContext turnContext) {
        if (
            !turnContext.getActivity().isType(ActivityTypes.MESSAGE)
                || StringUtils.isEmpty(turnContext.getActivity().getText())
        ) {

            return CompletableFuture.completedFuture(false);
        }

        String text = Activity.removeRecipientMentionImmutable(turnContext.getActivity());
        String[] command = text.split(" ");

        if (command.length > 1 && StringUtils.equals(command[0], COMMAND)) {
            if (command.length == 2 && StringUtils.equals(command[1], "open")) {
                return processOpenCommand(turnContext).thenApply((result) -> true);
            }

            if (command.length == 3 && StringUtils.equals(command[1], "attach")) {
                return processAttachCommand(turnContext, command[2]).thenApply((result) -> true);
            }
        }

        return CompletableFuture.completedFuture(false);
    }

    @Override
    protected CompletableFuture<Intercept> inbound(TurnContext turnContext, Activity activity) {
        return processCommand(turnContext).thenCompose(processResult -> {
            if (processResult) {
                return CompletableFuture.completedFuture(new Intercept(false, false));
            }

            return findSession(turnContext).thenCompose(session -> {
                if (session == null) {
                    return CompletableFuture.completedFuture(new Intercept(true, false));
                }

                return invokeSend(turnContext, session, activity).thenCompose(invokeResult -> {
                    if (invokeResult) {
                        return CompletableFuture.completedFuture(new Intercept(true, true));
                    }
                    return CompletableFuture.completedFuture(new Intercept(true, false));
                });
            });
        });
    }

    @Override
    protected CompletableFuture<Void> outbound(
        TurnContext turnContext,
        List<Activity> clonedActivities
    ) {
        return findSession(turnContext).thenCompose(session -> {
            if (session != null) {
                List<CompletableFuture<Boolean>> sends = new ArrayList<>();

                for (Activity traceActivity : clonedActivities) {
                    sends.add(invokeSend(turnContext, session, traceActivity));
                }

                return CompletableFuture.allOf(sends.toArray(new CompletableFuture[sends.size()]));
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    protected CompletableFuture<Void> traceState(TurnContext turnContext) {
        return findSession(turnContext).thenCompose(session -> {
            if (session == null) {
                return CompletableFuture.completedFuture(null);
            }

            CompletableFuture<Void> userLoad = userState == null
                ? CompletableFuture.completedFuture(null)
                : userState.load(turnContext);

            CompletableFuture<Void> conversationLoad = conversationState == null
                ? CompletableFuture.completedFuture(null)
                : conversationState.load(turnContext);

            return CompletableFuture.allOf(userLoad, conversationLoad).thenCompose(loadResult -> {
                ObjectNode botState = JsonNodeFactory.instance.objectNode();
                if (userState != null) {
                    botState.set("userState", userState.get(turnContext));
                }

                if (conversationState != null) {
                    botState.set("conversationState", conversationState.get(turnContext));
                }

                return invokeSend(
                    turnContext,
                    session,
                    InspectionActivityExtensions.traceActivity(botState)
                ).thenCompose(invokeResult -> CompletableFuture.completedFuture(null));
            });
        });
    }

    private CompletableFuture<Void> processOpenCommand(TurnContext turnContext) {
        StatePropertyAccessor<InspectionSessionsByStatus> accessor = inspectionState.createProperty(
            InspectionSessionsByStatus.class.getName()
        );

        return accessor.get(turnContext, InspectionSessionsByStatus::new).thenCompose(result -> {
            InspectionSessionsByStatus sessions = (InspectionSessionsByStatus) result;
            String sessionId = openCommand(
                sessions,
                turnContext.getActivity().getConversationReference()
            );

            String command = String.format("%s attach %s", COMMAND, sessionId);
            return turnContext.sendActivity(
                InspectionActivityExtensions.makeCommandActivity(command)
            );
        }).thenCompose(resourceResponse -> inspectionState.saveChanges(turnContext));
    }

    private CompletableFuture<Void> processAttachCommand(
        TurnContext turnContext,
        String sessionId
    ) {
        StatePropertyAccessor<InspectionSessionsByStatus> accessor = inspectionState.createProperty(
            InspectionSessionsByStatus.class.getName()
        );

        return accessor.get(turnContext, InspectionSessionsByStatus::new).thenCompose(sessions -> {
            if (
                attachCommand(
                    turnContext.getActivity().getConversation().getId(),
                    sessions,
                    sessionId
                )
            ) {
                return turnContext.sendActivity(
                    MessageFactory.text(
                        "Attached to session, all traffic is being replicated for inspection."
                    )
                );
            } else {
                return turnContext.sendActivity(
                    MessageFactory.text(
                        String.format("Open session with id %s does not exist.", sessionId)
                    )
                );
            }
        }).thenCompose(resourceResponse -> inspectionState.saveChanges(turnContext));
    }

    private String openCommand(
        InspectionSessionsByStatus sessions,
        ConversationReference conversationReference
    ) {
        String sessionId = UUID.randomUUID().toString();
        sessions.getOpenedSessions().put(sessionId, conversationReference);
        return sessionId;
    }

    private boolean attachCommand(
        String conversationId,
        InspectionSessionsByStatus sessions,
        String sessionId
    ) {
        ConversationReference inspectionSessionState = sessions.getOpenedSessions().get(sessionId);
        if (inspectionSessionState == null)
            return false;

        sessions.getAttachedSessions().put(conversationId, inspectionSessionState);
        sessions.getOpenedSessions().remove(sessionId);

        return true;
    }

    protected InspectionSession createSession(
        ConversationReference reference,
        MicrosoftAppCredentials credentials
    ) {
        return new InspectionSession(reference, credentials);
    }

    private CompletableFuture<InspectionSession> findSession(TurnContext turnContext) {
        StatePropertyAccessor<InspectionSessionsByStatus> accessor = inspectionState.createProperty(
            InspectionSessionsByStatus.class.getName()
        );

        return accessor.get(turnContext, InspectionSessionsByStatus::new).thenApply(
            openSessions -> {
                ConversationReference reference = openSessions.getAttachedSessions().get(
                    turnContext.getActivity().getConversation().getId()
                );

                if (reference != null) {
                    return createSession(reference, credentials);
                }

                return null;
            }
        );
    }

    private CompletableFuture<Boolean> invokeSend(
        TurnContext turnContext,
        InspectionSession session,
        Activity activity
    ) {

        return session.send(activity).thenCompose(result -> {
            if (result) {
                return CompletableFuture.completedFuture(true);
            }

            return cleanupSession(turnContext).thenCompose(
                cleanupResult -> CompletableFuture.completedFuture(false)
            );
        });
    }

    private CompletableFuture<Void> cleanupSession(TurnContext turnContext) {
        StatePropertyAccessor<InspectionSessionsByStatus> accessor = inspectionState.createProperty(
            InspectionSessionsByStatus.class.getName()
        );

        return accessor.get(turnContext, InspectionSessionsByStatus::new).thenCompose(result -> {
            InspectionSessionsByStatus openSessions = (InspectionSessionsByStatus) result;
            openSessions.getAttachedSessions().remove(
                turnContext.getActivity().getConversation().getId()
            );
            return inspectionState.saveChanges(turnContext);
        });
    }
}
