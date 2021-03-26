// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Contains methods for matching user input against a list of choices.
 */
public final class Find {
    private Find() { }

    /**
     * Matches user input against a list of strings.
     * @param utterance The input.
     * @param choices The list of choices.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> findChoicesFromStrings(
        String utterance,
        List<String> choices
    ) {
        return findChoicesFromStrings(utterance, choices, null);
    }

    /**
     * Matches user input against a list of strings.
     * @param utterance The input.
     * @param choices The list of choices.
     * @param options Optional, options to control the recognition strategy.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> findChoicesFromStrings(
        String utterance,
        List<String> choices,
        FindChoicesOptions options
    ) {
        if (choices == null) {
            throw new IllegalArgumentException("choices argument is missing");
        }

        return findChoices(utterance, choices.stream().map(Choice::new)
            .collect(Collectors.toList()), options);
    }

    /**
     * Matches user input against a list of Choices.
     * @param utterance The input.
     * @param choices The list of choices.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> findChoices(String utterance, List<Choice> choices) {
        return findChoices(utterance, choices, null);
    }

    /**
     * Matches user input against a list of Choices.
     * @param utterance The input.
     * @param choices The list of choices.
     * @param options Optional, options to control the recognition strategy.
     * @return A list of found choices, sorted by most relevant first.
     */
    public static List<ModelResult<FoundChoice>> findChoices(
        String utterance,
        List<Choice> choices,
        FindChoicesOptions options
    ) {
        if (choices == null) {
            throw new IllegalArgumentException("choices argument is missing");
        }

        FindChoicesOptions opt = options != null ? options : new FindChoicesOptions();

        // Build up full list of synonyms to search over.
        // - Each entry in the list contains the index of the choice it belongs to which will later be
        //   used to map the search results back to their choice.
        List<SortedValue> synonyms = new ArrayList<>();

        for (int index = 0; index < choices.size(); index++) {
            Choice choice = choices.get(index);

            if (!opt.isNoValue()) {
                synonyms.add(new SortedValue(choice.getValue(), index));
            }

            if (choice.getAction() != null && choice.getAction().getTitle() != null && !opt.isNoAction()) {
                synonyms.add(new SortedValue(choice.getAction().getTitle(), index));
            }

            if (choice.getSynonyms() != null) {
                for (String synonym : choice.getSynonyms()) {
                    synonyms.add(new SortedValue(synonym, index));
                }
            }
        }

        // Find synonyms in utterance and map back to their choices
        return findValues(utterance, synonyms, options).stream().map(v -> {
            Choice choice = choices.get(v.getResolution().getIndex());

            FoundChoice resolution = new FoundChoice();
            resolution.setValue(choice.getValue());
            resolution.setIndex(v.getResolution().getIndex());
            resolution.setScore(v.getResolution().getScore());
            resolution.setSynonym(v.getResolution().getValue());
            ModelResult<FoundChoice> modelResult = new ModelResult<FoundChoice>();
            modelResult.setStart(v.getStart());
            modelResult.setEnd(v.getEnd());
            modelResult.setTypeName("choice");
            modelResult.setText(v.getText());
            modelResult.setResolution(resolution);
            return modelResult;
        }).collect(Collectors.toList());
    }

    /**
     * This method is internal and should not be used.
     * @param utterance The input.
     * @param values The values.
     * @return A list of found values.
     */
    static List<ModelResult<FoundValue>> findValues(String utterance, List<SortedValue> values) {
        return findValues(utterance, values, null);
    }

