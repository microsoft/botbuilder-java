package com.microsoft.bot.sample.suggestedactions;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.SuggestedActions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>This is where application specific logic for interacting with the users would be
 * added.  For this sample, the {@link #onMessageActivity(TurnContext)} displays a list
 * of SuggestedActions to the user.  The {@link #onMembersAdded(List, TurnContext)} will
 * send a greeting to new conversation participants.</p>
 */
@Component
public class SuggestedActionsBot extends ActivityHandler {
    public static final String WELCOMETEXT = "This bot will introduce you to suggestedActions."
     + " Please answer the question:";

    @Override
    protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {
        return sendWelcomeMessage(turnContext);
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        // Extract the text from the message activity the user sent.
        String text = turnContext.getActivity().getText().toLowerCase();

        // Take the input from the user and create the appropriate response.
        String responseText = processInput(text);

        // Respond to the user.
        return turnContext
            .sendActivities(
                MessageFactory.text(responseText),
                createSuggestedActions())
            .thenApply(responses -> null);
    }

    private CompletableFuture<Void> sendWelcomeMessage(TurnContext turnContext) {
        return turnContext.getActivity().getMembersAdded().stream()
            .filter(member -> !StringUtils.equals(member.getId(), turnContext.getActivity().getRecipient().getId()))
            .map(channel -> turnContext.sendActivities(
                MessageFactory.text("Welcome to SuggestedActionsBot " + channel.getName() + ". " + WELCOMETEXT),
                createSuggestedActions()
            ))
            .collect(CompletableFutures.toFutureList())
            .thenApply(resourceResponses -> null);
    }

    private String processInput(String text) {
        String colorText = "is the best color, I agree.";
        switch (text) {
            case "red":
                return "Red " + colorText;

            case "yellow":
                return "Yellow " + colorText;

            case "blue":
                return "Blue " + colorText;

            default:
                return "Please select a color from the suggested action choices";
        }
    }

    private Activity createSuggestedActions() {
        Activity reply = MessageFactory.text("What is your favorite color?");

        reply.setSuggestedActions(new SuggestedActions() {{
            setActions(Arrays.asList(
                new CardAction() {{
                    setTitle("Red");
                    setType(ActionTypes.IM_BACK);
                    setValue("Red");
                }},
                new CardAction() {{
                    setTitle("Yellow");
                    setType(ActionTypes.IM_BACK);
                    setValue("Yellow");
                }},
                new CardAction() {{
                    setTitle("Blue");
                    setType(ActionTypes.IM_BACK);
                    setValue("Blue");
                }}
            ));
        }});

        return reply;
    }
}
