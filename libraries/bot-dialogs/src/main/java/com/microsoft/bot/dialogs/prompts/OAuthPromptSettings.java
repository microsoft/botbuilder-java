// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.dialogs.prompts;

import com.microsoft.bot.connector.authentication.AppCredentials;

/**
 * Contains settings for an {@link OAuthPrompt}/>.
 */
public class OAuthPromptSettings {

    private AppCredentials oAuthAppCredentials;

    private String connectionName;

    private String title;

    private String text;

    private Integer timeout;

    private boolean endOnInvalidMessage;

    /**
     * Gets the OAuthAppCredentials for OAuthPrompt.
     *
     * @return the OAuthAppCredentials value as a AppCredentials.
     */
    public AppCredentials getOAuthAppCredentials() {
        return this.oAuthAppCredentials;
    }

    /**
     * Sets the OAuthAppCredentials for OAuthPrompt.
     *
     * @param withOAuthAppCredentials The OAuthAppCredentials value.
     */
    public void setOAuthAppCredentials(AppCredentials withOAuthAppCredentials) {
        this.oAuthAppCredentials = withOAuthAppCredentials;
    }

    /**
     * Gets the name of the OAuth connection.
     *
     * @return the ConnectionName value as a String.
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    /**
     * Sets the name of the OAuth connection.
     *
     * @param withConnectionName The ConnectionName value.
     */
    public void setConnectionName(String withConnectionName) {
        this.connectionName = withConnectionName;
    }

    /**
     * Gets the title of the sign-in card.
     *
     * @return the Title value as a String.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title of the sign-in card.
     *
     * @param withTitle The Title value.
     */
    public void setTitle(String withTitle) {
        this.title = withTitle;
    }

    /**
     * Gets any additional text to include in the sign-in card.
     *
     * @return the Text value as a String.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets any additional text to include in the sign-in card.
     *
     * @param withText The Text value.
     */
    public void setText(String withText) {
        this.text = withText;
    }

    /**
     * Gets the number of milliseconds the prompt waits for the user to
     * authenticate. Default is 900,000 (15 minutes).
     *
     * @return the Timeout value as a int?.
     */
    public Integer getTimeout() {
        return this.timeout;
    }

    /**
     * Sets the number of milliseconds the prompt waits for the user to
     * authenticate. Default is 900,000 (15 minutes).
     *
     * @param withTimeout The Timeout value.
     */
    public void setTimeout(Integer withTimeout) {
        this.timeout = withTimeout;
    }

    /**
     * Gets a value indicating whether the {@link OAuthPrompt} should end upon
     * receiving an invalid message. Generally the {@link OAuthPrompt} will ignore
     * incoming messages from the user during the auth flow, if they are not related
     * to the auth flow. This flag enables ending the {@link OAuthPrompt} rather
     * than ignoring the user's message. Typically, this flag will be set to 'true',
     * but is 'false' by default for backwards compatibility.
     *
     * @return the EndOnInvalidMessage value as a boolean.
     */
    public boolean getEndOnInvalidMessage() {
        return this.endOnInvalidMessage;
    }

    /**
     * Sets a value indicating whether the {@link OAuthPrompt} should end upon
     * receiving an invalid message. Generally the {@link OAuthPrompt} will ignore
     * incoming messages from the user during the auth flow, if they are not related
     * to the auth flow. This flag enables ending the {@link OAuthPrompt} rather
     * than ignoring the user's message. Typically, this flag will be set to 'true',
     * but is 'false' by default for backwards compatibility.
     *
     * @param withEndOnInvalidMessage The EndOnInvalidMessage value.
     */
    public void setEndOnInvalidMessage(boolean withEndOnInvalidMessage) {
        this.endOnInvalidMessage = withEndOnInvalidMessage;
    }

}