    /**
     * This method is internal and should not be used.
     * @param utterance The input.
     * @param values The values.
     * @param options The options for the search.
     * @return A list of found values.
     */
    static List<ModelResult<FoundValue>> findValues(
        String utterance,
        List<SortedValue> values,
        FindValuesOptions options
    ) {
        // Sort values in descending order by length so that the longest value is searched over first.
        List<SortedValue> list = new ArrayList<>(values);
        list.sort((a, b) -> b.getValue().length() - a.getValue().length());

        // Search for each value within the utterance.
        List<ModelResult<FoundValue>> matches = new ArrayList<>();
        FindValuesOptions opt = options != null ? options : new FindValuesOptions();
        TokenizerFunction tokenizer = opt.getTokenizer() != null ? opt.getTokenizer() : new Tokenizer();
        List<Token> tokens = tokenizer.tokenize(utterance, opt.getLocale());
        int maxDistance = opt.getMaxTokenDistance();

        for (SortedValue entry : list) {
            // Find all matches for a value
            // - To match "last one" in "the last time I chose the last one" we need
            //   to re-search the String starting from the end of the previous match.
            // - The start & end position returned for the match are token positions.
            int startPos = 0;
            List<Token> searchedTokens = tokenizer.tokenize(entry.getValue().trim(), opt.getLocale());
            while (startPos < tokens.size()) {
                ModelResult<FoundValue> match = matchValue(
                    tokens,
                    maxDistance,
                    opt,
                    entry.getIndex(),
                    entry.getValue(),
                    searchedTokens,
                    startPos
                );
                if (match != null) {
                    startPos = match.getEnd() + 1;
                    matches.add(match);
                } else {
                    break;
                }
            }
        }

        // Sort matches by score descending
        matches.sort((a, b) -> Float.compare(b.getResolution().getScore(), a.getResolution().getScore()));

        // Filter out duplicate matching indexes and overlapping characters.
        // - The start & end positions are token positions and need to be translated to
        //   character positions before returning. We also need to populate the "text"
        //   field as well.
        List<ModelResult<FoundValue>> results = new ArrayList<>();
        Set<Integer> foundIndexes = new HashSet<>();
        Set<Integer> usedTokens = new HashSet<>();

        for (ModelResult<FoundValue> match : matches) {
            // Apply filters
            boolean add = !foundIndexes.contains(match.getResolution().getIndex());
            for (int i = match.getStart(); i <= match.getEnd(); i++) {
                if (usedTokens.contains(i)) {
                    add = false;
                    break;
                }
            }

            // Add to results
            if (add) {
                // Update filter info
                foundIndexes.add(match.getResolution().getIndex());

                for (int i = match.getStart(); i <= match.getEnd(); i++) {
                    usedTokens.add(i);
                }

                // Translate start & end and populate text field
                match.setStart(tokens.get(match.getStart()).getStart());
                match.setEnd(tokens.get(match.getEnd()).getEnd());
                match.setText(utterance.substring(match.getStart(), match.getEnd() + 1));
                results.add(match);
            }
        }

        // Return the results sorted by position in the utterance
        results.sort((a, b) -> a.getStart() - b.getStart());
        return results;
    }

    private static int indexOfToken(List<Token> tokens, Token token, int startPos) {
        for (int i = startPos; i < tokens.size(); i++) {
            if (StringUtils.equalsIgnoreCase(tokens.get(i).getNormalized(), token.getNormalized())) {
                return i;
            }
        }

        return -1;
    }

    private static ModelResult<FoundValue> matchValue(
        List<Token> sourceTokens,
        int maxDistance,
        FindValuesOptions options,
        int index,
        String value,
        List<Token> searchedTokens,
        int startPos
    ) {
        // Match value to utterance and calculate total deviation.
        // - The tokens are matched in order so "second last" will match in
        //   "the second from last one" but not in "the last from the second one".
        // - The total deviation is a count of the number of tokens skipped in the
        //   match so for the example above the number of tokens matched would be
        //   2 and the total deviation would be 1.
        int matched = 0;
        int totalDeviation = 0;
        int start = -1;
        int end = -1;
        for (Token token : searchedTokens) {
            // Find the position of the token in the utterance.
            int pos = indexOfToken(sourceTokens, token, startPos);
            if (pos >= 0) {
                // Calculate the distance between the current tokens position and the
                // previous tokens distance.
                int distance = matched > 0 ? pos - startPos : 0;
                if (distance <= maxDistance) {
                    // Update count of tokens matched and move start pointer to search
                    // for next token after the current token.
                    matched++;
                    totalDeviation += distance;
                    startPos = pos + 1;

                    // Update start & end position that will track the span of the utterance
                    // that's matched.
                    if (start < 0) {
                        start = pos;
                    }

                    end = pos;
                }
            }
        }

        // Calculate score and format result
        // - The start & end positions and the results text field will be corrected by the caller.
        ModelResult<FoundValue> result = null;

        if (matched > 0 && (matched == searchedTokens.size() || options.getAllowPartialMatches())) {
            // Percentage of tokens matched. If matching "second last" in
            // "the second from last one" the completeness would be 1.0 since
            // all tokens were found.
            int completeness = matched / searchedTokens.size();

            // Accuracy of the match. The accuracy is reduced by additional tokens
            // occurring in the value that weren't in the utterance. So an utterance
            // of "second last" matched against a value of "second from last" would
            // result in an accuracy of 0.5.
            float accuracy = (float) matched / (matched + totalDeviation);

            // The final score is simply the completeness multiplied by the accuracy.
            float score = completeness * accuracy;

            // Format result
            FoundValue resolution = new FoundValue();
            resolution.setValue(value);
            resolution.setIndex(index);
            resolution.setScore(score);
            result = new ModelResult<>();
            result.setStart(start);
            result.setEnd(end);
            result.setTypeName("value");
            result.setResolution(resolution);
        }

        return result;
    }
}
