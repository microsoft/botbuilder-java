// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.microsoft.recognizers.text.IModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.microsoft.recognizers.text.number.NumberRecognizer;

/**
 * Contains methods for matching user input against a list of choices.
 */
public final class ChoiceRecognizers {
    private ChoiceRecognizers() { }

    /**
     * Matches user input against a list of choices.
     * @param utterance The input.
     * @param choices The list of string choices.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> recognizeChoicesFromStrings(String utterance, List<String> choices) {
        return recognizeChoicesFromStrings(utterance, choices, null);
    }

    /**
     * Matches user input against a list of choices.
     * @param utterance The input.
     * @param choices The list of string choices.
     * @param options Optional, options to control the recognition strategy.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> recognizeChoicesFromStrings(
        String utterance,
        List<String> choices,
        FindChoicesOptions options
    ) {
        return recognizeChoices(utterance, choices.stream().map(Choice::new)
            .collect(Collectors.toList()), options);
    }

    /**
     * Matches user input against a list of choices.
     * @param utterance The input.
     * @param choices The list of string choices.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> recognizeChoices(String utterance, List<Choice> choices) {
        return recognizeChoices(utterance, choices, null);
    }

    /**
     * Matches user input against a list of choices.
     * @param utterance The input.
     * @param choices The list of string choices.
     * @param options Optional, options to control the recognition strategy.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> recognizeChoices(
        String utterance,
        List<Choice> choices,
        FindChoicesOptions options
    ) {
        // Try finding choices by text search first
        // - We only want to use a single strategy for returning results to avoid issues where utterances
        //   like the "the third one" or "the red one" or "the first division book" would miss-recognize as
        //   a numerical index or ordinal as well.
        String locale = options != null ? options.getLocale() : Locale.ENGLISH.getDisplayName();
        List<ModelResult<FoundChoice>> matched = Find.findChoices(utterance, choices, options);
        if (matched.size() == 0) {
            List<ModelResult<FoundChoice>> matches = new ArrayList<>();
            if (options == null || options.isRecognizeOrdinals()) {
                // Next try finding by ordinal
                matches = recognizeNumbers(utterance, new NumberRecognizer(locale).getOrdinalModel(locale, true));
                for (ModelResult<FoundChoice> match : matches) {
                    matchChoiceByIndex(choices, matched, match);
                }
            }

            if (matches.size() == 0 && (options == null || options.isRecognizeNumbers())) {
                // Then try by numerical index
                matches = recognizeNumbers(utterance, new NumberRecognizer(locale).getNumberModel(locale, true));
                for (ModelResult<FoundChoice> match : matches) {
                    matchChoiceByIndex(choices, matched, match);
                }
            }

            // Sort any found matches by their position within the utterance.
            // - The results from findChoices() are already properly sorted so we just need this
            //   for ordinal & numerical lookups.
            matched.sort(Comparator.comparingInt(ModelResult::getStart));
        }

        return matched;
    }

    private static void matchChoiceByIndex(
        List<Choice> list,
        List<ModelResult<FoundChoice>> matched,
        ModelResult<FoundChoice> match
    ) {
        try {
            // converts Resolution Values containing "end" (e.g. utterance "last") in numeric values.
            String value = match.getResolution().getValue().replace("end", Integer.toString(list.size()));
            int index = Integer.parseInt(value) - 1;
            if (index >= 0 && index < list.size()) {
                Choice choice = list.get(index);
                FoundChoice resolution = new FoundChoice();
                resolution.setValue(choice.getValue());
                resolution.setIndex(index);
                resolution.setScore(1.0f);
                ModelResult<FoundChoice> modelResult = new ModelResult<FoundChoice>();
                modelResult.setStart(match.getStart());
                modelResult.setEnd(match.getEnd());
                modelResult.setTypeName("choice");
                modelResult.setText(match.getText());
                modelResult.setResolution(resolution);
                matched.add(modelResult);
            }
        } catch (Throwable ignored) {
            // noop here, as in dotnet/node repos
        }
    }

    private static List<ModelResult<FoundChoice>> recognizeNumbers(String utterance, IModel model) {
        List<com.microsoft.recognizers.text.ModelResult> result = model.parse(utterance == null ? "" : utterance);
        return result.stream().map(r -> {
            FoundChoice resolution = new FoundChoice();
            resolution.setValue(r.resolution.get("value").toString());
            ModelResult<FoundChoice> modelResult = new ModelResult<FoundChoice>();
            modelResult.setStart(r.start);
            modelResult.setEnd(r.end);
            modelResult.setText(r.text);
            modelResult.setResolution(resolution);
            return modelResult;
        }).collect(Collectors.toList());
    }
}
