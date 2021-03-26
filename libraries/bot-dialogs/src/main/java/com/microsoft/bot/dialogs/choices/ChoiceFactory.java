// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.HeroCard;
import com.microsoft.bot.schema.InputHints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Assists with formatting a message activity that contains a list of choices.
 */
public final class ChoiceFactory {
    private ChoiceFactory() {
    }

    /**
     * Creates an Activity that includes a list of choices formatted based on the
     * capabilities of a given channel.
     *
     * @param channelId A channel ID. The Connector.Channels class contains known
     *                  channel IDs.
     * @param list      The list of choices to include.
     * @param text      The text of the message to send. Can be null.
     * @return The created Activity
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static Activity forChannel(String channelId, List<Choice> list, String text) {
        return forChannel(channelId, list, text, null, null);
    }

    /**
     * Creates an Activity that includes a list of choices formatted based on the
     * capabilities of a given channel.
     *
     * @param channelId A channel ID. The Connector.Channels class contains known
     *                  channel IDs.
     * @param list      The list of choices to include.
     * @param text      The text of the message to send. Can be null.
     * @param speak     The text to be spoken by your bot on a speech-enabled
     *                  channel. Can be null.
     * @param options   The formatting options to use when rendering as a list. If
     *                  null, the default options are used.
     * @return The created Activity
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static Activity forChannel(String channelId, List<Choice> list, String text, String speak,
            ChoiceFactoryOptions options) {
        if (list == null) {
            list = new ArrayList<>();
        }

        // Find maximum title length
        int maxTitleLength = 0;
        for (Choice choice : list) {
            int l = choice.getAction() != null && !StringUtils.isEmpty(choice.getAction().getTitle())
                    ? choice.getAction().getTitle().length()
                    : choice.getValue().length();

            if (l > maxTitleLength) {
                maxTitleLength = l;
            }
        }

        // Determine list style
        boolean supportsSuggestedActions = Channel.supportsSuggestedActions(channelId, list.size());
        boolean supportsCardActions = Channel.supportsCardActions(channelId, list.size());
        int maxActionTitleLength = Channel.maxActionTitleLength(channelId);
        boolean longTitles = maxTitleLength > maxActionTitleLength;

        if (!longTitles && !supportsSuggestedActions && supportsCardActions) {
            // SuggestedActions is the preferred approach, but for channels that don't
            // support them (e.g. Teams, Cortana) we should use a HeroCard with CardActions
            return heroCard(list, text, speak);
        }

        if (!longTitles && supportsSuggestedActions) {
            // We always prefer showing choices using suggested actions. If the titles are
            // too long, however,
            // we'll have to show them as a text list.
            return suggestedAction(list, text, speak);
        }

        if (!longTitles && list.size() <= 3) {
            // If the titles are short and there are 3 or less choices we'll use an inline
            // list.
            return inline(list, text, speak, options);
        }

        // Show a numbered list.
        return list(list, text, speak, options);
    }

    /**
     * Creates an Activity that includes a list of choices formatted as an inline
     * list.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send. Can be null.
     * @return The created Activity.
     */
    public static Activity inline(List<Choice> choices, String text) {
        return inline(choices, text, null, null);
    }

    /**
     * Creates an Activity that includes a list of choices formatted as an inline
     * list.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send. Can be null.
     * @param speak   The text to be spoken by your bot on a speech-enabled channel.
     *                Cab be null.
     * @param options The formatting options to use when rendering as a list. Can be
     *                null.
     * @return The created Activity.
     */
    public static Activity inline(List<Choice> choices, String text, String speak, ChoiceFactoryOptions options) {
        if (choices == null) {
            choices = new ArrayList<>();
        }

        // clone options with defaults applied if needed.
        ChoiceFactoryOptions opt = new ChoiceFactoryOptions(options);

        // Format list of choices
        String connector = "";
        StringBuilder txtBuilder;
        if (StringUtils.isNotBlank(text)) {
            txtBuilder = new StringBuilder(text).append(' ');
        } else {
            txtBuilder = new StringBuilder().append(' ');
        }

        for (int index = 0; index < choices.size(); index++) {
            Choice choice = choices.get(index);
            String title = choice.getAction() != null && choice.getAction().getTitle() != null
                    ? choice.getAction().getTitle()
                    : choice.getValue();

            txtBuilder.append(connector);
            if (opt.getIncludeNumbers()) {
                txtBuilder.append('(').append(index + 1).append(") ");
            }

            txtBuilder.append(title);
            if (index == choices.size() - 2) {
                connector = index == 0 ? opt.getInlineOr() : opt.getInlineOrMore();
            } else {
                connector = opt.getInlineSeparator();
            }
        }

        // Return activity with choices as an inline list.
        return MessageFactory.text(txtBuilder.toString(), speak, InputHints.EXPECTING_INPUT);
    }

    /**
     * Creates a message activity that includes a list of choices formatted as a
     * numbered or bulleted list.
     *
     * @param choices The list of strings to include as Choices.
     * @return The created Activity.
     */
    public static Activity listFromStrings(List<String> choices) {
        return listFromStrings(choices, null, null, null);
    }

    /**
     * Creates a message activity that includes a list of choices formatted as a
     * numbered or bulleted list.
     *
     * @param choices The list of strings to include as Choices.
     * @param text    The text of the message to send.
     * @param speak   The text to be spoken by your bot on a speech-enabled channel.
     * @param options The formatting options to use when rendering as a list.
     * @return The created Activity.
     */
    public static Activity listFromStrings(List<String> choices, String text, String speak,
            ChoiceFactoryOptions options) {
        return list(toChoices(choices), text, speak, options);
    }

