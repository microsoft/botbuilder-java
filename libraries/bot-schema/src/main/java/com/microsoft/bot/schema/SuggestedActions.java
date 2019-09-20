/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SuggestedActions that can be performed.
 */
public class SuggestedActions {
    /**
     * Ids of the recipients that the actions should be shown to.  These Ids
     * are relative to the channelId and a subset of all recipients of the
     * activity.
     */
    @JsonProperty(value = "to")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> to;

    /**
     * Actions that can be shown to the user.
     */
    @JsonProperty(value = "actions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardAction> actions;

    public static SuggestedActions clone(SuggestedActions suggestedActions) {
        if (suggestedActions == null) {
            return null;
        }

        return new SuggestedActions() {{
            setTo(suggestedActions.getTo());

            setActions(suggestedActions.getActions().stream()
                .map(card -> CardAction.clone(card))
                .collect(Collectors.toCollection(ArrayList::new)));
        }};
    }

    /**
     * Default empty SuggestedActions
     */
    public SuggestedActions() {

    }

    /**
     *
     * @param withCardActions
     */
    public SuggestedActions(CardAction[] withCardActions) {
        this.setActions(Arrays.asList(withCardActions));
    }

    /**
     * Get the to value.
     *
     * @return the to value
     */
    public List<String> getTo() {
        return this.to;
    }

    /**
     * Set the to value.
     *
     * @param withTo the to value to set
     */
    public void setTo(List<String> withTo) {
        this.to = withTo;
    }

    /**
     * Get the actions value.
     *
     * @return the actions value
     */
    public List<CardAction> getActions() {
        return this.actions;
    }

    /**
     * Set the actions value.
     *
     * @param withActions the actions value to set
     */
    public void setActions(List<CardAction> withActions) {
        this.actions = withActions;
    }
}
