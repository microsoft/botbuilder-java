// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamstaskmodule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.HeroCard;
import com.microsoft.bot.schema.teams.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
public class TeamsTaskModuleBot extends TeamsActivityHandler {
    private String appId;
    private String appPassword;

    public TeamsTaskModuleBot(Configuration configuration) {
        appId = configuration.getProperty("MicrosoftAppId");
        appPassword = configuration.getProperty("MicrosoftAppPassword");
    }

    @Override
    protected CompletableFuture<Void> onTeamsMembersAdded(
        List<TeamsChannelAccount> membersAdded,
        TeamInfo teamInfo,
        TurnContext turnContext
    ) {
        return turnContext.sendActivity(MessageFactory.attachment(getTaskModuleHeroCard()))
            .thenApply(resourceResponse -> null);
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(
        TurnContext turnContext) {
        Attachment attachment = getTaskModuleHeroCard();
        return turnContext.sendActivity(MessageFactory.attachment(attachment))
            .thenApply(resourceResponse -> null);
    }

    @Override
    protected CompletableFuture<TaskModuleResponse> onTeamsTaskModuleFetch(
        TurnContext turnContext,
        TaskModuleRequest taskModuleRequest) {

        Activity reply = MessageFactory.text("onTeamsTaskModuleFetch TaskModuleRequest: " );

        turnContext.sendActivity(reply)
            .thenApply(resourceResponse -> null);

        Attachment adaptiveCard = getTaskModuleAdaptiveCard();

        return CompletableFuture.completedFuture(new TaskModuleResponse(){{
            setTask(new TaskModuleContinueResponse(){{
                setType("continue");
                setValue(new TaskModuleTaskInfo(){{
                    setCard(adaptiveCard);
                    setHeight(200);
                    setWidth(400);
                    setTitle("Adaptive Card: Inputs");
                }});
            }});
        }});
    }

    @Override
    protected CompletableFuture<TaskModuleResponse> onTeamsTaskModuleSubmit(
        TurnContext turnContext,
        TaskModuleRequest taskModuleRequest){

        Activity reply = MessageFactory.text("onTeamsTaskModuleSubmit TaskModuleRequest: " );

        turnContext.sendActivity(reply)
            .thenApply(resourceResponse -> null);

        return CompletableFuture.completedFuture(new TaskModuleResponse(){{
            setTask(new TaskModuleMessageResponse(){{
                setType("message");
                setValue("Thanks!");
            }});
        }});

    }

    private Attachment getTaskModuleHeroCard()
    {
        HeroCard card =  new HeroCard() {{
        setTitle("");
        setSubtitle("Click the buttons below to update this card");
        setButtons(Arrays.asList(
            new TaskModuleAction("Adaptive Card", new JSONObject().put("data", "adaptivecard").toString())
            ));
        }};
        return card.toAttachment();
    }

    private Attachment getTaskModuleAdaptiveCard(){
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("adaptivecard.json");
            String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            return new Attachment(){{
                setContentType("application/vnd.microsoft.card.adaptive");
                setContent(new ObjectMapper().readValue((String) result, ObjectNode.class));
            }};
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Attachment();
    }
}
