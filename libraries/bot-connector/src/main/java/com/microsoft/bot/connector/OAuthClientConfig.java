package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import org.apache.commons.lang3.NotImplementedException;

import java.util.concurrent.CompletableFuture;

/**
 * OAuthClient config.
 */
public final class OAuthClientConfig {
    private OAuthClientConfig() {

    }

    /**
     * The default endpoint that is used for API requests.
     */
    public static final String OAUTHENDPOINT = AuthenticationConstants.OAUTH_URL;

    /**
     * Value indicating whether when using the Emulator, whether to emulate the
     * OAuthCard behavior or use connected flows.
     */
    @SuppressWarnings("checkstyle:VisibilityModifier")
    public static boolean emulateOAuthCards = false;

    /**
     * Send a dummy OAuth card when the bot is being used on the Emulator for
     * testing without fetching a real token.
     *
     * @param client  The OAuth client.
     * @param emulate Indicates whether the Emulator should emulate the OAuth card.
     * @return A task that represents the work queued to execute.
     */
    public static CompletableFuture<Void> sendEmulateOAuthCards(
        OAuthClient client,
        boolean emulate
    ) {
        throw new NotImplementedException("sendEmulateOAuthCards");
    }
}
