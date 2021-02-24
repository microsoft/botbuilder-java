// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Contains state information associated with a Dialog on a dialog stack.
 */
public class DialogInstance {
    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "state")
    private Map<String, Object> state;

    private int stackIndex;
    private String version;

    /**
     * Creates a DialogInstance with id and state.
     */
    public DialogInstance() {

    }

    /**
     * Creates a DialogInstance with id and state.
     * @param withId The id
     * @param withState The state.
     */
    public DialogInstance(String withId, Map<String, Object> withState) {
        id = withId;
        state = withState;
    }

    /**
     * Gets the ID of the dialog.
     * @return The dialog id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the dialog.
     * @param withId The dialog id.
     */
    public void setId(String withId) {
        id = withId;
    }

    /**
     * Gets the instance's persisted state.
     * @return The instance's persisted state.
     */
    public Map<String, Object> getState() {
        return state;
    }

    /**
     * Sets the instance's persisted state.
     * @param withState The instance's persisted state.
     */
    public void setState(Map<String, Object> withState) {
        state = withState;
    }

    /**
     * Gets stack index. Positive values are indexes within the current DC and negative values are
     * indexes in the parent DC.
     * @return Positive values are indexes within the current DC and negative values are indexes in
     * the parent DC.
     */
    public int getStackIndex() {
        return stackIndex;
    }

    /**
     * Sets stack index. Positive values are indexes within the current DC and negative values are
     * indexes in the parent DC.
     * @param withStackIndex Positive values are indexes within the current DC and negative
     *                       values are indexes in the parent DC.
     */
    public void setStackIndex(int withStackIndex) {
        stackIndex = withStackIndex;
    }

    /**
     * Gets version string.
     * @return Unique string from the dialog this dialoginstance is tracking which is used
     * to identify when a dialog has changed in way that should emit an event for changed content.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version string.
     * @param withVersion Unique string from the dialog this dialoginstance is tracking which
     *                    is used to identify when a dialog has changed in way that should emit
     *                    an event for changed content.
     */
    public void setVersion(String withVersion) {
        version = withVersion;
    }
}
