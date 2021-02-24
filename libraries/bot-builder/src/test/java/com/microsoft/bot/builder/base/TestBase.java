// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder.base;

import com.microsoft.bot.connector.authentication.MicrosoftAppCredentials;
import com.microsoft.bot.restclient.LogLevel;
import com.microsoft.bot.restclient.RestClient;
import com.microsoft.bot.restclient.ServiceResponseBuilder;
import com.microsoft.bot.restclient.credentials.ServiceClientCredentials;
import com.microsoft.bot.restclient.credentials.TokenCredentials;
import com.microsoft.bot.restclient.interceptors.LoggingInterceptor;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class TestBase {

    protected final static String ZERO_CLIENT_ID = "00000000-0000-0000-0000-000000000000";
    protected final static String ZERO_CLIENT_SECRET = "00000000000000000000000";
    protected final static String ZERO_USER_ID = "<--dummy-user-id-->";
    protected final static String ZERO_BOT_ID = "<--dummy-bot-id-->";
    protected final static String ZERO_TOKEN = "<--dummy-token-->";
    private static final String PLAYBACK_URI = "http://localhost:1234";
    protected static String hostUri = null;
    protected static String clientId = null;
    protected static String clientSecret = null;
    protected static String userId = null;
    protected static String botId = null;
    private static TestMode testMode = null;
    private final RunCondition runCondition;
    @Rule
    public TestName testName = new TestName();
    protected InterceptorManager interceptorManager = null;
    private PrintStream out;

    protected TestBase() {
        this(RunCondition.BOTH);
    }

    protected TestBase(RunCondition runCondition) {
        this.runCondition = runCondition;
    }

    private static void initTestMode() throws IOException {
        String azureTestMode = System.getenv("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            if (azureTestMode.equalsIgnoreCase("Record")) {
                testMode = TestMode.RECORD;
            } else if (azureTestMode.equalsIgnoreCase("Playback")) {
                testMode = TestMode.PLAYBACK;
            } else {
                throw new IOException("Unknown AZURE_TEST_MODE: " + azureTestMode);
            }
        } else {
            System.out.print(
                "Environment variable 'AZURE_TEST_MODE' has not been set yet. Using 'PLAYBACK' mode."
            );
            testMode = TestMode.RECORD;
        }
    }

    private static void initParams() {
        try {
            Properties mavenProps = new Properties();
            InputStream in = TestBase.class.getResourceAsStream("/maven.properties");
            if (in == null) {
                throw new IOException(
                    "The file \"maven.properties\" has not been generated yet. Please execute \"mvn compile\" to generate the file."
                );
            }
            mavenProps.load(in);

            clientId = mavenProps.getProperty("clientId");
            clientSecret = mavenProps.getProperty("clientSecret");
            hostUri = mavenProps.getProperty("hostUrl");
            userId = mavenProps.getProperty("userId");
            botId = mavenProps.getProperty("botId");
        } catch (IOException e) {
            clientId = ZERO_CLIENT_ID;
            clientSecret = ZERO_CLIENT_SECRET;
            hostUri = PLAYBACK_URI;
            userId = ZERO_USER_ID;
            botId = ZERO_BOT_ID;
        }
    }

    public static boolean isPlaybackMode() {
        if (testMode == null)
            try {
                initTestMode();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Can't init test mode.");
            }
        return testMode == TestMode.PLAYBACK;
    }

    public static boolean isRecordMode() {
        return !isPlaybackMode();
    }

    private static void printThreadInfo(String what) {
        long id = Thread.currentThread().getId();
        String name = Thread.currentThread().getName();
        System.out.println(String.format("\n***\n*** [%s:%s] - %s\n***\n", name, id, what));
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        printThreadInfo("beforeClass");
        initTestMode();
        initParams();
    }

    private String shouldCancelTest(boolean isPlaybackMode) {
        // Determine whether to run the test based on the condition the test has been
        // configured with
        switch (this.runCondition) {
            case MOCK_ONLY:
                return (!isPlaybackMode)
                    ? "Test configured to run only as mocked, not live."
                    : null;

            case LIVE_ONLY:
                return (isPlaybackMode) ? "Test configured to run only as live, not mocked." : null;

            default:
                return null;
        }
    }

    @Before
    public void beforeTest() throws IOException {
        printThreadInfo(String.format("%s: %s", "beforeTest", testName.getMethodName()));
        final String skipMessage = shouldCancelTest(isPlaybackMode());
        Assume.assumeTrue(skipMessage, skipMessage == null);

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        ServiceClientCredentials credentials;
        RestClient restClient;

        if (isPlaybackMode()) {
            credentials = new TokenCredentials(null, ZERO_TOKEN);
            restClient = buildRestClient(
                new RestClient.Builder().withBaseUrl(hostUri + "/").withSerializerAdapter(new JacksonAdapter()).withResponseBuilderFactory(new ServiceResponseBuilder.Factory()).withCredentials(credentials).withLogLevel(LogLevel.NONE).withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS)).withInterceptor(interceptorManager.initInterceptor()), true
            );

            out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    // DO NOTHING
                }
            }));
        } else { // Record mode
            credentials = new MicrosoftAppCredentials(clientId, clientSecret);
            restClient = buildRestClient(
                new RestClient.Builder().withBaseUrl(hostUri + "/").withSerializerAdapter(new JacksonAdapter()).withResponseBuilderFactory(new ServiceResponseBuilder.Factory()).withCredentials(credentials).withLogLevel(LogLevel.NONE).withReadTimeout(3, TimeUnit.MINUTES).withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS)).withInterceptor(interceptorManager.initInterceptor()), false
            );

            // interceptorManager.addTextReplacementRule(hostUri, PLAYBACK_URI);
        }
        initializeClients(restClient, botId, userId);
    }

    @After
    public void afterTest() throws IOException {
        if (shouldCancelTest(isPlaybackMode()) != null) {
            return;
        }
        cleanUpResources();
        interceptorManager.finalizeInterceptor();
    }

    protected void addTextReplacementRule(String from, String to) {
        interceptorManager.addTextReplacementRule(from, to);
    }

    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        return builder.build();
    }

    protected abstract void initializeClients(
        RestClient restClient,
        String botId,
        String userId
    ) throws IOException;

    protected abstract void cleanUpResources();

    protected enum RunCondition {
        MOCK_ONLY,
        LIVE_ONLY,
        BOTH
    }

    public enum TestMode {
        PLAYBACK,
        RECORD
    }
}
