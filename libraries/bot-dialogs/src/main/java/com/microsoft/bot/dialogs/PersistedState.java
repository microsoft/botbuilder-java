// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.HashMap;

/**
 * Represents the persisted data across turns.
 */
public class PersistedState {

    /**
     * The user state.
     */
    private HashMap<String, Object> userState;

    /**
     * The converation state.
     */
    private HashMap<String, Object> conversationState;

    /**
     * Constructs a PersistedState object.
     */
    public PersistedState() {
        userState = new HashMap<String, Object>();
        conversationState = new HashMap<String, Object>();
    }

    /**
     * Initializes a new instance of the PersistedState class.
     *
     * @param keys The persisted keys.
     * @param data The data containing the state values.
     */
    @SuppressWarnings("unchecked")
    public PersistedState(PersistedStateKeys keys, HashMap<String, Object> data) {
        if (data.containsKey(keys.getUserState())) {
            userState = (HashMap<String, Object>) data.get(keys.getUserState());
        }
        if (data.containsKey(keys.getConversationState())) {
            userState = (HashMap<String, Object>) data.get(keys.getConversationState());
        }
    }

    /**
     * @return userState Gets the user profile data.
     */
    public HashMap<String, Object> getUserState() {
        return this.userState;
    }

    /**
     * @param withUserState Sets user profile data.
     */
    public void setUserState(HashMap<String, Object> withUserState) {
        this.userState = withUserState;
    }

    /**
     * @return conversationState Gets the dialog state data.
     */
    public HashMap<String, Object> getConversationState() {
        return this.conversationState;
    }

    /**
     * @param withConversationState Sets the dialog state data.
     */
    public void setConversationState(HashMap<String, Object> withConversationState) {
        this.conversationState = withConversationState;
    }
}
