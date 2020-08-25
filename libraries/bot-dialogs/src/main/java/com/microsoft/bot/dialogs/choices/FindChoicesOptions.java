// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains options to control how input is matched against a list of choices.
 */
public class FindChoicesOptions extends FindValuesOptions {
    @JsonProperty(value = "noValue")
    private boolean noValue;

    @JsonProperty(value = "noAction")
    private boolean noAction;

    @JsonProperty(value = "recognizeNumbers")
    private boolean recognizeNumbers = true;

    @JsonProperty(value = "recognizeOrdinals")
    private boolean recognizeOrdinals;

    /**
     * Indicates whether the choices value will NOT be search over. The default is false.
     * @return true if the choices value will NOT be search over.
     */
    public boolean isNoValue() {
        return noValue;
    }

    /**
     * Sets whether the choices value will NOT be search over.
     * @param withNoValue true if the choices value will NOT be search over.
     */
    public void setNoValue(boolean withNoValue) {
        noValue = withNoValue;
    }

    /**
     * Indicates whether the title of the choices action will NOT be searched over. The default
     * is false.
     * @return true if the title of the choices action will NOT be searched over.
     */
    public boolean isNoAction() {
        return noAction;
    }

    /**
     * Sets whether the title of the choices action will NOT be searched over.
     * @param withNoAction true if the title of the choices action will NOT be searched over.
     */
    public void setNoAction(boolean withNoAction) {
        noAction = withNoAction;
    }

    /**
     * Indicates whether the recognizer should check for Numbers using the NumberRecognizer's
     * NumberModel.
     * @return Default is true.  If false, the Number Model will not be used to check the
     * utterance for numbers.
     */
    public boolean isRecognizeNumbers() {
        return recognizeNumbers;
    }

    /**
     * Set whether the recognizer should check for Numbers using the NumberRecognizer's
     * NumberModel.
     * @param withRecognizeNumbers Default is true.  If false, the Number Model will not be
     *                             used to check the utterance for numbers.
     */
    public void setRecognizeNumbers(boolean withRecognizeNumbers) {
        recognizeNumbers = withRecognizeNumbers;
    }

    /**
     * Indicates whether the recognizer should check for Ordinal Numbers using the NumberRecognizer's
     * OrdinalModel.
     * @return Default is true.  If false, the Ordinal Model will not be used to check the
     * utterance for ordinal numbers.
     */
    public boolean isRecognizeOrdinals() {
        return recognizeOrdinals;
    }

    /**
     * Sets whether the recognizer should check for Ordinal Numbers using the NumberRecognizer's
     * OrdinalModel.
     * @param withRecognizeOrdinals If false, the Ordinal Model will not be used to check the
     *                              utterance for ordinal numbers.
     */
    public void setRecognizeOrdinals(boolean withRecognizeOrdinals) {
        recognizeOrdinals = withRecognizeOrdinals;
    }
}
