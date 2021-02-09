// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.microsoft.bot.builder.*;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.ResourceResponse;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


@ExtendWith(MockitoExtension.class)
public class LuisRecognizerOptionsV3Tests {

    @Mock
    DialogContext dC;

    @Mock
    Recognizer recognizer;

    // Set this values to test against the service
    String applicationId = "b31aeaf3-3511-495b-a07f-571fc873214b";
    String subscriptionKey = "b31aeaf3-3511-495b-a07f-571fc873214b";
    boolean mockLuisResponse = true;

    @ParameterizedTest
    @ValueSource(strings = {
        "Composite1.json",
        "Composite2.json",
        "Composite3.json",
        "DateTimeReference.json",
        "DynamicListsAndList.json",
        "ExternalEntitiesAndBuiltin.json",
        "ExternalEntitiesAndComposite.json",
        "ExternalEntitiesAndList.json",
        "ExternalEntitiesAndRegex.json",
        "ExternalEntitiesAndSimple.json",
        "ExternalEntitiesAndSimpleOverride.json",
        "GeoPeopleOrdinal.json",
        "Minimal.json",
//        "MinimalWithGeo.json",
        "NoEntitiesInstanceTrue.json",
        "Patterns.json",
        "Prebuilt.json",
        "roles.json",
        "TraceActivity.json",
        "Typed.json",
        "TypedPrebuilt.json"
    }) // six numbers
    public void shouldParseLuisResponsesCorrectly_TurnContextPassed(String fileName) {
        RecognizerResult  result = null, expected  = null;
        MockWebServer mockWebServer = new MockWebServer();

        try {
            // Get Oracle file
            String content = readFileContent("/src/test/java/com/microsoft/bot/ai/luis/testdata/" + fileName);

            //Extract V3 response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode testData = mapper.readTree(content);
            JsonNode v3SettingsAndResponse = testData.get("v3");
            JsonNode v3Response = v3SettingsAndResponse.get("response");

            //Extract V3 Test Settings
            JsonNode testSettings = v3SettingsAndResponse.get("options");

            // Set mock response in MockWebServer
            StringBuilder pathToMock = new StringBuilder("/luis/prediction/v3.0/apps/");
            String url = buildUrl(pathToMock, testSettings);
            String endpoint = "";
            if (this.mockLuisResponse) {
                endpoint = String.format(
                    "http://localhost:%s",
                    initializeMockServer(
                        mockWebServer,
                        v3Response,
                        url).port());
            }

            // Set LuisRecognizerOptions data
            LuisRecognizerOptionsV3 v3 = buildTestRecognizer(endpoint, testSettings);

            // Run test
            result = v3.recognizeInternal(createContext(testData.get("text").asText())).get();

            // Build expected result
            expected = mapper.readValue(content, RecognizerResult.class);
            Map<String, JsonNode> properties = expected.getProperties();
            properties.remove("v2");
            properties.remove("v3");

            assertEquals(mapper.writeValueAsString(expected), mapper.writeValueAsString(result));

            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals(String.format("POST %s HTTP/1.1", pathToMock.toString()), request.getRequestLine());
            assertEquals(pathToMock.toString(), request.getPath());

        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            assertFalse(true);
        } finally {
            try {
                mockWebServer.shutdown();
            } catch (IOException e) {
                // Empty error
            }
        }
    }

