// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * List Element for Dynamic Lists. Defines a sub-list to append to an existing
 * list entity.
 *
 */
public class ListElement {

    /**
     * Initializes a new instance of the ListElement class.
     */
    public ListElement() {
    }

    /**
     * Initializes a new instance of the ListElement class.
     *
     * @param canonicalForm The canonical form of the sub-list.
     * @param synonyms      The synonyms of the canonical form.
     */
    public ListElement(String canonicalForm, List<String> synonyms) {
        this.canonicalForm = canonicalForm;
        this.synonyms = synonyms;
    }

    /**
     * The canonical form of the sub-list.
     */
    @JsonProperty(value = "canonicalForm")
    private String canonicalForm;

    /**
     * The synonyms of the canonical form.
     */
    @JsonProperty(value = "synonyms")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> synonyms;

    /**
     * Gets the canonical form of the sub-list.
     *
     * @return String canonical form of the sub-list.
     */
    public String getCanonicalForm() {
        return canonicalForm;
    }

    /**
     * Sets the canonical form of the sub-list.
     *
     * @param canonicalForm the canonical form of the sub-list.
     */
    public void setCanonicalForm(String canonicalForm) {
        this.canonicalForm = canonicalForm;
    }

    /**
     * Gets the synonyms of the canonical form.
     *
     * @return the synonyms List of the canonical form.
     */
    public List<String> getSynonyms() {
        return synonyms;
    }

    /**
     * Sets the synonyms of the canonical form.
     *
     * @param synonyms List of synonyms of the canonical form.
     */
    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    /**
     * Validate the object.
     *
     * @throws IllegalArgumentException if canonicalForm is null.
     */
    public void validate() throws IllegalArgumentException {
        if (canonicalForm == null) {
            throw new IllegalArgumentException("RequestList requires CanonicalForm to be defined.");
        }
    }

}
