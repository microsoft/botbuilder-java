// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.builder;

import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.SimpleCredentialProvider;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ResourceResponse;

import org.junit.Assert;
import org.junit.Test;

public class ChannelServiceHandlerTests {

    @Test
    public void AuthenticateSetsAnonymousSkillClaim() {
        TestChannelServiceHandler sut = new TestChannelServiceHandler();
        sut.handleReplyToActivity(null, "123", "456", new Activity(ActivityTypes.MESSAGE));

        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_AUTH_TYPE,
                    sut.getClaimsIdentity().getType());
        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_SKILL_APPID,
                    JwtTokenValidation.getAppIdFromClaims(sut.getClaimsIdentity().claims()));
    }

    @Test
    public void testHandleSendToConversation() {
        TestChannelServiceHandler sut = new TestChannelServiceHandler();
        sut.handleSendToConversation(null, "456", new Activity(ActivityTypes.MESSAGE));

        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_AUTH_TYPE,
                    sut.getClaimsIdentity().getType());
        Assert.assertEquals(AuthenticationConstants.ANONYMOUS_SKILL_APPID,
                    JwtTokenValidation.getAppIdFromClaims(sut.getClaimsIdentity().claims()));
    }


    /**
     * A {@link ChannelServiceHandler} with overrides for testings.
     */
    private class TestChannelServiceHandler extends ChannelServiceHandler {
        TestChannelServiceHandler() {
            super(new SimpleCredentialProvider(), new AuthenticationConfiguration(), null);
        }

        private ClaimsIdentity claimsIdentity;

        @Override
        protected CompletableFuture<ResourceResponse> onReplyToActivity(
            ClaimsIdentity claimsIdentity,
            String conversationId,
            String activityId,
            Activity activity
        ) {
            this.claimsIdentity = claimsIdentity;
            return CompletableFuture.completedFuture(new ResourceResponse());
        }

        @Override
        protected CompletableFuture<ResourceResponse> onSendToConversation(
            ClaimsIdentity claimsIdentity,
            String activityId,
            Activity activity
        ) {
            this.claimsIdentity = claimsIdentity;
            return CompletableFuture.completedFuture(new ResourceResponse());
        }

        /**
             * Gets the {@link ClaimsIdentity} sent to the different methods after
             * auth is done.
         * @return the ClaimsIdentity value as a getClaimsIdentity().
         */
        public ClaimsIdentity getClaimsIdentity() {
            return this.claimsIdentity;
        }

        /**
             * Gets the {@link ClaimsIdentity} sent to the different methods after
             * auth is done.
         * @param withClaimsIdentity The ClaimsIdentity value.
         */
        private void setClaimsIdentity(ClaimsIdentity withClaimsIdentity) {
            this.claimsIdentity = withClaimsIdentity;
        }

    }
}

