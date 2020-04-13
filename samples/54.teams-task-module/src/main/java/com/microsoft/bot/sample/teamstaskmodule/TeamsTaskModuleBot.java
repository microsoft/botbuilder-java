// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamstaskmodule;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.BotFrameworkAdapter;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.builder.teams.TeamsInfo;
import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.teams.TaskModuleAction;
import com.microsoft.bot.schema.ConversationParameters;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.HeroCard;
import com.microsoft.bot.schema.Mention;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import com.microsoft.bot.schema.Attachment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.json.*;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>This is where application specific logic for interacting with the users would be
 * added.  For this sample, the {@link #onMessageActivity(TurnContext)} echos the text
 * back to the user.  The {@link #onMembersAdded(List, TurnContext)} will send a greeting
 * to new conversation participants.</p>
 */
@Component
public class TeamsTaskModuleBot extends TeamsActivityHandler {
    private String appId;
    private String appPassword;

    public TeamsTaskModuleBot(Configuration configuration) {
        appId = configuration.getProperty("MicrosoftAppId");
        appPassword = configuration.getProperty("MicrosoftAppPassword");
    }

    @Override
    public CompletableFuture<Void> onTeamsMembersAdded(
        List<TeamsChannelAccount> membersAdded,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return turnContext.sendActivity(MessageFactory.attachment(getTaskModuleHeroCard()))
            .thenApply(resourceResponse -> null);
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        return turnContext.sendActivity(MessageFactory.attachment(getTaskModuleHeroCard()))
            .thenApply(resourceResponse -> null);
    }

    private Attachment getTaskModuleHeroCard()
    {
        HeroCard card =  new HeroCard() {{
        setTitle("");
        setSubtitle("Click the buttons below to update this card");
        setButtons(Arrays.asList(
            new TaskModuleAction("Adaptive Card", "adaptivecard")
            ));
        }};
        return card.toAttachment();
    }
}
