package com.microsoft.bot.connector.authentication;

import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;

import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.RoleTypes;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.Assert.*;

public class ParameterizedBotFrameworkAuthenticationTest {
    private final String appId = "123";
    private final String appPassword = "test";
    private final String audienceEmail = "test@example.org";
    private final String callerId = "42";
    private final String tokenIssuer = "ABC123";
    private final String url = "https://example.org/example";
    private final Boolean validateAuth = true;
    private final String authHeader = "Auth Header";
    private final String channelIdHeader = "Channel Id Header";

    @Test
    public void getOriginatingAudience_withAuthenticationEnabled_shouldMatch() {
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory(appId, appPassword);

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        String originatedAudience = parameterizedBotFrameworkAuthentication.getOriginatingAudience();
        assertEquals(audienceEmail, originatedAudience);
    }

    @Test
    public void authenticateChannelRequest_withAuthenticationEnabledAndNoHeader_shouldNotBeNull() {
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory();

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());
        ClaimsIdentity claims = parameterizedBotFrameworkAuthentication
            .authenticateChannelRequest(null).join();

        assertNotNull(claims);
        assertTrue(claims.isAuthenticated());
        assertEquals(AuthenticationConstants.ANONYMOUS_AUTH_TYPE, claims.getIssuer());
    }

    @Test
    public void authenticateChannelRequest_withAuthenticationDisabled_shouldThrowAnError(){
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = Mockito.mock(PasswordServiceClientCredentialFactory.class);
        Mockito.when(passwordServiceClientCredentialFactory.isAuthenticationDisabled()).thenReturn(CompletableFuture.completedFuture(false));
        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();

        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        assertThrows(CompletionException.class, () -> {
            parameterizedBotFrameworkAuthentication.authenticateChannelRequest(null).join();;
        });
    }

    @Test
    public void authenticateRequest_withAuthenticationEnabled_shouldNotBeNull(){
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory(appId, appPassword);

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        Activity activity = Activity.createConversationUpdateActivity();
        activity.setChannelId(Channels.EMULATOR);
        ChannelAccount channelAccount = new ChannelAccount();
        RoleTypes roleTypes = RoleTypes.SKILL;
        channelAccount.setRole(roleTypes);
        activity.setRecipient(channelAccount);

        AuthenticateRequestResult result = parameterizedBotFrameworkAuthentication.authenticateRequest(activity,
            null).join();
        assertNotNull(result);
        assertTrue(result.getClaimsIdentity().isAuthenticated());
        assertEquals(AuthenticationConstants.ANONYMOUS_AUTH_TYPE, result.getClaimsIdentity().getIssuer());
    }

    @Test
    public void authenticateRequest_shouldNotBeNull() {
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory(appId, appPassword);

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        assertNotNull(parameterizedBotFrameworkAuthentication.authenticateRequest(Activity.createConversationUpdateActivity(),
            authHeader));
    }

    @Test
    public void authenticateStreamingRequest_withNoChannelId_shouldNotBeNull() {
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory(appId, appPassword);

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        assertThrows(CompletionException.class, () -> parameterizedBotFrameworkAuthentication.authenticateStreamingRequest(authHeader, "").join());
    }

    @Test
    public void authenticateStreamingRequest_withAuthenticationDisabled_shouldThrowAnError(){
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = Mockito.mock(PasswordServiceClientCredentialFactory.class);
        Mockito.when(passwordServiceClientCredentialFactory.isAuthenticationDisabled()).thenReturn(CompletableFuture.completedFuture(false));
        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();

        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        assertThrows(CompletionException.class, () -> {
            parameterizedBotFrameworkAuthentication.authenticateStreamingRequest(authHeader, null).join();
        });
    }

    @Test
    public void createConnectorFactory_shouldNotBeNull() {
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory(appId, appPassword);

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        assertNotNull(parameterizedBotFrameworkAuthentication.createConnectorFactory(SkillValidation.createAnonymousSkillClaim()));
    }

    @Test
    public void createUserTokenClient_shouldThrowAnError() {
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory(appId, appPassword);

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        assertThrows(IllegalArgumentException.class, () -> {
            parameterizedBotFrameworkAuthentication.createUserTokenClient(SkillValidation.createAnonymousSkillClaim()).join();
        });
    }

    @Test
    public void createBotFrameworkClient_shouldNotBeNull() {
        PasswordServiceClientCredentialFactory passwordServiceClientCredentialFactory = new PasswordServiceClientCredentialFactory(appId, appPassword);

        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        ParameterizedBotFrameworkAuthentication parameterizedBotFrameworkAuthentication = new ParameterizedBotFrameworkAuthentication(
            validateAuth, audienceEmail, audienceEmail, tokenIssuer,
            url, audienceEmail, audienceEmail, callerId, passwordServiceClientCredentialFactory,
            authenticationConfiguration, new OkHttpClient());

        assertNotNull(parameterizedBotFrameworkAuthentication.createBotFrameworkClient());
    }
}
