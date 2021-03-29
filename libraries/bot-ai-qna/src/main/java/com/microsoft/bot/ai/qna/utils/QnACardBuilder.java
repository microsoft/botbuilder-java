// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna.utils;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.bot.ai.qna.dialogs.QnAMakerPrompt;
import com.microsoft.bot.ai.qna.models.QueryResult;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.HeroCard;

/**
 * Message activity card builder for QnAMaker dialogs.
 */
public final class QnACardBuilder {

    private QnACardBuilder() {
    }

    /**
     * Get active learning suggestions card.
     *
     * @param suggestionsList List of suggested questions.
     * @param cardTitle       Title of the cards
     * @param cardNoMatchText No match text.
     * @return Activity.
     */
    public static Activity getSuggestionsCard(List<String> suggestionsList, String cardTitle, String cardNoMatchText) {
        if (suggestionsList == null) {
            throw new IllegalArgumentException("suggestionsList");
        }

        if (cardTitle == null) {
            throw new IllegalArgumentException("cardTitle");
        }

        if (cardNoMatchText == null) {
            throw new IllegalArgumentException("cardNoMatchText");
        }

        Activity chatActivity = Activity.createMessageActivity();
        chatActivity.setText(cardTitle);
        List<CardAction> buttonList = new ArrayList<CardAction>();

        // Add all suggestions
        for (String suggestion : suggestionsList) {
            CardAction cardAction = new CardAction();
            cardAction.setValue(suggestion);
            cardAction.setType(ActionTypes.IM_BACK);
            cardAction.setTitle(suggestion);

            buttonList.add(cardAction);
        }

        // Add No match text
        CardAction cardAction = new CardAction();
        cardAction.setValue(cardNoMatchText);
        cardAction.setType(ActionTypes.IM_BACK);
        cardAction.setTitle(cardNoMatchText);
        buttonList.add(cardAction);

        HeroCard plCard = new HeroCard();
        plCard.setButtons(buttonList);

        // Create the attachment.
        Attachment attachment = plCard.toAttachment();

        chatActivity.setAttachment(attachment);

        return chatActivity;
    }

    /**
     * Get active learning suggestions card.
     *
     * @param result          Result to be dispalyed as prompts.
     * @param cardNoMatchText No match text.
     * @return Activity.
     */
    public static Activity getQnAPromptsCard(QueryResult result, String cardNoMatchText) {
        if (result == null) {
            throw new IllegalArgumentException("result");
        }

        if (cardNoMatchText == null) {
            throw new IllegalArgumentException("cardNoMatchText");
        }

        Activity chatActivity = Activity.createMessageActivity();
        chatActivity.setText(result.getAnswer());
        List<CardAction> buttonList = new ArrayList<CardAction>();

        // Add all prompt
        for (QnAMakerPrompt prompt : result.getContext().getPrompts()) {
            CardAction card = new CardAction();
            card.setValue(prompt.getDisplayText());
            card.setType(ActionTypes.IM_BACK);
            card.setTitle(prompt.getDisplayText());
            buttonList.add(card);
        }

        HeroCard plCard = new HeroCard();
        plCard.setButtons(buttonList);

        // Create the attachment.
        Attachment attachment = plCard.toAttachment();

        chatActivity.setAttachment(attachment);

        return chatActivity;
    }
}
