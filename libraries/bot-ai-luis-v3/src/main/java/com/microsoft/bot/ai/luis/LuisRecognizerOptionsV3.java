// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.Recognizer;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ResourceResponse;

import org.apache.commons.lang3.StringUtils;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Luis Recognizer Options for V3 LUIS Runtime.
 *
 */
public class LuisRecognizerOptionsV3 extends LuisRecognizerOptions {
    private final HashSet<String> dateSubtypes = new HashSet<>(
            Arrays.asList("date", "daterange", "datetime", "datetimerange", "duration", "set", "time", "timerange"));

    private final HashSet<String> geographySubtypes = new HashSet<>(
            Arrays.asList("poi", "city", "countryRegion", "continent", "state"));

    private final String metadataKey = "$instance";

    /**
     * DatetimeV2 offset. The format for the datetimeReference is ISO 8601.
     */
    private String dateTimeReference = null;

    /**
     * Dynamic lists used to recognize entities for a particular query.
     */
    private List<DynamicList> dynamicLists = null;

    /**
     * External entities recognized in query.
     */
    private List<ExternalEntity> externalEntities = null;

    /**
     * External entity recognizer to recognize external entities to pass to LUIS.
     */
    private Recognizer externalEntityRecognizer = null;

    /**
     * Value indicating whether all intents come back or only the top one. True for
     * returning all intents.
     */
    private boolean includeAllIntents = false;

    /**
     * Value indicating whether or not instance data should be included in response.
     */
    private boolean includeInstanceData = true;

    /**
     * Value indicating whether queries should be logged in LUIS. If queries should
     * be logged in LUIS in order to help build better models through active
     * learning
     */
    private boolean log = true;

    /**
     * Value indicating whether external entities should override other means of
     * recognizing entities. True if external entities should be preferred to the
     * results from LUIS models
     */
    private boolean preferExternalEntities = true;

    /**
     * The LUIS slot to use for the application. By default this uses the production
     * slot. You can find other standard slots in LuisSlot. If you specify a
     * Version, then a private version of the application is used instead of a slot.
     */
    private String slot = LuisSlot.PRODUCTION;

    /**
     * The specific version of the application to access. LUIS supports versions and
     * this is the version to use instead of a slot. If this is specified, then the
     * Slot is ignored.
     */
    private String version = null;

    /**
     * The HttpClient instance to use for http calls against the LUIS endpoint.
     */
    private OkHttpClient httpClient = new OkHttpClient();

    /**
     * The value type for a LUIS trace activity.
     */
    public static final String LUIS_TRACE_TYPE = "https://www.luis.ai/schemas/trace";

    /**
     * The context label for a LUIS trace activity.
     */
    public static final String LUIS_TRACE_LABEL = "LuisV3 Trace";

    /**
     * Gets External entity recognizer to recognize external entities to pass to
     * LUIS.
     *
     * @return externalEntityRecognizer
     */
    public Recognizer getExternalEntityRecognizer() {
        return externalEntityRecognizer;
    }

    /**
     * Sets External entity recognizer to recognize external entities to pass to
     * LUIS.
     *
     * @param externalEntityRecognizer External Recognizer instance.
     */
    public void setExternalEntityRecognizer(Recognizer externalEntityRecognizer) {
        this.externalEntityRecognizer = externalEntityRecognizer;
    }

    /**
     * Gets indicating whether all intents come back or only the top one. True for
     * returning all intents.
     *
     * @return True for returning all intents.
     */
    public boolean isIncludeAllIntents() {
        return includeAllIntents;
    }

    /**
     * Sets indicating whether all intents come back or only the top one.
     *
     * @param includeAllIntents True for returning all intents.
     */
    public void setIncludeAllIntents(boolean includeAllIntents) {
        this.includeAllIntents = includeAllIntents;
    }

    /**
     * Gets value indicating whether or not instance data should be included in
     * response.
     *
     * @return True if instance data should be included in response.
     */
    public boolean isIncludeInstanceData() {
        return includeInstanceData;
    }

    /**
     * Sets value indicating whether or not instance data should be included in
     * response.
     *
     * @param includeInstanceData True if instance data should be included in
     *                            response.
     */
    public void setIncludeInstanceData(boolean includeInstanceData) {
        this.includeInstanceData = includeInstanceData;
    }

