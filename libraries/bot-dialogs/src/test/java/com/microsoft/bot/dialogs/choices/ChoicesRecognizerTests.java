// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ChoicesRecognizerTests {

    private static List<String> colorChoices = Arrays.asList("red", "green", "blue");
    private static List<String> overlappingChoices = Arrays
        .asList("bread", "bread pudding", "pudding");

    private static List<SortedValue> colorValues = Arrays.asList(
        new SortedValue("red", 0),
        new SortedValue("green", 1),
        new SortedValue("blue", 2)
    );

    private static List<SortedValue> overlappingValues = Arrays.asList(
        new SortedValue("bread", 0),
        new SortedValue("bread pudding", 1),
        new SortedValue("pudding", 2)
    );

    private static List<SortedValue> similarValues = Arrays.asList(
        new SortedValue("option A", 0),
        new SortedValue("option B", 1),
        new SortedValue("option C", 2)
    );

    //
    // FindChoices
    //

    @Test
    public void shouldFindASimpleValueInAnSingleWordUtterance() {
        List<ModelResult<FoundValue>> found = Find.findValues("red", colorValues);
        Assert.assertEquals(1, found.size());
        assertResult(found.get(0), 0, 2, "red");
        assertValue(found.get(0), "red", 0, 1.0f);
    }

    @Test
    public void shouldFindASimpleValueInAnUtterance() {
        List<ModelResult<FoundValue>> found = Find.findValues("the red one please.", colorValues);
        Assert.assertEquals(1, found.size());
        assertResult(found.get(0), 4, 6, "red");
        assertValue(found.get(0), "red", 0, 1.0f);
    }

    @Test
    public void shouldFindMultipleValuesWithinAnUtterance() {
        List<ModelResult<FoundValue>> found = Find.findValues("the red and blue ones please.", colorValues);
        Assert.assertEquals(2, found.size());
        assertResult(found.get(0), 4, 6, "red");
        assertValue(found.get(0), "red", 0, 1.0f);
        assertValue(found.get(1), "blue", 2, 1.0f);
    }

    @Test
    public void shouldFindMultipleValuesThatOverlap() {
        List<ModelResult<FoundValue>> found = Find.findValues("the bread pudding and bread please.", overlappingValues);
        Assert.assertEquals(2, found.size());
        assertResult(found.get(0), 4, 16, "bread pudding");
        assertValue(found.get(0), "bread pudding", 1, 1.0f);
        assertValue(found.get(1), "bread", 0, 1.0f);
    }

    @Test
    public void shouldCorrectlyDisambiguateBetweenVerySimilarValues() {
        FindChoicesOptions options = new FindChoicesOptions();
        options.setAllowPartialMatches(true);
        List<ModelResult<FoundValue>> found = Find.findValues("option B", similarValues, options);
        Assert.assertEquals(1, found.size());
        assertValue(found.get(0), "option B", 1, 1.0f);
    }

    @Test
    public void shouldFindASingleChoiceInAnUtterance() {
        List<ModelResult<FoundChoice>> found = Find.findChoicesFromStrings("the red one please.", colorChoices);
        Assert.assertEquals(1, found.size());
        assertResult(found.get(0), 4, 6, "red");
        assertChoice(found.get(0), "red", 0, 1.0f, null);
    }

    @Test
    public void shouldFindMultipleChoicesWithinAnUtterance() {
        List<ModelResult<FoundChoice>> found = Find.findChoicesFromStrings("the red and blue ones please.", colorChoices);
        Assert.assertEquals(2, found.size());
        assertResult(found.get(0), 4, 6, "red");
        assertChoice(found.get(0), "red", 0, 1.0f, null);
        assertChoice(found.get(1), "blue", 2, 1.0f, null);
    }

    @Test
    public void shouldFindMultipleChoicesThatOverlap() {
        List<ModelResult<FoundChoice>> found = Find.findChoicesFromStrings("the bread pudding and bread please.", overlappingChoices);
        Assert.assertEquals(2, found.size());
        assertResult(found.get(0), 4, 16, "bread pudding");
        assertChoice(found.get(0), "bread pudding", 1, 1.0f, null);
        assertChoice(found.get(1), "bread", 0, 1.0f, null);
    }

    @Test
    public void shouldAcceptNullUtteranceInFindChoices() {
        List<ModelResult<FoundChoice>> found = Find.findChoicesFromStrings(null, colorChoices);
        Assert.assertEquals(0, found.size());
    }

    //
    // RecognizeChoices
    //

    @Test
    public void shouldFindAChoiceInAnUtteranceByName() {
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings("the red one please", colorChoices);
        Assert.assertEquals(1, found.size());
        assertResult(found.get(0), 4, 6, "red");
        assertChoice(found.get(0), "red", 0, 1.0f, null);
    }

    @Test
    public void shouldFindAChoiceInAnUtteranceByOrdinalPosition() {
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings("the first one please.", colorChoices);
        Assert.assertEquals(1, found.size());
        assertResult(found.get(0), 4, 8, "first");
        assertChoice(found.get(0), "red", 0, 1.0f, null);
    }

    @Test
    public void shouldFindMultipleChoicesInAnUtteranceByOrdinalPosition() {
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings("the first and third one please.", colorChoices);
        Assert.assertEquals(2, found.size());
        assertChoice(found.get(0), "red", 0, 1.0f, null);
        assertChoice(found.get(1), "blue", 2, 1.0f, null);
    }

    @Test
    public void shouldFindAChoiceInAnUtteranceByNumericalIndex_digit() {
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings("1", colorChoices);
        Assert.assertEquals(1, found.size());
        assertResult(found.get(0), 0, 0, "1");
        assertChoice(found.get(0), "red", 0, 1.0f, null);
    }

    @Test
    public void shouldFindAChoiceInAnUtteranceByNumericalIndex_Text() {
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings("one", colorChoices);
        Assert.assertEquals(1, found.size());
        assertResult(found.get(0), 0, 2, "one");
        assertChoice(found.get(0), "red", 0, 1.0f, null);
    }

    @Test
    public void shouldFindMultipleChoicesInAnUtteranceByNumerical_index() {
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings("option one and 3.", colorChoices);
        Assert.assertEquals(2, found.size());
        assertChoice(found.get(0), "red", 0, 1.0f, null);
        assertChoice(found.get(1), "blue", 2, 1.0f, null);
    }

    @Test
    public void shouldAcceptNullUtteranceInRecognizeChoices() {
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings(null, colorChoices);
        Assert.assertEquals(0, found.size());
    }

    @Test
    public void shouldNOTFindAChoiceInAnUtteranceByOrdinalPosition_RecognizeOrdinalsFalseAndRecognizeNumbersFalse() {
        FindChoicesOptions options = new FindChoicesOptions();
        options.setRecognizeOrdinals(false);
        options.setRecognizeNumbers(false);
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings(
            "the first one please.",
            colorChoices,
            options);
        Assert.assertEquals(0, found.size());
    }

    @Test
    public void shouldNOTFindAChoiceInAnUtteranceByNumericalIndex_Text_RecognizeNumbersFalse() {
        FindChoicesOptions options = new FindChoicesOptions();
        options.setRecognizeNumbers(false);
        List<ModelResult<FoundChoice>> found = ChoiceRecognizers.recognizeChoicesFromStrings(
            "one",
            colorChoices,
            options);
        Assert.assertEquals(0, found.size());
    }

    //
    // Helper methods
    //

    private static <T> void assertResult(ModelResult<T> result, int start, int end, String text) {
        Assert.assertEquals(start, result.getStart());
        Assert.assertEquals(end, result.getEnd());
        Assert.assertEquals(text, result.getText());
    }

    private static void assertValue(ModelResult<FoundValue> result, String value, int index, float score) {
        Assert.assertEquals("value", result.getTypeName());
        Assert.assertNotNull(result.getResolution());
        FoundValue resolution = result.getResolution();
        Assert.assertEquals(value, resolution.getValue());
        Assert.assertEquals(index, resolution.getIndex());
        Assert.assertEquals(score, resolution.getScore(), .01f);
    }

    private static void assertChoice(ModelResult<FoundChoice> result, String value, int index, float score, String synonym) {
        Assert.assertEquals("choice", result.getTypeName());
        Assert.assertNotNull(result.getResolution());
        FoundChoice resolution = result.getResolution();
        Assert.assertEquals(value, resolution.getValue());
        Assert.assertEquals(index, resolution.getIndex());
        Assert.assertEquals(score, resolution.getScore(), .01f);
        if (synonym != null) {
            Assert.assertEquals(synonym, resolution.getSynonym());
        }
    }
}
