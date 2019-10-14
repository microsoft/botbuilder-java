package com.microsoft.bot.sample.statemanagement;

/**
 * This is the conversation data for this sample.
 *
 * <p>NOTE: Standard Java getters/setters must be used for properties.  Alternatively,
 * the Jackson JSON annotations could be used instead.  If any methods start with "get"
 * but aren't a property, the Jackson JSON 'JsonIgnore' annotation must be used.</p>
 */
public class UserProfile {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String withName) {
        name = withName;
    }
}