    //TODO: Enable once the class Dialog Recognizer is ported
//    @Test
//    public void shouldBuildExternalEntities_DialogContextPassed_ExternalRecognizer() {
//        MockWebServer mockWebServer = new MockWebServer();
//
//        try {
//            // Get Oracle file
//            String content = readFileContent("/src/test/java/com/microsoft/bot/ai/luis/testdata/ExternalRecognizer.json");
//
//            //Extract V3 response
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode testData = mapper.readTree(content);
//            JsonNode v3SettingsAndResponse = testData.get("v3");
//            JsonNode v3Response = v3SettingsAndResponse.get("response");
//
//            //Extract V3 Test Settings
//            JsonNode testSettings = v3SettingsAndResponse.get("options");
//
//            // Set mock response in MockWebServer
//            StringBuilder pathToMock = new StringBuilder("/luis/prediction/v3.0/apps/");
//            String url = buildUrl(pathToMock, testSettings);
//            String endpoint = String.format(
//                "http://localhost:%s",
//                initializeMockServer(
//                    mockWebServer,
//                    v3Response,
//                    url).port());
//
//            // Set LuisRecognizerOptions data
//            LuisRecognizerOptionsV3 v3 = buildTestRecognizer(endpoint, testSettings);
//            v3.setExternalEntityRecognizer(recognizer);
//            TurnContext tC = createContext(testData.get("text").asText());
//            when(dC.getContext()).thenReturn(tC);
//
//            doReturn(CompletableFuture.supplyAsync(() -> new RecognizerResult(){{
//                setEntities(testSettings.get("ExternalRecognizerResult"));
//            }}))
//                .when(recognizer)
//                .recognize(any(TurnContext.class));
//
//            v3.recognizeInternal(dC, tC.getActivity()).get();
//
//            RecordedRequest request = mockWebServer.takeRequest();
//            String resultBody = request.getBody().readUtf8();
//            assertEquals("{\"query\":\"deliver 35 WA to repent harelquin\"," +
//                    "\"options\":{\"preferExternalEntities\":true}," +
//                    "\"externalEntities\":[{\"entityName\":\"Address\",\"startIndex\":17,\"entityLength\":16," +
//                    "\"resolution\":[{\"endIndex\":33,\"modelType\":\"Composite Entity Extractor\"," +
//                    "\"resolution\":{\"number\":[3],\"State\":[\"France\"]}," +
//                    "\"startIndex\":17,\"text\":\"repent harelquin\",\"type\":\"Address\"}]}]}",
//                resultBody);
//
//        } catch (InterruptedException | ExecutionException | IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                mockWebServer.shutdown();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static TurnContext createContext(String message) {

        Activity activity = new Activity() {
            {
                setText(message);
                setType(ActivityTypes.MESSAGE);
                setChannelId("EmptyContext");
            }
        };

        return new TurnContextImpl(new NotImplementedAdapter(), activity);
    }

    private static class NotImplementedAdapter extends BotAdapter {
        @Override
        public CompletableFuture<ResourceResponse[]> sendActivities(
            TurnContext context,
            List<Activity> activities
        ) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<ResourceResponse> updateActivity(
            TurnContext context,
            Activity activity
        ) {
            throw new RuntimeException();
        }

        @Override
        public CompletableFuture<Void> deleteActivity(
            TurnContext context,
            ConversationReference reference
        ) {
            throw new RuntimeException();
        }
    }

    private String readFileContent (String pathToFile) throws IOException {
        String path = Paths.get("").toAbsolutePath().toString();
        File file = new File(path + pathToFile);
        return FileUtils.readFileToString(file, "utf-8");
    }

    private String buildUrl(StringBuilder pathToMock, JsonNode testSettings) {
        pathToMock.append(this.applicationId);

        if (testSettings.get("Version") != null ) {
            pathToMock.append(String.format("/versions/%s/predict", testSettings.get("Version").asText()));
        } else {
            pathToMock.append(String.format("/slots/%s/predict", testSettings.get("Slot").asText()));
        }
        pathToMock.append(
            String.format(
                "?verbose=%s&log=%s&show-all-intents=%s",
                testSettings.get("IncludeInstanceData").asText(),
                testSettings.get("Log").asText(),
                testSettings.get("IncludeAllIntents").asText()
            )
        );

        return pathToMock.toString();
    }

    private HttpUrl initializeMockServer(MockWebServer mockWebServer, JsonNode v3Response, String url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String mockResponse = mapper.writeValueAsString(v3Response);
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(mockResponse));

        mockWebServer.start();

        return mockWebServer.url(url);
    }

    private LuisRecognizerOptionsV3 buildTestRecognizer (String endpoint, JsonNode testSettings) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader readerDynamicList = mapper.readerFor(new TypeReference<List<DynamicList>>() {});
        ObjectReader readerExternalentities = mapper.readerFor(new TypeReference<List<ExternalEntity>>() {});
        return new LuisRecognizerOptionsV3(
            new LuisApplication(
                this.applicationId,
                this.subscriptionKey,
                endpoint)) {{
            setIncludeInstanceData(testSettings.get("IncludeInstanceData").asBoolean());
            setIncludeAllIntents(testSettings.get("IncludeAllIntents").asBoolean());
            setVersion(testSettings.get("Version") == null ? null : testSettings.get("Version").asText());
            setDynamicLists(testSettings.get("DynamicLists") == null ? null : readerDynamicList.readValue(testSettings.get("DynamicLists")));
            setExternalEntities(testSettings.get("ExternalEntities") == null ? null : readerExternalentities.readValue(testSettings.get("ExternalEntities")));
        }};
    }

}
