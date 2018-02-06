package com.microsoft.bot.connector;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.bot.connector.customizations.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class BotAuthenticatorTest {

    @BeforeClass
    public static void beforeClass() {
        EmulatorValidation.ToBotFromEmulatorTokenValidationParameters.validateLifetime = false;
        ChannelValidation.ToBotFromChannelTokenValidationParameters.validateLifetime = false;
    }

    @Test
    public void AuthHeaderWithCorrectAppIdAndServiceUrl() throws ExecutionException, InterruptedException {
        String header = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IkdDeEFyWG9OOFNxbzdQd2VBNy16NjVkZW5KUSIsIng1dCI6IkdDeEFyWG9OOFNxbzdQd2VBNy16NjVkZW5KUSJ9.eyJzZXJ2aWNldXJsIjoiaHR0cHM6Ly93ZWJjaGF0LmJvdGZyYW1ld29yay5jb20vIiwiaXNzIjoiaHR0cHM6Ly9hcGkuYm90ZnJhbWV3b3JrLmNvbSIsImF1ZCI6IjM5NjE5YTU5LTVhMGMtNGY5Yi04N2M1LTgxNmM2NDhmZjM1NyIsImV4cCI6MTUxNjczNzUyMCwibmJmIjoxNTE2NzM2OTIwfQ.TBgpxbDS-gx1wm7ldvl7To-igfskccNhp-rU1mxUMtGaDjnsU--usH4OXZfzRsZqMlnXWXug_Hgd_qOr5RH8wVlnXnMWewoZTSGZrfp8GOd7jHF13Gz3F1GCl8akc3jeK0Ppc8R_uInpuUKa0SopY0lwpDclCmvDlz4PN6yahHkt_666k-9UGmRt0DDkxuYjbuYG8EDZxyyAhr7J6sFh3yE2UGRpJjRDB4wXWqv08Cp0Gn9PAW2NxOyN8irFzZH5_YZqE3DXDAYZ_IOLpygXQR0O-bFIhLDVxSz6uCeTBRjh8GU7XJ_yNiRDoaby7Rd2IfRrSnvMkBRsB8MsWN8oXg";
        String appId = "39619a59-5a0c-4f9b-87c5-816c648ff357";
        String appPassword = "";
        CredentialProviderImpl credentials = new CredentialProviderImpl(appId, appPassword);

        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(header, credentials, "https://webchat.botframework.com/").get();
        Assert.assertTrue(identity.isAuthenticated());
    }

    @Test
    public void AuthHeaderWithWrongAppIdAndServiceUrl() throws ExecutionException, InterruptedException {
        String header = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IkdDeEFyWG9OOFNxbzdQd2VBNy16NjVkZW5KUSIsIng1dCI6IkdDeEFyWG9OOFNxbzdQd2VBNy16NjVkZW5KUSJ9.eyJzZXJ2aWNldXJsIjoiaHR0cHM6Ly93ZWJjaGF0LmJvdGZyYW1ld29yay5jb20vIiwiaXNzIjoiaHR0cHM6Ly9hcGkuYm90ZnJhbWV3b3JrLmNvbSIsImF1ZCI6IjM5NjE5YTU5LTVhMGMtNGY5Yi04N2M1LTgxNmM2NDhmZjM1NyIsImV4cCI6MTUxNjczNzUyMCwibmJmIjoxNTE2NzM2OTIwfQ.TBgpxbDS-gx1wm7ldvl7To-igfskccNhp-rU1mxUMtGaDjnsU--usH4OXZfzRsZqMlnXWXug_Hgd_qOr5RH8wVlnXnMWewoZTSGZrfp8GOd7jHF13Gz3F1GCl8akc3jeK0Ppc8R_uInpuUKa0SopY0lwpDclCmvDlz4PN6yahHkt_666k-9UGmRt0DDkxuYjbuYG8EDZxyyAhr7J6sFh3yE2UGRpJjRDB4wXWqv08Cp0Gn9PAW2NxOyN8irFzZH5_YZqE3DXDAYZ_IOLpygXQR0O-bFIhLDVxSz6uCeTBRjh8GU7XJ_yNiRDoaby7Rd2IfRrSnvMkBRsB8MsWN8oXg";
        String appId = "00000000-0000-0000-0000-000000000000";
        String appPassword = "";
        CredentialProviderImpl credentials = new CredentialProviderImpl(appId, appPassword);

        try {
            JwtTokenValidation.validateAuthHeader(header, credentials, "https://webchat.botframework.com/").get();
            Assert.fail("expected exception was not occurred.");
        } catch (AuthenticationException e) {
            Assert.assertEquals(e.getMessage(), "Invalid AppId passed on token: '39619a59-5a0c-4f9b-87c5-816c648ff357'.");
        }
    }

    @Test
    public void AuthHeaderWithAppIdAndWrongServiceUrl() throws ExecutionException, InterruptedException {
        String header = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IkdDeEFyWG9OOFNxbzdQd2VBNy16NjVkZW5KUSIsIng1dCI6IkdDeEFyWG9OOFNxbzdQd2VBNy16NjVkZW5KUSJ9.eyJzZXJ2aWNldXJsIjoiaHR0cHM6Ly93ZWJjaGF0LmJvdGZyYW1ld29yay5jb20vIiwiaXNzIjoiaHR0cHM6Ly9hcGkuYm90ZnJhbWV3b3JrLmNvbSIsImF1ZCI6IjM5NjE5YTU5LTVhMGMtNGY5Yi04N2M1LTgxNmM2NDhmZjM1NyIsImV4cCI6MTUxNjczNzUyMCwibmJmIjoxNTE2NzM2OTIwfQ.TBgpxbDS-gx1wm7ldvl7To-igfskccNhp-rU1mxUMtGaDjnsU--usH4OXZfzRsZqMlnXWXug_Hgd_qOr5RH8wVlnXnMWewoZTSGZrfp8GOd7jHF13Gz3F1GCl8akc3jeK0Ppc8R_uInpuUKa0SopY0lwpDclCmvDlz4PN6yahHkt_666k-9UGmRt0DDkxuYjbuYG8EDZxyyAhr7J6sFh3yE2UGRpJjRDB4wXWqv08Cp0Gn9PAW2NxOyN8irFzZH5_YZqE3DXDAYZ_IOLpygXQR0O-bFIhLDVxSz6uCeTBRjh8GU7XJ_yNiRDoaby7Rd2IfRrSnvMkBRsB8MsWN8oXg";
        String appId = "39619a59-5a0c-4f9b-87c5-816c648ff357";
        String appPassword = "";
        CredentialProviderImpl credentials = new CredentialProviderImpl(appId, appPassword);

        try {
            JwtTokenValidation.validateAuthHeader(header, credentials, "https://skype.botframework.com/").get();
            Assert.fail("expected exception was not occurred.");
        } catch (AuthenticationException e) {
            Assert.assertEquals(e.getMessage(), "'serviceurl' claim does not match service url provided (https://skype.botframework.com/).");
        }
    }

    @Test
    public void AuthHeaderEmulatorWithoutAppIdAndServiceUrl() throws ExecutionException, InterruptedException {
        String header = "";
        String appId = "";
        String appPassword = "";
        CredentialProviderImpl credentials = new CredentialProviderImpl(appId, appPassword);

        ClaimsIdentity identity = JwtTokenValidation.validateAuthHeader(header, credentials).get();
        Assert.assertTrue(identity.isAuthenticated());
    }
}
