// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.prompts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.dialogs.choices.Choice;
import com.microsoft.bot.dialogs.choices.ChoiceFactoryOptions;
import com.microsoft.bot.dialogs.choices.ChoiceRecognizers;
import com.microsoft.bot.dialogs.choices.FoundChoice;
import com.microsoft.bot.dialogs.choices.ListStyle;
import com.microsoft.bot.dialogs.choices.ModelResult;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.recognizers.text.choice.ChoiceRecognizer;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;

/**
 * Prompts a user to confirm something with a yes/no response.
 */
public class ConfirmPrompt extends Prompt<Boolean> {

    /**
     * A map of Default Choices based on {@link GetSupportedCultures} . Can
     * be replaced by user using the constructor that contains choiceDefaults.
     */
    private Map<String, Triplet<Choice, Choice, ChoiceFactoryOptions>> choiceDefaults;
    private ListStyle style;
    private String defaultLocale;
    private ChoiceFactoryOptions choiceOptions;
    private Pair<Choice, Choice> confirmChoices;

    /**
     * Initializes a new instance of the {@link ConfirmPrompt} class.
     *
     * @param dialogId      The ID to assign to this prompt.
     */
    public ConfirmPrompt(String dialogId) {
        this(dialogId, null, null);
    }

    /**
     * Initializes a new instance of the {@link ConfirmPrompt} class.
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
    public ConfirmPrompt(String dialogId, PromptValidator<Boolean> validator, String defaultLocale) {
        super(dialogId, validator);

        choiceDefaults = new HashMap<String, Triplet<Choice, Choice, ChoiceFactoryOptions>>();
        for (PromptCultureModel model : PromptCultureModels.getSupportedCultures()) {
            Choice yesChoice = new Choice(model.getYesInLanguage());
            Choice noChoice = new Choice(model.getNoInLanguage());
            ChoiceFactoryOptions factoryOptions = new ChoiceFactoryOptions();
            factoryOptions.setInlineSeparator(model.getSeparator());
            factoryOptions.setInlineOr(model.getInlineOr());
            factoryOptions.setInlineOrMore(model.getInlineOrMore());
            factoryOptions.setIncludeNumbers(true);
            choiceDefaults.put(model.getLocale(), new Triplet<Choice,
                                                              Choice,
                                                              ChoiceFactoryOptions>(yesChoice,
                                                                                    noChoice,
                                                                                    factoryOptions));
        }

        this.style = ListStyle.AUTO;
        this.defaultLocale = defaultLocale;
    }

    /**
     * Initializes a new instance of the {@link ConfirmPrompt} class.
     *
     * @param dialogId       The ID to assign to this prompt.
     * @param validator      Optional, a {@link PromptValidator{FoundChoice}} that
     *                       contains additional, custom validation for this prompt.
     * @param defaultLocale  Optional, the default locale used to determine
     *                       language-specific behavior of the prompt. The locale is
     *                       a 2, 3, or 4 character ISO 639 code that represents a
     *                       language or language family.
     * @param choiceDefaults Overrides the dictionary of Bot Framework SDK-supported
     *                       _choiceDefaults (for prompt localization). Must be
     *                       passed in to each ConfirmPrompt that needs the custom
     *                       choice defaults.
     *
     *                       The value of {@link dialogId} must be unique within the
     *                       {@link DialogSet} or {@link ComponentDialog} to which
     *                       the prompt is added. If the {@link Activity#locale} of
     *                       the {@link DialogContext}
     *                       .{@link DialogContext#context}
     *                       .{@link ITurnContext#activity} is specified, then that
     *                       local is used to determine language specific behavior;
     *                       otherwise the {@link defaultLocale} is used. US-English
     *                       is the used if no language or default locale is
     *                       available, or if the language or locale is not
     *                       otherwise supported.
     */
    public ConfirmPrompt(String dialogId, Map<String, Triplet<Choice, Choice, ChoiceFactoryOptions>> choiceDefaults,
            PromptValidator<Boolean> validator, String defaultLocale) {
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
     * Gets the yes and no {@link Choice} for the prompt.
     * @return The yes and no {@link Choice} for the prompt.
     */
    public Pair<Choice, Choice> getConfirmChoices() {
        return this.confirmChoices;
    }

    /**
     * Sets the yes and no {@link Choice} for the prompt.
     * @param confirmChoices The yes and no {@link Choice} for the prompt.
     */
    public void setConfirmChoices(Pair<Choice, Choice> confirmChoices) {
        this.confirmChoices = confirmChoices;
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

        // Format prompt to send
        Activity prompt;
        String channelId = turnContext.getActivity().getChannelId();
        String culture = determineCulture(turnContext.getActivity());
        Triplet<Choice, Choice, ChoiceFactoryOptions> defaults = choiceDefaults.get(culture);
        ChoiceFactoryOptions localChoiceOptions = getChoiceOptions() != null ? getChoiceOptions()
                                                  : defaults.getValue2();
        List<Choice> choices = new ArrayList<Choice>(Arrays.asList(defaults.getValue0(),
                                                                   defaults.getValue1()));

        ListStyle localStyle = options.getStyle() != null ? options.getStyle() : getStyle();
        if (isRetry && options.getRetryPrompt() != null) {
            prompt = appendChoices(options.getRetryPrompt(), channelId,
                                    choices, localStyle, localChoiceOptions);
        } else {
            prompt = appendChoices(options.getPrompt(), channelId, choices,
                                    localStyle, localChoiceOptions);
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
    protected CompletableFuture<PromptRecognizerResult<Boolean>> onRecognize(TurnContext turnContext,
                            Map<String, Object> state, PromptOptions options) {

        if (turnContext == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "turnContext cannot be null"
            ));
        }

        PromptRecognizerResult<Boolean> result = new PromptRecognizerResult<Boolean>();
        if (turnContext.getActivity().isType(ActivityTypes.MESSAGE)) {
            // Recognize utterance
            String utterance = turnContext.getActivity().getText();
            if (StringUtils.isBlank(utterance)) {
                return CompletableFuture.completedFuture(result);
            }

            String culture = determineCulture(turnContext.getActivity());
            List<com.microsoft.recognizers.text.ModelResult> results =
                    ChoiceRecognizer.recognizeBoolean(utterance, culture);
            if (results.size() > 0) {
                com.microsoft.recognizers.text.ModelResult first = results.get(0);
                Boolean value = (Boolean) first.resolution.get("value");
                if (value != null) {
                    result.setSucceeded(true);
                    result.setValue(value);
                }
            } else {
                // First check whether the prompt was sent to the user with numbers -
                // if it was we should recognize numbers
                Triplet<Choice, Choice, ChoiceFactoryOptions> defaults = choiceDefaults.get(culture);
                ChoiceFactoryOptions choiceOpts = choiceOptions != null ? choiceOptions : defaults.getValue2();

                // This logic reflects the fact that IncludeNumbers is nullable and True is the default
                // set in Inline style
                if (choiceOpts.getIncludeNumbers() == null
                    || choiceOpts.getIncludeNumbers() != null && choiceOpts.getIncludeNumbers()) {
                    // The text may be a number in which case we will interpret that as a choice.
                    Pair<Choice, Choice> confirmedChoices = confirmChoices != null ? confirmChoices
                                            : new Pair<Choice, Choice>(defaults.getValue0(), defaults.getValue1());
                    ArrayList<Choice> choices = new ArrayList<Choice>();
                    choices.add(confirmedChoices.getValue0());
                    choices.add(confirmedChoices.getValue1());

                    List<ModelResult<FoundChoice>> secondAttemptResults =
                            ChoiceRecognizers.recognizeChoices(utterance, choices);
                    if (secondAttemptResults.size() > 0) {
                        result.setSucceeded(true);
                        result.setValue(secondAttemptResults.get(0).getResolution().getIndex() == 0);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    private String determineCulture(Activity activity) {

        String locale;
        if (activity.getLocale() != null) {
            locale = activity.getLocale();
        } else if (defaultLocale != null) {
            locale = defaultLocale;
        } else {
            locale = PromptCultureModels.ENGLISH_CULTURE;
        }

        String culture = PromptCultureModels.mapToNearestLanguage(locale);
        if (StringUtils.isEmpty(culture) || !choiceDefaults.containsKey(culture)) {
            culture = PromptCultureModels.ENGLISH_CULTURE;
        }
        return culture;
    }
}
