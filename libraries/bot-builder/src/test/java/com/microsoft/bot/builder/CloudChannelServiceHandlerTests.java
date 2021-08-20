// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthentication;
import com.microsoft.bot.connector.authentication.BotFrameworkAuthenticationFactory;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ResourceResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class CloudChannelServiceHandlerTests {

    @Test
    public void authenticateSetsAnonymousSkillClaim() {
        TestCloudChannelServiceHandler sut = new TestCloudChannelServiceHandler(
            BotFrameworkAuthenticationFactory.create());
        sut.handleReplyToActivity(
            null,
            "123",
            "456",
            new Activity(ActivityTypes.MESSAGE)).join();

        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_AUTH_TYPE, sut.getClaimsIdentity().getType());
        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_SKILL_APPID, JwtTokenValidation.getAppIdFromClaims(sut.getClaimsIdentity().claims()));
    }

    private class TestCloudChannelServiceHandler extends CloudChannelServiceHandler {

        private ClaimsIdentity claimsIdentity;

        public ClaimsIdentity getClaimsIdentity() {
            return claimsIdentity;
        }

        /**
         * {@inheritDoc}
         */
        public TestCloudChannelServiceHandler(BotFrameworkAuthentication withAuth) {
            super(withAuth);
        }

        @Override
        protected CompletableFuture<ResourceResponse> onReplyToActivity(
            ClaimsIdentity claimsIdentity,
            String conversationId,
            String activityId,
            Activity activity) {
            this.claimsIdentity = claimsIdentity;
            return CompletableFuture.completedFuture(new ResourceResponse());
        }
    }
}
