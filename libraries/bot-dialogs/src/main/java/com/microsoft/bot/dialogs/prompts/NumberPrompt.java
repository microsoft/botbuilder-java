package com.microsoft.bot.dialogs.prompts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.activation.UnsupportedDataTypeException;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.recognizers.text.ModelResult;
import com.microsoft.recognizers.text.number.NumberRecognizer;
import com.microsoft.recognizers.text.numberwithunit.NumberWithUnitRecognizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Prompts a user to enter a number.
 *
 * The number prompt currently supports these types: {@link float} , {@link int}
 * , {@link long} , {@link double} , and {@link decimal} .
 * @param <T> numeric type for this prompt, which can be int, long, double, or float.
 */
public class NumberPrompt<T> extends Prompt<T> {

    private String defaultLocale;
    private final Class<T> classOfNumber;

    /**
     * Initializes a new instance of the {@link NumberPrompt{T}} class.
     *
     * @param dialogId      Unique ID of the dialog within its parent
     *                      {@link DialogSet} or {@link ComponentDialog} .
     * @param validator     Validator that will be called each time the user
     *                      responds to the prompt.
     * @param defaultLocale Locale to use.
     * @param classOfNumber Type of <T> used to determine within the class what type was created for. This is required
     *                      due to type erasure in Java not allowing checking the type of <T> during runtime.
     * @throws UnsupportedDataTypeException thrown if a type other than int, long, float, or double are used for <T>.
     */
    public NumberPrompt(String dialogId, PromptValidator<T> validator, String defaultLocale, Class<T> classOfNumber)
            throws UnsupportedDataTypeException {

        super(dialogId, validator);
        this.defaultLocale = defaultLocale;
        this.classOfNumber = classOfNumber;

        if (!(classOfNumber.getSimpleName() == "int" || classOfNumber.getSimpleName() == "long"
                || classOfNumber.getSimpleName() == "float" || classOfNumber.getSimpleName() == "double")) {
            throw new UnsupportedDataTypeException(String.format("NumberPrompt: Type argument %0 <T> is not supported",
                    classOfNumber.getSimpleName()));
        }
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
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param state       Contains state for the current instance of the prompt on
     *                    the dialog stack.
     * @param options     A prompt options Object constructed from the options
     *                    initially provided in the call to
     *                    {@link DialogContext#prompt(String, PromptOptions)} .
     * @param isRetry     true if this is the first time this prompt dialog instance
     *                    on the stack is prompting the user for input; otherwise,
     *                    false.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    protected CompletableFuture<Void> onPrompt(TurnContext turnContext, Map<String, Object> state,
            PromptOptions options, Boolean isRetry) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null");
        }

        if (options == null) {
            throw new IllegalArgumentException("options cannot be nulle");
        }

        if (isRetry && options.getRetryPrompt() != null) {
            turnContext.sendActivity(options.getRetryPrompt()).join();
        } else if (options.getPrompt() != null) {
            turnContext.sendActivity(options.getPrompt()).join();
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Attempts to recognize the user's input.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     * @param state       Contains state for the current instance of the prompt on
     *                    the dialog stack.
     * @param options     A prompt options Object constructed from the options
     *                    initially provided in the call to
     *                    {@link DialogContext#prompt(String, PromptOptions)} .
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     *
     *         If the task is successful, the result describes the result of the
     *         recognition attempt.
     */
    @Override
    @SuppressWarnings("PMD")
    protected CompletableFuture<PromptRecognizerResult<T>> onRecognize(TurnContext turnContext,
            Map<String, Object> state, PromptOptions options) {
        if (turnContext == null) {
            throw new IllegalArgumentException("turnContext cannot be null.");
        }

        PromptRecognizerResult<T> result = new PromptRecognizerResult<T>();
        if (turnContext.getActivity().getType() == ActivityTypes.MESSAGE) {
            String utterance = turnContext.getActivity().getText();
            if (StringUtils.isEmpty(utterance)) {
                return CompletableFuture.completedFuture(result);
            }

            String culture = turnContext.getActivity().getLocale() != null ? turnContext.getActivity().getLocale()
                    : defaultLocale != null ? defaultLocale : PromptCultureModels.ENGLISH_CULTURE;
            List<ModelResult> results = recognizeNumberWithUnit(utterance, culture);
            if (results.size() > 0) {
                // Try to parse value based on type
                String text = "";

                // Try to parse value based on type
                Object valueResolution = results.get(0).resolution.get("value");
                if (valueResolution != null) {
                    text = (String) valueResolution;
                }

                if (classOfNumber.getSimpleName() == "float") {
                    try {
                        float value = Float.parseFloat(text);
                        result.setSucceeded(true);
                        result.setValue((T) (Object) value);

                    } catch (NumberFormatException numberFormatException) {
                    }
                } else if (classOfNumber.getSimpleName() == "int") {
                    try {
                        int value = Integer.parseInt(text);
                        result.setSucceeded(true);
                        result.setValue((T) (Object) value);

                    } catch (NumberFormatException numberFormatException) {
                    }
                } else if (classOfNumber.getSimpleName() == "long") {
                    try {
                        long value = Long.parseLong(text);
                        result.setSucceeded(true);
                        result.setValue((T) (Object) value);

                    } catch (NumberFormatException numberFormatException) {
                    }
                } else if (classOfNumber.getSimpleName() == "double") {
                    try {
                        double value = Double.parseDouble(text);
                        result.setSucceeded(true);
                        result.setValue((T) (Object) value);

                    } catch (NumberFormatException numberFormatException) {
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(result);
    }

    private static List<ModelResult> recognizeNumberWithUnit(String utterance, String culture) {
        List<ModelResult> number = NumberRecognizer.recognizeNumber(utterance, culture);

        if (number.size() > 0) {
            // Result when it matches with a number recognizer
            return number;
        } else {
            List<ModelResult> result;
            // Analyze every option for numberWithUnit
            result = NumberWithUnitRecognizer.recognizeCurrency(utterance, culture);
            if (result.size() > 0) {
                return result;
            }

            result = NumberWithUnitRecognizer.recognizeAge(utterance, culture);
            if (result.size() > 0) {
                return result;
            }

            result = NumberWithUnitRecognizer.recognizeTemperature(utterance, culture);
            if (result.size() > 0) {
                return result;
            }

            result = NumberWithUnitRecognizer.recognizeDimension(utterance, culture);
            if (result.size() > 0) {
                return result;
            }

            return null;
        }
    }
}
