// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.microsoft.bot.dialogs.prompts.PromptCultureModel;
import com.microsoft.bot.dialogs.prompts.PromptCultureModels;

import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Test;



public class PromptCultureModelTests {

    @Test
    public void MapCulturesToNearest() {

        List<Pair<String, String>> testData = new ArrayList<Pair<String, String>>();
        testData.add(Pair.with("en-us", "en-us"));
        testData.add(Pair.with("en-US", "en-us"));
        testData.add(Pair.with("en-Us", "en-us"));
        testData.add(Pair.with("EN", "en-us"));
        testData.add(Pair.with("en", "en-us"));
        testData.add(Pair.with("es-es", "es-es"));
        testData.add(Pair.with("es-ES", "es-es"));
        testData.add(Pair.with("es-Es", "es-es"));
        testData.add(Pair.with("ES", "es-es"));
        testData.add(Pair.with("es", "es-es"));

        for (Pair<String, String> pair : testData) {
            ShouldCorrectlyMapToNearesLanguage(pair.getValue0(), pair.getValue1());
        }

    }

    public void ShouldCorrectlyMapToNearesLanguage(String localeVariation, String expectedResult) {
        String result = PromptCultureModels.mapToNearestLanguage(localeVariation);
        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void ShouldReturnAllSupportedCultures() {
        PromptCultureModel[] expected = new PromptCultureModel[] {
            // Bulgarian,
            // Chinese,
            // Dutch,
            PromptCultureModels.ENGLISH,
            // French,
            // German,
            // Hindi,
            // Italian,
            // Japanese,
            // Korean,
            // Portuguese,
            PromptCultureModels.SPANISH
            // Swedish,
            // Turkish
        };

        PromptCultureModel[] supportedCultures = PromptCultureModels.getSupportedCultures();

        List<PromptCultureModel> expectedList = Arrays.asList(expected);
        List<PromptCultureModel> supportedList = Arrays.asList(supportedCultures);
        for (PromptCultureModel promptCultureModel : expectedList) {
            Assert.assertTrue(supportedList.contains(promptCultureModel));
        }

    }
}

