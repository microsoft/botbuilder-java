package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class JwtTokenValidationTests {
    @Test
    public void Connector_AuthHeader_CorrectAppIdAndServiceUrl_ShouldValidate()
        throws MalformedURLException, ExecutionException, InterruptedException {

        MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(
            "2cd87869-38a0-4182-9251-d056e8f0ac24",
            "2.30Vs3VQLKt974F");

//        String header = "Bearer " + credentials.getToken().get().getAccessToken();
//        SimpleCredentialProvider credentials = new SimpleCredentialProvider(
//            "2cd87869-38a0-4182-9251-d056e8f0ac24",
//            null);
//
//        JwtTokenValidation.ValidateAuthHeader(header, credentials, new SimpleChannelProvider(), null, "https://webchat.botframework.com/", client);
        //
        //        Assert.True(result.IsAuthenticated);
    }
}
