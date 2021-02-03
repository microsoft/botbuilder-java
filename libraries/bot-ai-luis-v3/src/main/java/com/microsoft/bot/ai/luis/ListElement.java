package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.applicationinsights.core.dependencies.google.gson.JsonSyntaxException;

import java.util.List;

public class ListElement {
    /// <summary>
    /// Initializes a new instance of the <see cref="ListElement"/> class.
    /// </summary>
    public ListElement() {
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="ListElement"/> class.
    /// </summary>
    /// <param name="canonicalForm">The canonical form of the sub-list.</param>
    /// <param name="synonyms">The synonyms of the canonical form.</param>
    public ListElement(String canonicalForm, List<String> synonyms) {
        canonicalForm = canonicalForm;
        synonyms = synonyms;
    }

    /// <summary>
    /// Gets or sets the canonical form of the sub-list.
    /// </summary>
    /// <value>
    /// The canonical form of the sub-list.
    /// </value>
    @JsonProperty(value = "canonicalForm")
    public String canonicalForm;

    /// <summary>
    /// Gets or sets the synonyms of the canonical form.
    /// </summary>
    /// <value>
    /// The synonyms of the canonical form.
    /// </value>
    @JsonProperty(value = "synonyms")
    public List<String> synonyms;

    public void validate() {
        if (canonicalForm == null) {
            throw new JsonSyntaxException("RequestList requires CanonicalForm to be defined.");
        }
    }

}