    /**
     * Value indicating whether queries should be logged in LUIS. If queries should
     * be logged in LUIS in order to help build better models through active
     * learning
     *
     * @return True if queries should be logged in LUIS.
     */
    public boolean isLog() {
        return log;
    }

    /**
     * Value indicating whether queries should be logged in LUIS. If queries should
     * be logged in LUIS in order to help build better models through active
     * learning.
     *
     * @param log True if queries should be logged in LUIS.
     */
    public void setLog(boolean log) {
        this.log = log;
    }

    /**
     * Returns Dynamic lists used to recognize entities for a particular query.
     *
     * @return Dynamic lists used to recognize entities for a particular query
     */
    public List<DynamicList> getDynamicLists() {
        return dynamicLists;
    }

    /**
     * Sets Dynamic lists used to recognize entities for a particular query.
     *
     * @param dynamicLists to recognize entities for a particular query.
     */
    public void setDynamicLists(List<DynamicList> dynamicLists) {
        this.dynamicLists = dynamicLists;
    }

    /**
     * Gets External entities to be recognized in query.
     *
     * @return External entities to be recognized in query.
     */
    public List<ExternalEntity> getExternalEntities() {
        return externalEntities;
    }

    /**
     * Sets External entities to be recognized in query.
     *
     * @param externalEntities External entities to be recognized in query.
     */
    public void setExternalEntities(List<ExternalEntity> externalEntities) {
        this.externalEntities = externalEntities;
    }

    /**
     * Gets value indicating whether external entities should override other means
     * of recognizing entities.
     *
     * @return True if external entities should be preferred to the results from
     *         LUIS models.
     */
    public boolean isPreferExternalEntities() {
        return preferExternalEntities;
    }

    /**
     * Sets value indicating whether external entities should override other means
     * of recognizing entities.
     *
     * @param preferExternalEntities True if external entities should be preferred
     *                               to the results from LUIS models.
     */
    public void setPreferExternalEntities(boolean preferExternalEntities) {
        this.preferExternalEntities = preferExternalEntities;
    }

    /**
     * Gets datetimeV2 offset. The format for the datetimeReference is ISO 8601.
     *
     * @return The format for the datetimeReference in ISO 8601.
     */
    public String getDateTimeReference() {
        return dateTimeReference;
    }

    /**
     * Sets datetimeV2 offset.
     *
     * @param dateTimeReference The format for the datetimeReference is ISO 8601.
     */
    public void setDateTimeReference(String dateTimeReference) {
        this.dateTimeReference = dateTimeReference;
    }

    /**
     * Gets the LUIS slot to use for the application. By default this uses the
     * production slot. You can find other standard slots in LuisSlot. If you
     * specify a Version, then a private version of the application is used instead
     * of a slot.
     *
     * @return LuisSlot constant.
     */
    public String getSlot() {
        return slot;
    }

    /**
     * Sets the LUIS slot to use for the application. By default this uses the
     * production slot. You can find other standard slots in LuisSlot. If you
     * specify a Version, then a private version of the application is used instead
     * of a slot.
     *
     * @param slot LuisSlot value to use.
     */
    public void setSlot(String slot) {
        this.slot = slot;
    }

    /**
     * Gets the specific version of the application to access. LUIS supports
     * versions and this is the version to use instead of a slot. If this is
     * specified, then the Slot is ignored.
     *
     * @return Luis application version to Query.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the specific version of the application to access. LUIS supports
     * versions and this is the version to use instead of a slot.
     *
     * @param version Luis Application version. If this is specified, then the Slot
     *                is ignored.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets whether the http client.
     *
     * @return OkHttpClient used to query the Luis Service.
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Sets the http client.
     *
     * @param httpClient to use for Luis Service http calls.
     */
    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Initializes a new instance of the LuisRecognizerOptionsV3.
     *
     * @param application Luis Application instance to query.
     */
    public LuisRecognizerOptionsV3(LuisApplication application) {
        super(application);
    }

