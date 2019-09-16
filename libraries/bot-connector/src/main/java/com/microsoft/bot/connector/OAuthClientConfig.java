package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import org.apache.commons.lang3.NotImplementedException;

import java.util.concurrent.CompletableFuture;

public final class OAuthClientConfig {
    private OAuthClientConfig() {

    }

    public static String OAUTHENDPOINT = AuthenticationConstants.OAUTH_URL;
    public static boolean EMULATEOAUTHCARDS = false;

    public static CompletableFuture<Void> sendEmulateOAuthCards(OAuthClient client, boolean emulateOAuthCards) {
        throw new NotImplementedException("sendEmulateOAuthCards");
    }
}
