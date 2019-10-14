package com.microsoft.bot.sample.welcomeuser;

/**
 * This is the welcome state for this sample.
 *
 * <p>NOTE: Standard Java getters/setters must be used for properties.  Alternatively,
 * the Jackson JSON annotations could be used instead.  If any methods start with "get"
 * but aren't a property, the Jackson JSON 'JsonIgnore' annotation must be used.</p>
 */
public class WelcomeUserState {
    private boolean didBotWelcomeUser;

    public boolean getDidBotWelcomeUser() {
        return didBotWelcomeUser;
    }

    public void setDidBotWelcomeUser(boolean withDidWelcomUser) {
        didBotWelcomeUser = withDidWelcomUser;
    }
}