    /**
     * Internal implementation of the http request to the LUIS service and parsing
     * of the response to a Recognizer Result instance.
     *
     * @param dialogContext Context Object.
     * @param activity      Activity object to extract the utterance.
     */
    @Override
    CompletableFuture<RecognizerResult> recognizeInternal(DialogContext dialogContext, Activity activity) {
        if (externalEntityRecognizer == null) {
            return recognizeInternal(dialogContext.getContext(), activity.getText());
        }

        // call external entity recognizer
        List<ExternalEntity> originalExternalEntities = externalEntities;
        return externalEntityRecognizer.recognize(dialogContext, activity).thenCompose(matches -> {
            if (matches.getEntities() == null || matches.getEntities().toString().equals("{}")) {
                return recognizeInternal(dialogContext.getContext(), activity.getText());
            }

            List<ExternalEntity> recognizerExternalEntities = new ArrayList<>();
            JsonNode entities = matches.getEntities();
            JsonNode instance = entities.get("$instance");

            if (instance == null) {
                return recognizeInternal(dialogContext.getContext(), activity.getText());
            }

            Iterator<Map.Entry<String, JsonNode>> instanceEntitiesIterator = instance.fields();

            while (instanceEntitiesIterator.hasNext()) {
                Map.Entry<String, JsonNode> property = instanceEntitiesIterator.next();

                if (property.getKey().equals("text") || property.getKey().equals("$instance")) {
                    continue;
                }

                ArrayNode instances = (ArrayNode) instance.get(property.getKey());
                ArrayNode values = (ArrayNode) property.getValue();

                if (instances == null || values == null || instances.size() != values.size()) {
                    continue;
                }

                for (JsonNode childInstance : values) {
                    if (childInstance != null && childInstance.has("startIndex") && childInstance.has("endIndex")) {
                        int start = childInstance.get("startIndex").asInt();
                        int end = childInstance.get("endIndex").asInt();
                        recognizerExternalEntities
                                .add(new ExternalEntity(property.getKey(), start, end - start, property.getValue()));
                    }
                }
                recognizerExternalEntities
                        .addAll(originalExternalEntities == null ? new ArrayList<>() : originalExternalEntities);
                externalEntities = recognizerExternalEntities;
            }

            return recognizeInternal(dialogContext.getContext(), activity.getText()).thenApply(recognizerResult -> {
                externalEntities = originalExternalEntities;
                return recognizerResult;
            });
        });
    }

