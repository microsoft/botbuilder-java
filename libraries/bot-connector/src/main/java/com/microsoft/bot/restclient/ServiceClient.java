// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient;

import com.google.common.hash.Hashing;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import com.microsoft.bot.restclient.protocol.SerializerAdapter;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import java.net.NetworkInterface;
import java.util.Enumeration;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 */
public abstract class ServiceClient {
    /**
     * The RestClient instance storing all information needed for making REST calls.
     */
    private final RestClient restClient;

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service endpoint
     */
    protected ServiceClient(String baseUrl) {
        this(baseUrl, new OkHttpClient.Builder(), new Retrofit.Builder());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service base uri
     * @param clientBuilder the http client builder
     * @param restBuilder the retrofit rest client builder
     */
    protected ServiceClient(String baseUrl, OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        this(new RestClient.Builder(clientBuilder, restBuilder)
                .withBaseUrl(baseUrl)
                .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
                .withSerializerAdapter(new JacksonAdapter())
                .build());
    }

    protected ServiceClient(String baseUrl, ServiceClientCredentials credentials) {
        this(baseUrl, credentials, new OkHttpClient.Builder(), new Retrofit.Builder());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service base uri
     * @param credentials the credentials
     * @param clientBuilder the http client builder
     * @param restBuilder the retrofit rest client builder
     */
    protected ServiceClient(String baseUrl, ServiceClientCredentials credentials, OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        this(new RestClient.Builder(clientBuilder, restBuilder)
            .withBaseUrl(baseUrl)
            .withCredentials(credentials)
            .withSerializerAdapter(new JacksonAdapter())
            .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
            .build());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param restClient the REST client
     */
    protected ServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @return the {@link RestClient} instance.
     */
    public RestClient restClient() {
        return restClient;
    }

    /**
     * @return the Retrofit instance.
     */
    public Retrofit retrofit() {
        return restClient.retrofit();
    }

    /**
     * @return the HTTP client.
     */
    public OkHttpClient httpClient() {
        return this.restClient.httpClient();
    }

    /**
     * @return the adapter to a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}.
     */
    public SerializerAdapter<?> serializerAdapter() {
        return this.restClient.serializerAdapter();
    }

    /**
     * The default User-Agent header. Override this method to override the user agent.
     *
     * @return the user agent string.
     */
    public String userAgent() {
        return String.format("Azure-SDK-For-Java/%s OS:%s MacAddressHash:%s Java:%s",
            getClass().getPackage().getImplementationVersion(),
            OS,
            MAC_ADDRESS_HASH,
            JAVA_VERSION);
    }

    private static final String MAC_ADDRESS_HASH;
    private static final String OS;
    private static final String JAVA_VERSION;

    static {
        OS = System.getProperty("os.name") + "/" + System.getProperty("os.version");
        String macAddress = "Unknown";

        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null) {
                    macAddress = Hashing.sha256().hashBytes(mac).toString();
                    break;
                }
            }
        } catch (Throwable ignore) {  //NOPMD
            // It's okay ignore mac address hash telemetry
        }
        MAC_ADDRESS_HASH = macAddress;
        String version = System.getProperty("java.version");
        JAVA_VERSION = version != null ? version : "Unknown";
    }
}
