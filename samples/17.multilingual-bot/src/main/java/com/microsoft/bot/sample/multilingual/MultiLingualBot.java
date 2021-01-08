// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.sample.multilingual;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.SuggestedActions;
import com.microsoft.bot.schema.Attachment;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This bot demonstrates how to use Microsoft Translator.
 * More information can be found
 * here https://docs.microsoft.com/en-us/azure/cognitive-services/translator/translator-info-overview.
 */
public class MultiLingualBot extends ActivityHandler {
    private static final String WELCOME_TEXT =
        new StringBuilder("This bot will introduce you to translation middleware. ")
        .append("Say 'hi' to get started.").toString();

    private static final String ENGLISH_ENGLISH = "en";
    private static final String ENGLISH_SPANISH = "es";
    private static final String SPANISH_ENGLISH = "in";
    private static final String SPANISH_SPANISH = "it";

    private UserState userState;
    private StatePropertyAccessor<String> languagePreference;

    /**
     * Creates a Multilingual bot.
     * @param withUserState User state object.
     */
    public MultiLingualBot(UserState withUserState) {
        if (withUserState != null) {
            this.userState = withUserState;
        } else {
            throw new IllegalArgumentException("userState");
        }

        this.languagePreference = userState.createProperty("LanguagePreference");
    }

    /**
     * This method is executed when a user is joining to the conversation.
     * @param membersAdded A list of all the members added to the conversation,
     *                     as described by the conversation update activity.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded,
                                                     TurnContext turnContext) {
        return this.sendWelcomeMessage(turnContext);
    }

    /**
     * This method is executed when the turnContext receives a message activity.
     * @param turnContext The context object for this turn.
     * @return A task that represents the work queued to execute.
     */
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        if (MultiLingualBot.isLanguageChangeRequested(turnContext.getActivity().getText())) {
            String currentLang = turnContext.getActivity().getText().toLowerCase();
            String lang = currentLang.equals(ENGLISH_ENGLISH) || currentLang.equals(SPANISH_ENGLISH)
                ? ENGLISH_ENGLISH : ENGLISH_SPANISH;

            // If the user requested a language change through the suggested actions with values "es" or "en",
            // simply change the user's language preference in the user state.
            // The translation middleware will catch this setting and translate both ways to the user's
            // selected language.
            // If Spanish was selected by the user, the reply below will actually be shown in spanish to the user.
            return languagePreference.set(turnContext, lang)
                .thenCompose(task -> {
                    Activity reply = MessageFactory.text(String.format("Your current language code is: %s", lang));
                    return turnContext.sendActivity(reply);
                })
                // Save the user profile updates into the user state.
                .thenCompose(task -> userState.saveChanges(turnContext, false));
        } else {
            // Show the user the possible options for language. If the user chooses a different language
            // than the default, then the translation middleware will pick it up from the user state and
            // translate messages both ways, i.e. user to bot and bot to user.
            Activity reply = MessageFactory.text("Choose your language:");
            CardAction esAction = new CardAction() {
                {
                    setTitle("Español");
                    setType(ActionTypes.POST_BACK);
                    setValue(ENGLISH_SPANISH);
                }
            };
            CardAction enAction = new CardAction() {
                {
                    setTitle("English");
                    setType(ActionTypes.POST_BACK);
                    setValue(ENGLISH_ENGLISH);
                }
            };
            List<CardAction> actions = new ArrayList<>(Arrays.asList(esAction, enAction));
            SuggestedActions suggestedActions = new SuggestedActions() {
                {
                    setActions(actions);
                }
            };
            reply.setSuggestedActions(suggestedActions);
            return turnContext.sendActivity(reply).thenApply(resourceResponse -> null);
        }
    }

    private static CompletableFuture<Void> sendWelcomeMessage(TurnContext turnContext) {
        for (ChannelAccount member: turnContext.getActivity().getMembersAdded()) {
            // Greet anyone that was not the target (recipient) of this message.
            // To learn more about Adaptive Cards, see https://aka.ms/msbot-adaptivecards for more details.
            if (member.getId() != turnContext.getActivity().getRecipient().getId()) {
                Attachment welcomeCard = MultiLingualBot.createAdaptiveCardAttachment();
                Activity response = MessageFactory.attachment(welcomeCard);
                return turnContext.sendActivity(response)
                    .thenCompose(task -> turnContext.sendActivity(MessageFactory.text(WELCOME_TEXT))
                    .thenApply(resourceResponse -> null));
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Load attachment from file.
     * @return the welcome adaptive card
     */
    private static Attachment createAdaptiveCardAttachment() {
        // combine path for cross platform support
        try {
            Path path = Paths.get(".", "cards", "welcomeCard.json");
            BufferedReader reader = Files.newBufferedReader(path);
            String adaptiveCard = reader.readLine();
            ObjectMapper mapper = new ObjectMapper();
            return new Attachment() {
                {
                    setContentType("application/vnd.microsoft.card.adaptive");
                    setContent(mapper.readValue(adaptiveCard, String.class));
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks whether the utterance from the user is requesting a language change.
     * In a production bot, we would use the Microsoft Text Translation API language
     * detection feature, along with detecting language names.
     * For the purpose of the sample, we just assume that the user requests language
     * changes by responding with the language code through the suggested action presented
     * above or by typing it.
     * @param utterance utterance the current turn utterance.
     * @return the utterance.
     */
    private static Boolean isLanguageChangeRequested(String utterance) {
        if (utterance == null || utterance.isEmpty()) {
            return false;
        }

        utterance = utterance.toLowerCase().trim();
        return utterance.equals(ENGLISH_SPANISH) || utterance.equals(ENGLISH_ENGLISH)
            || utterance.equals(SPANISH_SPANISH) || utterance.equals(SPANISH_ENGLISH);
    }
}
