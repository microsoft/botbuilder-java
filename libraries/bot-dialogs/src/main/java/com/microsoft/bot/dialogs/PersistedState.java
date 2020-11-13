package com.microsoft.bot.dialogs;

import java.util.HashMap;

/**
 * Represents the persisted data across turns.
 */
public class PersistedState {

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
    public PersistedState(PersistedStateKeys keys, HashMap<String, Object> data) {
        // UserState = data.ContainsKey(keys.UserState) ? (IDictionary<string, object>)data[keys.UserState] : new ConcurrentDictionary<string, object>();
        // ConversationState = data.ContainsKey(keys.ConversationState) ? (IDictionary<string, object>)data[keys.ConversationState] : new ConcurrentDictionary<string, object>();
    }

    /**
     * @return userState
     */
    public HashMap<String, Object> getUserState() {
        return this.userState;
    }

    /**
     * @param withUserState
     */
    public void setUserState(HashMap<String, Object> withUserState) {
        this.userState = withUserState;
    }

    /**
     * @return conversationState
     */
    public HashMap<String, Object> getConversationState() {
        return this.conversationState;
    }

    /**
     * @param withConversationState
     */
    public void setConversationState(HashMap<String, Object> withConversationState) {
        this.conversationState = withConversationState;
    }

    private HashMap<String, Object> userState;

    private HashMap<String, Object> conversationState;

}
