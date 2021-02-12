package com.microsoft.bot.connector;

import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.SkillValidation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SkillValidationTests {
    @Test
    public void IsSkillClaimTest() {
        Map<String, String> claims = new HashMap<>();
        String audience = UUID.randomUUID().toString();
        String appId = UUID.randomUUID().toString();

        // Empty list of claims
        Assert.assertFalse(SkillValidation.isSkillClaim(claims));

        // No Audience claim
        claims.put(AuthenticationConstants.VERSION_CLAIM, "1.0");
        Assert.assertFalse(SkillValidation.isSkillClaim(claims));

        // Emulator Audience claim
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, AuthenticationConstants.TO_BOT_FROM_CHANNEL_TOKEN_ISSUER);
        Assert.assertFalse(SkillValidation.isSkillClaim(claims));

        // No AppId claim
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, audience);
        Assert.assertFalse(SkillValidation.isSkillClaim(claims));

        // AppId != Audience
        claims.put(AuthenticationConstants.APPID_CLAIM, audience);
        Assert.assertFalse(SkillValidation.isSkillClaim(claims));

        // Anonymous skill app id
        claims.put(AuthenticationConstants.APPID_CLAIM, AuthenticationConstants.ANONYMOUS_SKILL_APPID);
        Assert.assertTrue(SkillValidation.isSkillClaim(claims));

        // All checks pass, should be good now
        claims.put(AuthenticationConstants.APPID_CLAIM, appId);
        Assert.assertTrue(SkillValidation.isSkillClaim(claims));
    }

    @Test
    public void IsSkillTokenTest() {
        Assert.assertEquals("Null String", false, SkillValidation.isSkillToken(null));
        Assert.assertEquals("Empty String", false, SkillValidation.isSkillToken(""));
        Assert.assertEquals("No token Part", false, SkillValidation.isSkillToken("Bearer"));
        Assert.assertEquals("No bearer part", false, SkillValidation.isSkillToken("ew0KICAiYWxnIjogIlJTMjU2IiwNCiAgImtpZCI6ICJKVzNFWGRudy13WTJFcUxyV1RxUTJyVWtCLWciLA0KICAieDV0IjogIkpXM0VYZG53LXdZMkVxTHJXVHFRMnJVa0ItZyIsDQogICJ0eXAiOiAiSldUIg0KfQ.ew0KICAic2VydmljZXVybCI6ICJodHRwczovL2RpcmVjdGxpbmUuYm90ZnJhbWV3b3JrLmNvbS8iLA0KICAibmJmIjogMTU3MTE5MDM0OCwNCiAgImV4cCI6IDE1NzExOTA5NDgsDQogICJpc3MiOiAiaHR0cHM6Ly9hcGkuYm90ZnJhbWV3b3JrLmNvbSIsDQogICJhdWQiOiAiNGMwMDM5ZTUtNjgxNi00OGU4LWIzMTMtZjc3NjkxZmYxYzVlIg0KfQ.cEVHmQCTjL9HVHGk91sja5CqjgvM7B-nArkOg4bE83m762S_le94--GBb0_7aAy6DCdvkZP0d4yWwbpfOkukEXixCDZQM2kWPcOo6lz_VIuXxHFlZAGrTvJ1QkBsg7vk-6_HR8XSLJQZoWrVhE-E_dPj4GPBKE6s1aNxYytzazbKRAEYa8Cn4iVtuYbuj4XfH8PMDv5aC0APNvfgTGk-BlIiP6AGdo4JYs62lUZVSAYg5VLdBcJYMYcKt-h2n1saeapFDVHx_tdpRuke42M4RpGH_wzICeWC5tTExWEkQWApU85HRA5zzk4OpTv17Ct13JCvQ7cD5x9RK5f7CMnbhQ"));
        Assert.assertEquals("Invalid scheme", false, SkillValidation.isSkillToken("Potato ew0KICAiYWxnIjogIlJTMjU2IiwNCiAgImtpZCI6ICJKVzNFWGRudy13WTJFcUxyV1RxUTJyVWtCLWciLA0KICAieDV0IjogIkpXM0VYZG53LXdZMkVxTHJXVHFRMnJVa0ItZyIsDQogICJ0eXAiOiAiSldUIg0KfQ.ew0KICAic2VydmljZXVybCI6ICJodHRwczovL2RpcmVjdGxpbmUuYm90ZnJhbWV3b3JrLmNvbS8iLA0KICAibmJmIjogMTU3MTE5MDM0OCwNCiAgImV4cCI6IDE1NzExOTA5NDgsDQogICJpc3MiOiAiaHR0cHM6Ly9hcGkuYm90ZnJhbWV3b3JrLmNvbSIsDQogICJhdWQiOiAiNGMwMDM5ZTUtNjgxNi00OGU4LWIzMTMtZjc3NjkxZmYxYzVlIg0KfQ.cEVHmQCTjL9HVHGk91sja5CqjgvM7B-nArkOg4bE83m762S_le94--GBb0_7aAy6DCdvkZP0d4yWwbpfOkukEXixCDZQM2kWPcOo6lz_VIuXxHFlZAGrTvJ1QkBsg7vk-6_HR8XSLJQZoWrVhE-E_dPj4GPBKE6s1aNxYytzazbKRAEYa8Cn4iVtuYbuj4XfH8PMDv5aC0APNvfgTGk-BlIiP6AGdo4JYs62lUZVSAYg5VLdBcJYMYcKt-h2n1saeapFDVHx_tdpRuke42M4RpGH_wzICeWC5tTExWEkQWApU85HRA5zzk4OpTv17Ct13JCvQ7cD5x9RK5f7CMnbhQ"));
        Assert.assertEquals("To bot v2 from webchat", false, SkillValidation.isSkillToken("Bearer ew0KICAiYWxnIjogIlJTMjU2IiwNCiAgImtpZCI6ICJKVzNFWGRudy13WTJFcUxyV1RxUTJyVWtCLWciLA0KICAieDV0IjogIkpXM0VYZG53LXdZMkVxTHJXVHFRMnJVa0ItZyIsDQogICJ0eXAiOiAiSldUIg0KfQ.ew0KICAic2VydmljZXVybCI6ICJodHRwczovL2RpcmVjdGxpbmUuYm90ZnJhbWV3b3JrLmNvbS8iLA0KICAibmJmIjogMTU3MTE5MDM0OCwNCiAgImV4cCI6IDE1NzExOTA5NDgsDQogICJpc3MiOiAiaHR0cHM6Ly9hcGkuYm90ZnJhbWV3b3JrLmNvbSIsDQogICJhdWQiOiAiNGMwMDM5ZTUtNjgxNi00OGU4LWIzMTMtZjc3NjkxZmYxYzVlIg0KfQ.cEVHmQCTjL9HVHGk91sja5CqjgvM7B-nArkOg4bE83m762S_le94--GBb0_7aAy6DCdvkZP0d4yWwbpfOkukEXixCDZQM2kWPcOo6lz_VIuXxHFlZAGrTvJ1QkBsg7vk-6_HR8XSLJQZoWrVhE-E_dPj4GPBKE6s1aNxYytzazbKRAEYa8Cn4iVtuYbuj4XfH8PMDv5aC0APNvfgTGk-BlIiP6AGdo4JYs62lUZVSAYg5VLdBcJYMYcKt-h2n1saeapFDVHx_tdpRuke42M4RpGH_wzICeWC5tTExWEkQWApU85HRA5zzk4OpTv17Ct13JCvQ7cD5x9RK5f7CMnbhQ"));
        Assert.assertEquals("To bot v1 token from emulator", false, SkillValidation.isSkillToken("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImFQY3R3X29kdlJPb0VOZzNWb09sSWgydGlFcyIsImtpZCI6ImFQY3R3X29kdlJPb0VOZzNWb09sSWgydGlFcyJ9.eyJhdWQiOiI0YzMzYzQyMS1mN2QzLTRiNmMtOTkyYi0zNmU3ZTZkZTg3NjEiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIvIiwiaWF0IjoxNTcxMTg5ODczLCJuYmYiOjE1NzExODk4NzMsImV4cCI6MTU3MTE5Mzc3MywiYWlvIjoiNDJWZ1lLaWJGUDIyMUxmL0NjL1Yzai8zcGF2RUFBPT0iLCJhcHBpZCI6IjRjMzNjNDIxLWY3ZDMtNGI2Yy05OTJiLTM2ZTdlNmRlODc2MSIsImFwcGlkYWNyIjoiMSIsImlkcCI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0L2Q2ZDQ5NDIwLWYzOWItNGRmNy1hMWRjLWQ1OWE5MzU4NzFkYi8iLCJ0aWQiOiJkNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIiLCJ1dGkiOiJOdXJ3bTVOQnkwR2duT3dKRnFVREFBIiwidmVyIjoiMS4wIn0.GcKs3XZ_4GONVsAoPYI7otqUZPoNN8pULUnlJMxQa-JKXRKV0KtvTAdcMsfYudYxbz7HwcNYerFT1q3RZAimJFtfF4x_sMN23yEVxsQmYQrsf2YPmEsbCfNiEx0YEoWUdS38R1N0Iul2P_P_ZB7XreG4aR5dT6lY5TlXbhputv9pi_yAU7PB1aLuB05phQme5NwJEY22pUfx5pe1wVHogI0JyNLi-6gdoSL63DJ32tbQjr2DNYilPVtLsUkkz7fTky5OKd4p7FmG7P5EbEK4H5j04AGe_nIFs-X6x_FIS_5OSGK4LGA2RPnqa-JYpngzlNWVkUbnuH10AovcAprgdg"));
        Assert.assertEquals("To bot v2 token from emulator", false, SkillValidation.isSkillToken("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImFQY3R3X29kdlJPb0VOZzNWb09sSWgydGlFcyJ9.eyJhdWQiOiI0YzAwMzllNS02ODE2LTQ4ZTgtYjMxMy1mNzc2OTFmZjFjNWUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiL3YyLjAiLCJpYXQiOjE1NzExODkwMTEsIm5iZiI6MTU3MTE4OTAxMSwiZXhwIjoxNTcxMTkyOTExLCJhaW8iOiI0MlZnWUxnYWxmUE90Y2IxaEoxNzJvbmxIc3ZuQUFBPSIsImF6cCI6IjRjMDAzOWU1LTY4MTYtNDhlOC1iMzEzLWY3NzY5MWZmMWM1ZSIsImF6cGFjciI6IjEiLCJ0aWQiOiJkNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIiLCJ1dGkiOiJucEVxVTFoR1pVbXlISy1MUVdJQ0FBIiwidmVyIjoiMi4wIn0.CXcPx7LfatlRsOX4QG-jaC-guwcY3PFxpFICqwfoOTxAjHpeJNFXOpFeA3Qb5VKM6Yw5LyA9eraL5QDJB_4uMLCCKErPXMyoSm8Hw-GGZkHgFV5ciQXSXhE-IfOinqHE_0Lkt_VLR2q6ekOncnJeCR111QCqt3D8R0Ud0gvyLv_oONxDtqg7HUgNGEfioB-BDnBsO4RN7NGrWQFbyPxPmhi8a_Xc7j5Bb9jeiiIQbVaWkIrrPN31aWY1tEZLvdN0VluYlOa0EBVrzpXXZkIyWx99mpklg0lsy7mRyjuM1xydmyyGkzbiCKtODOanf8UwTjkTg5XTIluxe79_hVk2JQ"));
        Assert.assertEquals("To skill valid v1 token", true, SkillValidation.isSkillToken("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImFQY3R3X29kdlJPb0VOZzNWb09sSWgydGlFcyIsImtpZCI6ImFQY3R3X29kdlJPb0VOZzNWb09sSWgydGlFcyJ9.eyJhdWQiOiI0YzMzYzQyMS1mN2QzLTRiNmMtOTkyYi0zNmU3ZTZkZTg3NjEiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIvIiwiaWF0IjoxNTcxMTg5NjMwLCJuYmYiOjE1NzExODk2MzAsImV4cCI6MTU3MTE5MzUzMCwiYWlvIjoiNDJWZ1lJZzY1aDFXTUVPd2JmTXIwNjM5V1lLckFBPT0iLCJhcHBpZCI6IjRjMDAzOWU1LTY4MTYtNDhlOC1iMzEzLWY3NzY5MWZmMWM1ZSIsImFwcGlkYWNyIjoiMSIsImlkcCI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0L2Q2ZDQ5NDIwLWYzOWItNGRmNy1hMWRjLWQ1OWE5MzU4NzFkYi8iLCJ0aWQiOiJkNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIiLCJ1dGkiOiJhWlpOUTY3RjRVNnNmY3d0S0R3RUFBIiwidmVyIjoiMS4wIn0.Yogk9fptxxJKO8jRkk6FrlLQsAulNNgoa0Lqv2JPkswyyizse8kcwQhxOaZOotY0UBduJ-pCcrejk6k4_O_ZReYXKz8biL9Q7Z02cU9WUMvuIGpAhttz8v0VlVSyaEJVJALc5B-U6XVUpZtG9LpE6MVror_0WMnT6T9Ijf9SuxUvdVCcmAJyZuoqudodseuFI-jtCpImEapZp0wVN4BUodrBacMbTeYjdZyAbNVBqF5gyzDztMKZR26HEz91gqulYZvJJZOJO6ejnm0j62s1tqvUVRBywvnSOon-MV0Xt2Vm0irhv6ipzTXKwWhT9rGHSLj0g8r6NqWRyPRFqLccvA"));
        Assert.assertEquals("To skill valid v2 token", true, SkillValidation.isSkillToken("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImFQY3R3X29kdlJPb0VOZzNWb09sSWgydGlFcyJ9.eyJhdWQiOiI0YzAwMzllNS02ODE2LTQ4ZTgtYjMxMy1mNzc2OTFmZjFjNWUiLCJpc3MiOiJodHRwczovL2xvZ2luLm1pY3Jvc29mdG9ubGluZS5jb20vZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiL3YyLjAiLCJpYXQiOjE1NzExODk3NTUsIm5iZiI6MTU3MTE4OTc1NSwiZXhwIjoxNTcxMTkzNjU1LCJhaW8iOiI0MlZnWUpnZDROZkZKeG1tMTdPaVMvUk8wZll2QUE9PSIsImF6cCI6IjRjMzNjNDIxLWY3ZDMtNGI2Yy05OTJiLTM2ZTdlNmRlODc2MSIsImF6cGFjciI6IjEiLCJ0aWQiOiJkNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIiLCJ1dGkiOiJMc2ZQME9JVkNVS1JzZ1IyYlFBQkFBIiwidmVyIjoiMi4wIn0.SggsEbEyXDYcg6EdhK-RA1y6S97z4hwEccXc6a3ymnHP-78frZ3N8rPLsqLoK5QPGA_cqOXsX1zduA4vlFSy3MfTV_npPfsyWa1FIse96-2_3qa9DIP8bhvOHXEVZeq-r-0iF972waFyPPC_KVYWnIgAcunGhFWvLhhOUx9dPgq7824qTq45ma1rOqRoYbhhlRn6PJDymIin5LeOzDGJJ8YVLnFUgntc6_4z0P_fnuMktzar88CUTtGvR4P7XNJhS8v9EwYQujglsJNXg7LNcwV7qOxDYWJtT_UMuMAts9ctD6FkuTGX_-6FTqmdUPPUS4RWwm4kkl96F_dXnos9JA"));
    }

    @Test
    public void IdentityValidationTests() {
        String audience = UUID.randomUUID().toString();
        String appId = UUID.randomUUID().toString();
        Map<String, String> claims = new HashMap<>();
        ClaimsIdentity mockIdentity = mock(ClaimsIdentity.class);
        CredentialProvider mockCredentials = mock(CredentialProvider.class);

        // Null identity
        CompletionException exception = Assert.assertThrows(CompletionException.class, () -> {
            SkillValidation.validateIdentity(null, null).join();
        });
        Assert.assertEquals("Invalid Identity", exception.getCause().getMessage());

        // not authenticated identity
        when(mockIdentity.isAuthenticated()).thenReturn(false);
        exception = Assert.assertThrows(CompletionException.class, () -> {
            SkillValidation.validateIdentity(mockIdentity, null).join();
        });
        Assert.assertEquals("Token Not Authenticated", exception.getCause().getMessage());

        // No version claims
        when(mockIdentity.isAuthenticated()).thenReturn(true);
        when(mockIdentity.claims()).thenReturn(claims);
        exception = Assert.assertThrows(CompletionException.class, () -> {
            SkillValidation.validateIdentity(mockIdentity, null).join();
        });
        Assert.assertEquals(
            AuthenticationConstants.VERSION_CLAIM + " claim is required on skill Tokens.",
            exception.getCause().getMessage()
        );

        // No audience claim
        claims.put(AuthenticationConstants.VERSION_CLAIM, "1.0");
        exception = Assert.assertThrows(CompletionException.class, () -> {
            SkillValidation.validateIdentity(mockIdentity, null).join();
        });
        Assert.assertEquals(
            AuthenticationConstants.AUDIENCE_CLAIM + " claim is required on skill Tokens.",
            exception.getCause().getMessage()
        );

        // Invalid AppId in in appId or azp
        claims.put(AuthenticationConstants.AUDIENCE_CLAIM, audience);
        when(mockCredentials.isValidAppId(any())).thenReturn(CompletableFuture.completedFuture(true));
        exception = Assert.assertThrows(CompletionException.class, () -> {
            SkillValidation.validateIdentity(mockIdentity, mockCredentials).join();
        });
        Assert.assertEquals("Invalid appId.", exception.getCause().getMessage());

        // Invalid AppId in audience
        claims.put(AuthenticationConstants.APPID_CLAIM, appId);
        when(mockCredentials.isValidAppId(any())).thenReturn(CompletableFuture.completedFuture(false));
        exception = Assert.assertThrows(CompletionException.class, () -> {
            SkillValidation.validateIdentity(mockIdentity, mockCredentials).join();
        });
        Assert.assertEquals("Invalid audience.", exception.getCause().getMessage());

        // All checks pass (no exception thrown)
        when(mockCredentials.isValidAppId(any())).thenReturn(CompletableFuture.completedFuture(true));
        SkillValidation.validateIdentity(mockIdentity, mockCredentials).join();
    }

    @Test
    public void CreateAnonymousSkillClaimTest() {
        ClaimsIdentity sut = SkillValidation.createAnonymousSkillClaim();
        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_SKILL_APPID, JwtTokenValidation.getAppIdFromClaims(sut.claims()));
        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_AUTH_TYPE, sut.getType());
    }
}
