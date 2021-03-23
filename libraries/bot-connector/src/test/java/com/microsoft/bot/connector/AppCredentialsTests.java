// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.connector;

import java.io.IOException;
import java.net.MalformedURLException;

import com.microsoft.bot.connector.authentication.AppCredentials;
import com.microsoft.bot.connector.authentication.AppCredentialsInterceptor;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;
import com.microsoft.bot.connector.authentication.Authenticator;
import com.microsoft.bot.restclient.ServiceClient;

import org.junit.Assert;
import org.junit.Test;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class AppCredentialsTests {

    @Test
    public void ConstructorTests() {
        TestAppCredentials shouldDefaultToChannelScope = new TestAppCredentials("irrelevant");
        Assert.assertEquals(AuthenticationConstants.TO_CHANNEL_FROM_BOT_OAUTH_SCOPE,
                            shouldDefaultToChannelScope.oAuthScope());

        TestAppCredentials shouldDefaultToCustomScope = new TestAppCredentials("irrelevant", "customScope");
        Assert.assertEquals("customScope", shouldDefaultToCustomScope.oAuthScope());
    }

    @Test
    public void basicCredentialsTest() throws Exception {
        TestAppCredentials credentials = new TestAppCredentials("irrelevant", "pass");
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        credentials.applyCredentialsFilter(clientBuilder);
        clientBuilder.addInterceptor(
                new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        String header = chain.request().header("Authorization");
                        Assert.assertNull(header);
                        return new Response.Builder()
                                .request(chain.request())
                                .code(200)
                                .message("OK")
                                .protocol(Protocol.HTTP_1_1)
                                .body(ResponseBody.create(MediaType.parse("text/plain"), "azure rocks"))
                                .build();
                    }
                });
        ServiceClient serviceClient = new ServiceClient("http://localhost", clientBuilder, new Retrofit.Builder()) { };
        Response response = serviceClient.httpClient().newCall(
            new Request.Builder().url("http://localhost").build()).execute();
        Assert.assertEquals(200, response.code());
    }

    private class TestAppCredentials extends AppCredentials {
        TestAppCredentials(String channelAuthTenant) {
            super(channelAuthTenant);
        }

        TestAppCredentials(String channelAuthTenant, String oAuthScope) {
            super(channelAuthTenant, oAuthScope);
        }

        @Override
        protected Authenticator buildAuthenticator() throws MalformedURLException {
            return null;
        }

        /**
         * Apply the credentials to the HTTP request.
         *
         * <p>
         * Note: Provides the same functionality as dotnet ProcessHttpRequestAsync
         * </p>
         *
         * @param clientBuilder the builder for building up an {@link OkHttpClient}
         */
        @Override
        public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
            clientBuilder.interceptors().add(new AppCredentialsInterceptor(this));
        }

    }
}