    /**
     * Creates a message activity that includes a list of choices formatted as a
     * numbered or bulleted list.
     *
     * @param choices The list of choices to include.
     * @return The created Activity.
     */
    public static Activity list(List<Choice> choices) {
        return list(choices, null, null, null);
    }

    /**
     * Creates a message activity that includes a list of choices formatted as a
     * numbered or bulleted list.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send.
     * @return The created Activity.
     */
    public static Activity list(List<Choice> choices, String text) {
        return list(choices, text, null, null);
    }

    /**
     * Creates a message activity that includes a list of choices formatted as a
     * numbered or bulleted list.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send.
     * @param speak   The text to be spoken by your bot on a speech-enabled channel.
     * @param options The formatting options to use when rendering as a list.
     * @return The created Activity.
     */
    public static Activity list(List<Choice> choices, String text, String speak, ChoiceFactoryOptions options) {
        if (choices == null) {
            choices = new ArrayList<>();
        }

        // clone options with defaults applied if needed.
        ChoiceFactoryOptions opt = new ChoiceFactoryOptions(options);

        boolean includeNumbers = opt.getIncludeNumbers();

        // Format list of choices
        String connector = "";
        StringBuilder txtBuilder = text == null ? new StringBuilder() : new StringBuilder(text).append("\n\n   ");

        for (int index = 0; index < choices.size(); index++) {
            Choice choice = choices.get(index);

            String title = choice.getAction() != null && !StringUtils.isEmpty(choice.getAction().getTitle())
                    ? choice.getAction().getTitle()
                    : choice.getValue();

            txtBuilder.append(connector);
            if (includeNumbers) {
                txtBuilder.append(index + 1).append(". ");
            } else {
                txtBuilder.append("- ");
            }

            txtBuilder.append(title);
            connector = "\n   ";
        }

        // Return activity with choices as a numbered list.
        return MessageFactory.text(txtBuilder.toString(), speak, InputHints.EXPECTING_INPUT);
    }

    /**
     * Creates an Activity that includes a list of card actions.
     *
     * @param choices The list of strings to include as actions.
     * @return The created Activity.
     */
    public static Activity suggestedActionFromStrings(List<String> choices) {
        return suggestedActionFromStrings(choices, null, null);
    }

    /**
     * Creates an Activity that includes a list of card actions.
     *
     * @param choices The list of strings to include as actions.
     * @param text    The text of the message to send.
     * @param speak   The text to be spoken by your bot on a speech-enabled channel.
     * @return The created Activity.
     */
    public static Activity suggestedActionFromStrings(List<String> choices, String text, String speak) {
        return suggestedAction(toChoices(choices), text, speak);
    }

    /**
     * Creates an Activity that includes a list of card actions.
     *
     * @param choices The list of choices to include.
     * @return The created Activity.
     */
    public static Activity suggestedAction(List<Choice> choices) {
        return suggestedAction(choices, null, null);
    }

    /**
     * Creates an Activity that includes a list of card actions.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send.
     * @return The created Activity.
     */
    public static Activity suggestedAction(List<Choice> choices, String text) {
        return suggestedAction(choices, text, null);
    }

    /**
     * Creates an Activity that includes a list of card actions.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send.
     * @param speak   The text to be spoken by your bot on a speech-enabled channel.
     * @return The created Activity.
     */
    public static Activity suggestedAction(List<Choice> choices, String text, String speak) {
        // Return activity with choices as suggested actions
        return MessageFactory.suggestedCardActions(extractActions(choices), text, speak, InputHints.EXPECTING_INPUT);
    }

    /**
     * Creates an Activity with a HeroCard based on a list of Choices.
     *
     * @param choices The list of choices to include.
     * @return The created Activity.
     */
    public static Activity heroCard(List<Choice> choices) {
        return heroCard(choices, null, null);
    }

    /**
     * Creates an Activity with a HeroCard based on a list of Choices.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send.
     * @return The created Activity.
     */
    public static Activity heroCard(List<Choice> choices, String text) {
        return heroCard(choices, text, null);
    }

    /**
     * Creates an Activity with a HeroCard based on a list of Choices.
     *
     * @param choices The list of choices to include.
     * @param text    The text of the message to send.
     * @param speak   The text to be spoken by your bot on a speech-enabled channel.
     * @return The created Activity.
     */
    public static Activity heroCard(List<Choice> choices, String text, String speak) {
        HeroCard card = new HeroCard();
        card.setText(text);
        card.setButtons(extractActions(choices));

        List<Attachment> attachments = new ArrayList<Attachment>() {
            /**
            *
            */
            private static final long serialVersionUID = 1L;

            {
                add(card.toAttachment());
            }
        };

        // Return activity with choices as HeroCard with buttons
        return MessageFactory.attachment(attachments, null, speak, InputHints.EXPECTING_INPUT);
    }

    /**
     * Returns a list of strings as a list of Choices.
     *
     * @param choices The list of strings to convert.
     * @return A List of Choices.
     */
    public static List<Choice> toChoices(List<String> choices) {
        return choices == null ? new ArrayList<>() : choices.stream().map(Choice::new).collect(Collectors.toList());
    }

    /**
     * Returns a list of strings as a list of Choices.
     *
     * @param choices The strings to convert.
     * @return A List of Choices.
     */
    public static List<Choice> toChoices(String... choices) {
        return toChoices(Arrays.asList(choices));
    }

    private static List<CardAction> extractActions(List<Choice> choices) {
        if (choices == null) {
            choices = new ArrayList<>();
        }

        // Map choices to actions
        return choices.stream().map(choice -> {
            if (choice.getAction() != null) {
                return choice.getAction();
            }

            CardAction card = new CardAction();
            card.setType(ActionTypes.IM_BACK);
            card.setValue(choice.getValue());
            card.setTitle(choice.getValue());
            return card;
        }).collect(Collectors.toList());
    }
}
