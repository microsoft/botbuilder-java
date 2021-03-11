// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.recognizers.text.ModelResult;
import com.microsoft.recognizers.text.datetime.DateTimeOptions;
import com.microsoft.recognizers.text.datetime.DateTimeRecognizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Prompts a user for a date-time value.
 */
public class DateTimePrompt extends Prompt<List<DateTimeResolution>> {

    private String defaultLocale;

    /**
     * Initializes a new instance of the {@link DateTimePrompt} class.
     *
     * @param dialogId      The ID to assign to this prompt.
     * @param validator     Optional, a {@link PromptValidator.FoundChoice} that
     *                      contains additional, custom validation for this prompt.
     * @param defaultLocale Optional, the default locale used to determine
     *                      language-specific behavior of the prompt. The locale is
     *                      a 2, 3, or 4 character ISO 639 code that represents a
     *                      language or language family.
     *
     *                      The value of {@link dialogId} must be unique within the
     *                      {@link DialogSet} or {@link ComponentDialog} to which
     *                      the prompt is added. If the {@link Activity#locale} of
     *                      the {@link DialogContext} .{@link DialogContext#context}
     *                      .{@link ITurnContext#activity} is specified, then that
     *                      local is used to determine language specific behavior;
     *                      otherwise the {@link defaultLocale} is used. US-English
     *                      is the used if no language or default locale is
     *                      available, or if the language or locale is not otherwise
     *                      supported.
     */
    public DateTimePrompt(String dialogId, PromptValidator<List<DateTimeResolution>> validator, String defaultLocale) {
        super(dialogId, validator);
        this.defaultLocale = defaultLocale;
    }

    /**
     * Gets the default locale used to determine language-specific behavior of the
     * prompt.
     *
     * @return The default locale used to determine language-specific behavior of
     *         the prompt.
     */
    public String getDefaultLocale() {
        return this.defaultLocale;
    }

    /**
     * Sets the default locale used to determine language-specific behavior of the
     * prompt.
     *
     * @param defaultLocale The default locale used to determine language-specific
     *                      behavior of the prompt.
     */
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Prompts the user for input.
     *
     * @param turnContext  Context for the current turn of conversation with the user.
     * @param state        Contains state for the current instance of the prompt on the
     *                     dialog stack.
     * @param options      A prompt options Object constructed from the options initially
     *                     provided in the call to {@link DialogContext#prompt(String, PromptOptions)} .
     * @param isRetry      true if this is the first time this prompt dialog instance on the
     *                     stack is prompting the user for input; otherwise, false.
     *
     * @return   A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    protected CompletableFuture<Void> onPrompt(TurnContext turnContext, Map<String, Object> state,
                                               PromptOptions options, Boolean isRetry) {

        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        if (options == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "options cannot be null"
            ));
        }

        if (isRetry && options.getRetryPrompt() != null) {
             return turnContext.sendActivity(options.getRetryPrompt()).thenApply(result -> null);
        } else if (options.getPrompt() != null) {
             return turnContext.sendActivity(options.getPrompt()).thenApply(result -> null);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Attempts to recognize the user's input as a date-time value.
     *
     * @param turnContext  Context for the current turn of conversation with the user.
     * @param state        Contains state for the current instance of the prompt on the
     *                     dialog stack.
     * @param options      A prompt options Object constructed from the options initially
     *                     provided in the call to {@link DialogContext#prompt(String, PromptOptions)} .
     *
     * @return   A {@link CompletableFuture} representing the asynchronous operation.
     *
     * If the task is successful, the result describes the result of the recognition attempt.
     */
    @Override
    protected CompletableFuture<PromptRecognizerResult<List<DateTimeResolution>>>
            onRecognize(TurnContext turnContext, Map<String, Object> state, PromptOptions options) {

        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        PromptRecognizerResult<List<DateTimeResolution>> result =
                                new PromptRecognizerResult<List<DateTimeResolution>>();
        if (turnContext.getActivity().isType(ActivityTypes.MESSAGE)) {
            String utterance = turnContext.getActivity().getText();
            if (StringUtils.isEmpty(utterance)) {
                return CompletableFuture.completedFuture(result);
            }

            String culture = turnContext.getActivity().getLocale() != null ?  turnContext.getActivity().getLocale()
                             : defaultLocale != null ? defaultLocale : PromptCultureModels.ENGLISH_CULTURE;
            LocalDateTime refTime = turnContext.getActivity().getLocalTimestamp() != null
                                    ? turnContext.getActivity().getLocalTimestamp().toLocalDateTime() : null;
            List<ModelResult> results =
                DateTimeRecognizer.recognizeDateTime(utterance, culture, DateTimeOptions.None, true, refTime);
            if (results.size() > 0) {
                // Return list of resolutions from first match
                result.setSucceeded(true);
                result.setValue(new ArrayList<DateTimeResolution>());
                List<Map<String, String>> values = (List<Map<String, String>>) results.get(0).resolution.get("values");
                for (Map<String, String> mapEntry : values) {
                    result.getValue().add(readResolution(mapEntry));
                }
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    private static DateTimeResolution readResolution(Map<String, String> resolution) {
        DateTimeResolution result = new DateTimeResolution();

        if (resolution.containsKey("timex")) {
            result.setTimex(resolution.get("timex"));
        }

        if (resolution.containsKey("value")) {
            result.setValue(resolution.get("value"));
        }

        if (resolution.containsKey("start")) {
            result.setStart(resolution.get("start"));
        }

        if (resolution.containsKey("end")) {
            result.setEnd(resolution.get("end"));
        }
        return result;
    }

}
