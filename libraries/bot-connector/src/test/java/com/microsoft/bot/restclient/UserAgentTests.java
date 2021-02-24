/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.restclient;

import com.microsoft.bot.restclient.interceptors.UserAgentInterceptor;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Retrofit;

import java.io.IOException;

public class UserAgentTests {
    @Test
    public void defaultUserAgentTests() throws Exception {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor())
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        String header = chain.request().header("User-Agent");
                        Assert.assertEquals("AutoRest-Java", header);
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
        Response response = serviceClient.httpClient()
                .newCall(new Request.Builder().get().url("http://localhost").build()).execute();
        Assert.assertEquals(200, response.code());
    }

    @Test
    public void customUserAgentTests() throws Exception {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor().withUserAgent("Awesome"))
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        String header = chain.request().header("User-Agent");
                        Assert.assertEquals("Awesome", header);
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
        Response response = serviceClient.httpClient()
                .newCall(new Request.Builder().get().url("http://localhost").build()).execute();
        Assert.assertEquals(200, response.code());
    }
}
