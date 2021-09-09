// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.dialogs.choices.Choice;
import com.microsoft.bot.dialogs.choices.ChoiceFactoryOptions;
import com.microsoft.bot.dialogs.choices.ChoiceRecognizers;
import com.microsoft.bot.dialogs.choices.FindChoicesOptions;
import com.microsoft.bot.dialogs.choices.FoundChoice;
import com.microsoft.bot.dialogs.choices.ListStyle;
import com.microsoft.bot.dialogs.choices.ModelResult;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

import org.apache.commons.lang3.StringUtils;

/**
 * Prompts a user to select from a list of choices.
 */
public class ChoicePrompt extends Prompt<FoundChoice> {

    /**
     * A dictionary of Default Choices based on {@link GetSupportedCultures} . Can
     * be replaced by user using the constructor that contains choiceDefaults.
     */
    private Map<String, ChoiceFactoryOptions> choiceDefaults;

    private ListStyle style;
    private String defaultLocale;
    private FindChoicesOptions recognizerOptions;
    private ChoiceFactoryOptions choiceOptions;

    /**
     * Initializes a new instance of the {@link ChoicePrompt} class.
     *
     * @param dialogId      The ID to assign to this prompt.
     */
    public ChoicePrompt(String dialogId) {
        this(dialogId, null, null);
    }

    /**
     * Initializes a new instance of the {@link ChoicePrompt} class.
     *
     * @param dialogId      The ID to assign to this prompt.
     * @param validator     Optional, a {@link PromptValidator{FoundChoice}} that
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
    public ChoicePrompt(String dialogId, PromptValidator<FoundChoice> validator, String defaultLocale) {
        super(dialogId, validator);

        choiceDefaults = new HashMap<String, ChoiceFactoryOptions>();
        for (PromptCultureModel model : PromptCultureModels.getSupportedCultures()) {
            ChoiceFactoryOptions options = new ChoiceFactoryOptions();
            options.setInlineSeparator(model.getSeparator());
            options.setInlineOr(model.getInlineOr());
            options.setInlineOrMore(model.getInlineOrMore());
            options.setIncludeNumbers(true);
            choiceDefaults.put(model.getLocale(), options);
        }

        this.style = ListStyle.AUTO;
        this.defaultLocale = defaultLocale;
    }

    /**
     * Initializes a new instance of the {@link ChoicePrompt} class.
     *
     * @param dialogId        The ID to assign to this prompt.
     * @param validator       Optional, a {@link PromptValidator{FoundChoice}} that contains
     *                        additional, custom validation for this prompt.
     * @param defaultLocale   Optional, the default locale used to determine
     *                        language-specific behavior of the prompt. The locale is a 2, 3, or 4 character
     *                        ISO 639 code
     *                        that represents a language or language family.
     * @param choiceDefaults  Overrides the dictionary of Bot Framework SDK-supported
     *                        _choiceDefaults (for prompt localization). Must be passed in to each ConfirmPrompt that
     *                        needs the custom choice defaults.
     *
     * The value of {@link dialogId} must be unique within the {@link DialogSet} or
     * {@link ComponentDialog} to which the prompt is added. If the {@link Activity#locale} of the
     * {@link DialogContext} .{@link DialogContext#context} .{@link ITurnContext#activity} is
     * specified, then that local is used to determine language specific behavior; otherwise the
     * {@link defaultLocale} is used. US-English is the used if no language or default locale is
     * available, or if the language or locale is not otherwise supported.
     */
    public ChoicePrompt(String dialogId, Map<String, ChoiceFactoryOptions> choiceDefaults,
                        PromptValidator<FoundChoice> validator, String defaultLocale) {
        this(dialogId, validator, defaultLocale);

        this.choiceDefaults = choiceDefaults;
    }

    /**
     * Gets the style to use when presenting the prompt to the user.
     *
     * @return The style to use when presenting the prompt to the user.
     */
    public ListStyle getStyle() {
        return this.style;
    }

    /**
     * Sets the style to use when presenting the prompt to the user.
     *
     * @param style The style to use when presenting the prompt to the user.
     */
    public void setStyle(ListStyle style) {
        this.style = style;
    }

