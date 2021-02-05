// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

/**
 * Represents an event related to the "lifecycle" of the dialog.
 */
public class DialogEvent {
    private boolean bubble;
    private String name;
    private Object value;

    /**
     * Indicates whether the event will be bubbled to the parent `DialogContext`
     * if not handled by the current dialog.
     * @return Whether the event can be bubbled to the parent `DialogContext`.
     */
    public boolean shouldBubble() {
        return bubble;
    }

    /**
     * Sets whether the event will be bubbled to the parent `DialogContext`
     * if not handled by the current dialog.
     * @param withBubble Whether the event can be bubbled to the parent `DialogContext`.
     */
    public void setBubble(boolean withBubble) {
        bubble = withBubble;
    }

    /**
     * Gets name of the event being raised.
     * @return Name of the event being raised.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the event being raised.
     * @param withName Name of the event being raised.
     */
    public void setName(String withName) {
        name = withName;
    }

    /**
     * Gets optional value associated with the event.
     * @return Optional value associated with the event.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets optional value associated with the event.
     * @param withValue Optional value associated with the event.
     */
    public void setValue(Object withValue) {
        value = withValue;
    }
}