    /**
     * Internal implementation of the http request to the LUIS service and parsing
     * of the response to a Recognizer Result instance.
     *
     * @param turnContext Context Object.
     */
    @Override
    CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext) {
        return recognizeInternal(turnContext, turnContext.getActivity().getText());
    }

    private Request buildRequest(RequestBody body) {
        StringBuilder path = new StringBuilder(getApplication().getEndpoint());
        path.append(String.format("/luis/prediction/v3.0/apps/%s", getApplication().getApplicationId()));

        if (version == null) {
            path.append(String.format("/slots/%s/predict", slot));
        } else {
            path.append(String.format("/versions/%s/predict", version));
        }

        HttpUrl.Builder httpBuilder = HttpUrl.parse(path.toString()).newBuilder();

        httpBuilder.addQueryParameter("verbose", Boolean.toString(includeInstanceData));
        httpBuilder.addQueryParameter("log", Boolean.toString(log));
        httpBuilder.addQueryParameter("show-all-intents", Boolean.toString(includeAllIntents));

        Request.Builder requestBuilder = new Request.Builder().url(httpBuilder.build())
                .addHeader("Ocp-Apim-Subscription-Key", getApplication().getEndpointKey()).post(body);
        return requestBuilder.build();
    }

    private RequestBody buildRequestBody(String utterance) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ObjectNode content = JsonNodeFactory.instance.objectNode().put("query", utterance);
        ObjectNode queryOptions = JsonNodeFactory.instance.objectNode().put("preferExternalEntities",
                preferExternalEntities);

        if (StringUtils.isNotBlank(dateTimeReference)) {
            queryOptions.put("datetimeReference", dateTimeReference);
        }

        content.set("options", queryOptions);

        if (dynamicLists != null) {
            content.set("dynamicLists", mapper.valueToTree(dynamicLists));
        }

        if (externalEntities != null) {
            for (ExternalEntity entity : externalEntities) {
                entity.validate();
            }
            content.set("externalEntities", mapper.valueToTree(externalEntities));
        }

        String contentAsText = mapper.writeValueAsString(content);
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), contentAsText);
    }

    private CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext, String utterance) {

        RecognizerResult recognizerResult;
        JsonNode luisResponse = null;
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        if (utterance == null || utterance.isEmpty()) {
            recognizerResult = new RecognizerResult();
            recognizerResult.setText(utterance);
        } else {
            try {
                Request request = buildRequest(buildRequestBody(utterance));
                Response response = httpClient.newCall(request).execute();
                luisResponse = mapper.readTree(response.body().string());
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + luisResponse.toString());
                }

            } catch (IOException e) {
                CompletableFuture<RecognizerResult> exceptionResult = new CompletableFuture<>();
                exceptionResult.completeExceptionally(e);
                return exceptionResult;
            }

            JsonNode prediction = luisResponse.get("prediction");
            recognizerResult = new RecognizerResult();
            recognizerResult.setText(utterance);
            if (prediction.get("alteredQuery") != null) {
                recognizerResult.setAlteredText(prediction.get("alteredQuery").asText());
            }

            recognizerResult.setIntents(getIntents(prediction));
            recognizerResult.setEntities(getEntities(prediction));

            addProperties(prediction, recognizerResult);
            if (isIncludeAPIResults()) {
                recognizerResult.getProperties().put("luisResult", luisResponse);
            }

            if (includeInstanceData && recognizerResult.getEntities().get(metadataKey) == null) {
                ((ObjectNode) recognizerResult.getEntities()).putObject(metadataKey);
            }
        }

        return sendTraceActivity(recognizerResult, luisResponse, turnContext).thenApply(v -> recognizerResult);

    }

    private Map<String, IntentScore> getIntents(JsonNode prediction) {
        Map<String, IntentScore> intents = new LinkedHashMap<>();

        JsonNode intentsObject = prediction.get("intents");
        if (intentsObject == null) {
            return intents;
        }

        for (Iterator<Map.Entry<String, JsonNode>> it = intentsObject.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> intent = it.next();
            double score = intent.getValue().get("score").asDouble();
            String intentName = intent.getKey().replace(".", "_").replace(" ", "_");
            IntentScore intentScore = new IntentScore();
            intentScore.setScore(score);
            intents.put(intentName, intentScore);
        }

        return intents;
    }

    private String normalizeEntity(String entity) {
        // Type::Role -> Role
        String[] type = entity.split(":");
        return type[type.length - 1].replace(".", "_").replace(" ", "_");
    }

    private JsonNode getEntities(JsonNode prediction) {
        if (prediction.get("entities") == null) {
            return JsonNodeFactory.instance.objectNode();
        }

        return mapEntitiesRecursive(prediction.get("entities"), false);
    }

    // Exact Port from C#
    private JsonNode mapEntitiesRecursive(JsonNode source, boolean inInstance) {
        JsonNode result = source;
        if (!source.isArray() && source.isObject()) {
            ObjectNode nobj = JsonNodeFactory.instance.objectNode();
            // Fix datetime by reverting to simple timex
            JsonNode obj = source;
            JsonNode type = source.get("type");

            if (!inInstance && type != null && dateSubtypes.contains(type.asText())) {
                JsonNode timexs = obj.get("values");
                ArrayNode arr = JsonNodeFactory.instance.arrayNode();
                if (timexs != null) {
                    Set<String> unique = new HashSet<>();

                    for (JsonNode elt : timexs) {
                        unique.add(elt.get("timex").textValue());
                    }

                    for (String timex : unique) {
                        arr.add(timex);
                    }

                    nobj.set("timex", arr);
                }

                nobj.set("type", type);
            } else {
                // Map or remove properties
                Iterator<Map.Entry<String, JsonNode>> nodes = obj.fields();
                while (nodes.hasNext()) {
                    Map.Entry<String, JsonNode> property = (Map.Entry<String, JsonNode>) nodes.next();
                    String name = normalizeEntity(property.getKey());
                    boolean isArray = property.getValue().isArray();
                    boolean isString = property.getValue().isTextual();
                    boolean isInt = property.getValue().isInt();
                    JsonNode val = mapEntitiesRecursive(property.getValue(), inInstance || name.equals(metadataKey));

                    if (name.equals("datetime") && isArray) {
                        nobj.set("datetimeV1", val);
                    } else if (name.equals("datetimeV2") && isArray) {
                        nobj.set("datetime", val);
                    } else if (inInstance) {
                        // Correct $instance issues
                        if (name.equals("length") && isInt) {
                            int value = property.getValue().intValue();
                            if (obj.get("startIndex") != null) {
                                value += obj.get("startIndex").intValue();
                            }
                            nobj.put("endIndex", value);
                        } else if (!((isInt && name.equals("modelTypeId")) || // NOPMD
                                (isString && name.equals("role"))) // NOPMD
                        ) { // NOPMD
                            nobj.set(name, val);
                        }
                    } else {
                        // Correct non-$instance values
                        if (name.equals("unit") && isString) {
                            nobj.set("units", val);
                        } else {
                            nobj.set(name, val);
                        }
                    }
                }
            }

            result = nobj;
        } else if (source.isArray()) {
            JsonNode arr = source;
            ArrayNode narr = JsonNodeFactory.instance.arrayNode();
            for (JsonNode elt : arr) {
                // Check if element is geographyV2
                String isGeographyV2 = "";

                Iterator<Map.Entry<String, JsonNode>> nodes = elt.fields();
                while (nodes.hasNext()) {
                    Map.Entry<String, JsonNode> props = (Map.Entry<String, JsonNode>) nodes.next();

                    if (props == null) {
                        break;
                    }

                    if (props.getKey().contains("type") && geographySubtypes.contains(props.getValue().textValue())) {
                        isGeographyV2 = props.getValue().textValue();
                        break;
                    }
                }

                if (!inInstance && !isGeographyV2.isEmpty()) {
                    ObjectNode geoEntity = JsonNodeFactory.instance.objectNode();
                    nodes = elt.fields();
                    while (nodes.hasNext()) {
                        Map.Entry<String, JsonNode> tokenProp = (Map.Entry<String, JsonNode>) nodes.next();

                        if (tokenProp.getKey().contains("value")) {
                            geoEntity.set("location", tokenProp.getValue());
                        }
                    }

                    geoEntity.put("type", isGeographyV2);
                    narr.add(geoEntity);
                } else {
                    narr.add(mapEntitiesRecursive(elt, inInstance));
                }
            }
            result = narr;
        }

        return result;
    }

    private void addProperties(JsonNode prediction, RecognizerResult result) {
        JsonNode sentiment = prediction.get("sentiment");
        if (sentiment != null) {
            ObjectNode sentimentNode = JsonNodeFactory.instance.objectNode();
            sentimentNode.set("label", sentiment.get("label"));
            sentimentNode.set("score", sentiment.get("score"));
            result.getProperties().put("sentiment", sentimentNode);
        }
    }

    private CompletableFuture<ResourceResponse> sendTraceActivity(RecognizerResult recognizerResult,
            JsonNode luisResponse, TurnContext turnContext) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        try {
            ObjectNode traceInfo = JsonNodeFactory.instance.objectNode();
            traceInfo.put("recognizerResult",
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(recognizerResult));
            traceInfo.set("luisResult", luisResponse);
            traceInfo.set("luisModel",
                    JsonNodeFactory.instance.objectNode().put("ModelId", getApplication().getApplicationId()));

            ObjectNode luisOptions = JsonNodeFactory.instance.objectNode();
            luisOptions.put("includeAllIntents", includeAllIntents);
            luisOptions.put("includeInstanceData", includeInstanceData);
            luisOptions.put("log", log);
            luisOptions.put("preferExternalEntities", preferExternalEntities);
            luisOptions.put("dateTimeReference", dateTimeReference);
            luisOptions.put("slot", slot);
            luisOptions.put("version", version);

            if (externalEntities != null) {
                ArrayNode externalEntitiesNode = JsonNodeFactory.instance.arrayNode();
                for (ExternalEntity e : externalEntities) {
                    externalEntitiesNode.add(mapper.valueToTree(e));
                }
                luisOptions.put("externalEntities", externalEntitiesNode);
            }

            if (dynamicLists != null) {
                ArrayNode dynamicListNode = JsonNodeFactory.instance.arrayNode();
                for (DynamicList e : dynamicLists) {
                    dynamicListNode.add(mapper.valueToTree(e));
                }
                luisOptions.put("dynamicLists", dynamicListNode);
            }

            traceInfo.set("luisOptions", luisOptions);

            return turnContext.sendActivity(
                    Activity.createTraceActivity("LuisRecognizer", LUIS_TRACE_TYPE, traceInfo, LUIS_TRACE_LABEL));

        } catch (IOException e) {
            CompletableFuture<ResourceResponse> exceptionResult = new CompletableFuture<>();
            exceptionResult.completeExceptionally(e);
            return exceptionResult;
        }
    }
}