    /**
     * Sets or sets the default locale used to determine language-specific behavior
     * of the prompt.
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
     * Gets or sets additional options passed to the underlying
     * {@link ChoiceRecognizers#recognizeChoices(String, IList{Choice}, FindChoicesOptions)} method.
     *
     * @return Options to control the recognition strategy.
     */
    public FindChoicesOptions getRecognizerOptions() {
        return this.recognizerOptions;
    }

    /**
     * Gets or sets additional options passed to the underlying
     * {@link ChoiceRecognizers#recognizeChoices(String, IList{Choice}, FindChoicesOptions)} method.
     *
     * @param recognizerOptions Options to control the recognition strategy.
     */
    public void setRecognizerOptions(FindChoicesOptions recognizerOptions) {
        this.recognizerOptions = recognizerOptions;
    }

    /**
     * Gets additional options passed to the {@link ChoiceFactory} and used to tweak the
     * style of choices rendered to the user.
     * @return Additional options for presenting the set of choices.
     */
        public ChoiceFactoryOptions getChoiceOptions() {
        return this.choiceOptions;
    }

    /**
     * Sets additional options passed to the {@link ChoiceFactory} and used to tweak the
     * style of choices rendered to the user.
     * @param choiceOptions Additional options for presenting the set of choices.
     */
    public void setChoiceOptions(ChoiceFactoryOptions choiceOptions) {
        this.choiceOptions = choiceOptions;
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
    protected  CompletableFuture<Void> onPrompt(TurnContext turnContext, Map<String, Object> state,
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

        String culture = determineCulture(turnContext.getActivity());

        // Format prompt to send
        Activity prompt;

        List<Choice> choices = options.getChoices() != null ? options.getChoices() : new ArrayList<Choice>();
        String channelId = turnContext.getActivity().getChannelId();
        ChoiceFactoryOptions choiceOpts = getChoiceOptions() != null
                                             ? getChoiceOptions() : choiceDefaults.get(culture);
        ListStyle choiceStyle = options.getStyle() != null ? options.getStyle() : style;

        if (isRetry && options.getRetryPrompt() != null) {
            prompt = appendChoices(options.getRetryPrompt(), channelId, choices, choiceStyle, choiceOpts);
        } else {
            prompt = appendChoices(options.getPrompt(), channelId, choices, choiceStyle, choiceOpts);
        }

        // Send prompt
        return turnContext.sendActivity(prompt).thenApply(result -> null);
    }

    /**
     * Attempts to recognize the user's input.
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
    protected CompletableFuture<PromptRecognizerResult<FoundChoice>> onRecognize(TurnContext turnContext,
                                    Map<String, Object> state, PromptOptions options) {

        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        List<Choice> choices = options.getChoices() != null ? options.getChoices() : new ArrayList<Choice>();

        PromptRecognizerResult<FoundChoice> result = new PromptRecognizerResult<FoundChoice>();
        if (turnContext.getActivity().isType(ActivityTypes.MESSAGE)) {
            Activity activity = turnContext.getActivity();
            String utterance = activity.getText();
            if (StringUtils.isEmpty(utterance)) {
                return CompletableFuture.completedFuture(result);
            }

            FindChoicesOptions opt = recognizerOptions != null ? recognizerOptions : new FindChoicesOptions();
            opt.setLocale(determineCulture(activity, opt));
            List<ModelResult<FoundChoice>> results = ChoiceRecognizers.recognizeChoices(utterance, choices, opt);
            if (results != null && results.size() > 0) {
                result.setSucceeded(true);
                result.setValue(results.get(0).getResolution());
            }
        }
        return CompletableFuture.completedFuture(result);
    }

    private String determineCulture(Activity activity) {
        return determineCulture(activity, null);
    }

    private String determineCulture(Activity activity, FindChoicesOptions opt) {

        String locale;
        if (activity.getLocale() != null) {
            locale = activity.getLocale();
        } else if (opt != null) {
            locale = opt.getLocale();
        } else if (defaultLocale != null) {
            locale = defaultLocale;
        } else {
            locale = PromptCultureModels.ENGLISH_CULTURE;
        }

        String culture = PromptCultureModels.mapToNearestLanguage(locale);
        if (StringUtils.isBlank(culture) || !choiceDefaults.containsKey(culture)) {
            culture = PromptCultureModels.ENGLISH_CULTURE;
        }
        return culture;
    }
}
