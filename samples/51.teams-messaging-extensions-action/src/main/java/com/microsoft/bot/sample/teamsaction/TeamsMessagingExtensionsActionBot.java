// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamsaction;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.HeroCard;
import com.microsoft.bot.schema.teams.*;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>This is where application specific logic for interacting with the users would be
 * added.  For this sample, the {@link #onMessageActivity(TurnContext)} echos the text
 * back to the user.  The {@link #onMembersAdded(List, TurnContext)} will send a greeting
 * to new conversation participants.</p>
 */
@Component
public class TeamsMessagingExtensionsActionBot extends TeamsActivityHandler {
    private String appId;
    private String appPassword;

    public TeamsMessagingExtensionsActionBot(Configuration configuration) {
        appId = configuration.getProperty("MicrosoftAppId");
        appPassword = configuration.getProperty("MicrosoftAppPassword");
    }

    @Override
    protected CompletableFuture<MessagingExtensionActionResponse> onTeamsMessagingExtensionSubmitAction(
        TurnContext turnContext,
        MessagingExtensionAction action) {
        switch (action.getCommandId()) {
            // These commandIds are defined in the Teams App Manifest.
            case "createCard":
                return createCardCommand(turnContext, action);

            case "shareMessage":
                return shareMessageCommand(turnContext, action);
            default:
                return notImplemented(String.format("Invalid CommandId: %s", action.getCommandId()));
        }
    }

    private CompletableFuture<MessagingExtensionActionResponse> createCardCommand(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {
        LinkedHashMap actionData = (LinkedHashMap) action.getData();

        HeroCard card = new HeroCard() {{
            setTitle((String) actionData.get("title"));
            setSubtitle((String) actionData.get("subTitle"));
            setText((String) actionData.get("text"));
        }};

        List<MessagingExtensionAttachment> attachments = new ArrayList<MessagingExtensionAttachment>();
        attachments.add(new MessagingExtensionAttachment(){{
            setContent(card);
            setContentType(HeroCard.CONTENTTYPE);
            setPreview(card.toAttachment());
        }});

        return CompletableFuture.completedFuture(new MessagingExtensionActionResponse(){{
            setComposeExtension(new MessagingExtensionResult(){{
                setAttachmentLayout("list");
                setType("result");
                setAttachments(attachments);
            }});
        }});
    }

    private CompletableFuture<MessagingExtensionActionResponse> shareMessageCommand(
        TurnContext turnContext,
        MessagingExtensionAction action
    ) {

        HeroCard card = new HeroCard() {{
            setTitle("Test");
            setText("Test");
        }};

        if (action.getMessagePayload().getAttachments() != null && !action.getMessagePayload().getAttachments().isEmpty()) {
            card.setSubtitle("Test");
        }

        return CompletableFuture.completedFuture(new MessagingExtensionActionResponse(){{
            setComposeExtension(new MessagingExtensionResult(){{
                setAttachmentLayout("list");
                setType("result");
                setAttachments(Arrays.asList(new MessagingExtensionAttachment(){{
                    setContent(card);
                    setContentType(HeroCard.CONTENTTYPE);
                    setPreview(card.toAttachment());
                }}));
            }});
        }});
    }
}
