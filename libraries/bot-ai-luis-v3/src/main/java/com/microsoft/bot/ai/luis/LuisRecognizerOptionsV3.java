package com.microsoft.bot.ai.luis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.Recognizer;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.Activity;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LuisRecognizerOptionsV3 extends LuisRecognizerOptions
{
    private final HashSet<String> dateSubtypes = new HashSet<String> (Arrays.asList("date", "daterange", "datetime", "datetimerange", "duration", "set", "time", "timerange" ));
    private final HashSet<String> geographySubtypes = new HashSet<String> (Arrays.asList("poi", "city", "countryRegion", "continent", "state"));
    private final String metadataKey = "$instance";
    /// <summary>
    /// The value type for a LUIS trace activity.
    /// </summary>
    public static final String luisTraceType = "https://www.luis.ai/schemas/trace";

    /// <summary>
    /// The context label for a LUIS trace activity.
    /// </summary>
    public static final String LuisTraceLabel = "LuisV3 Trace";

    /// <summary>
    /// Initializes a new instance of the <see cref="LuisRecognizerOptionsV3"/> class.
    /// </summary>
    /// <param name="application">The LUIS application to use to recognize text.</param>
    public LuisRecognizerOptionsV3(LuisApplication application) {
        super(application);
    }

    /// <summary>
    /// Gets or sets entity recognizer to recognize external entities to pass to LUIS.
    /// </summary>
    /// <value>External entity recognizer.</value>
    public Recognizer externalEntityRecognizer;

    /// <summary>
    /// Gets or sets a value indicating whether all intents come back or only the top one.
    /// </summary>
    /// <value>
    /// True for returning all intents.
    /// </value>
    public boolean includeAllIntents = false;

    /// <summary>
    /// Gets or sets a value indicating whether or not instance data should be included in response.
    /// </summary>
    /// <value>
    /// A value indicating whether or not instance data should be included in response.
    /// </value>
    public boolean includeInstanceData  = false;

    /// <summary>
    /// Gets or sets a value indicating whether queries should be logged in LUIS.
    /// </summary>
    /// <value>
    /// If queries should be logged in LUIS in order to help build better models through active learning.
    /// </value>
    /// <remarks>The default is to log queries to LUIS in order to support active learning.  To default to the Luis setting set to null.</remarks>
    public boolean log  = true;

    /// <summary>
    /// Gets or sets dynamic lists used to recognize entities for a particular query.
    /// </summary>
    /// <value>
    /// Dynamic lists of things like contact names to recognize at query time.
    /// </value>
    public List<DynamicList> dynamicLists = null;

    /// <summary>
    /// Gets or sets external entities recognized in the query.
    /// </summary>
    /// <value>
    /// External entities recognized in query.
    /// </value>
    public List<ExternalEntity> externalEntities = null;

    /// <summary>
    /// Gets or sets a value indicating whether external entities should override other means of recognizing entities.
    /// </summary>
    /// <value>
    /// Boolean for if external entities should be preferred to the results from LUIS models.
    /// </value>
    public boolean preferExternalEntities = true;

    /// <summary>
    /// Gets or sets datetimeV2 offset. The format for the datetimeReference is ISO 8601.
    /// </summary>
    /// <value>
    /// DateTimeReference.
    /// </value>
    public String dateTimeReference = null;

    /// <summary>
    /// Gets or sets the LUIS slot to use for the application.
    /// </summary>
    /// <value>
    /// The LUIS slot to use for the application.
    /// </value>
    /// <remarks>
    /// By default this uses the production slot.  You can find other standard slots in <see cref="LuisSlot"/>.
    /// If you specify a Version, then a private version of the application is used instead of a slot.
    /// </remarks>
    public String slot = "production";

    /// <summary>
    /// Gets or sets the specific version of the application to access.
    /// </summary>
    /// <value>
    /// Version to access.
    /// </value>
    /// <remarks>
    /// LUIS supports versions and this is the version to use instead of a slot.
    /// If this is specified, then the <see cref="Slot"/> is ignored.
    /// </remarks>
    public String version = null;

//        @Override
//        protected CompletableFuture<RecognizerResult> recognizeInternalAsync(DialogContext dialogContext, Activity activity, OkHttpClient httpClient) {
//            //TODO: Enable external recognizer
//            var options = PredictionOptions;
//            if (ExternalEntityRecognizer != null)
//            {
//                // call external entity recognizer
//                var matches = await ExternalEntityRecognizer.RecognizeAsync(dialogContext, activity, cancellationToken).ConfigureAwait(false);
//                if (matches.Entities != null && matches.Entities.Count > 0)
//                {
//                    options = new LuisV3.LuisPredictionOptions(options);
//                    options.ExternalEntities = new List<LuisV3.ExternalEntity>();
//                    var entities = matches.Entities;
//                    var instance = entities["$instance"].ToObject<JObject>();
//                    if (instance != null)
//                    {
//                        foreach (var child in entities)
//                        {
//                            // TODO: Checking for "text" because we get an extra non-real entity from the text recognizers
//                            if (child.Key != "text" && child.Key != "$instance")
//                            {
//                                var instances = instance[child.Key]?.ToObject<JArray>();
//                                var values = child.Value.ToObject<JArray>();
//                                if (instances != null && values != null
//                                    && instances.Count == values.Count)
//                                {
//                                    for (var i = 0; i < values.Count; ++i)
//                                    {
//                                        var childInstance = instances[i].ToObject<JObject>();
//                                        if (childInstance != null
//                                            && childInstance.ContainsKey("startIndex")
//                                            && childInstance.ContainsKey("endIndex"))
//                                        {
//                                            var start = childInstance["startIndex"].Value<int>();
//                                            var end = childInstance["endIndex"].Value<int>();
//                                            options.ExternalEntities.Add(new LuisV3.ExternalEntity(child.Key, start, end - start, child.Value));
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }

        // call luis recognizer with options.ExternalEntities populated from externalEntityRecognizer.
//            return recognizeInternal(dialogContext, activity.getText(), httpClient).ConfigureAwait(false);
//        }

    @Override
    protected CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext, OkHttpClient httpClient) {
        return recognizeInternal(turnContext, turnContext.getActivity().getText(), httpClient);
    }

    private RequestBody BuildRequestBody(String utterance) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode content = JsonNodeFactory.instance.objectNode().put("query", utterance);
        ObjectNode queryOptions = JsonNodeFactory.instance.objectNode().put("preferExternalEntities", preferExternalEntities);

        if (dateTimeReference != null && !dateTimeReference.isEmpty()) {
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

        return RequestBody.create(content.asText(),  MediaType.parse("application/json; charset=utf-8"));
    }

    private CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext, String utterance, OkHttpClient httpClient) {
        RecognizerResult recognizerResult;
        JsonNode luisResponse = null;
        ObjectMapper mapper = new ObjectMapper();

        if (utterance == null || utterance.isEmpty()) {
            recognizerResult = new RecognizerResult() {{
                setText(utterance);
            }};
        }
        else {
            Request request = buildRequest(BuildRequestBody(utterance));
            OkHttpClient client = new OkHttpClient();

            try {
                Response response = httpClient.newCall(request).execute();
                luisResponse = mapper.readTree(response.body().string());

            } catch (IOException e) {
                e.printStackTrace();
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
            if (includeAPIResults) {
                recognizerResult.getProperties().put("luisResult", luisResponse);
            }

            if (includeInstanceData) {
                if (recognizerResult.getEntities().get(metadataKey) == null) {
                    ((ObjectNode)recognizerResult.getEntities()).putObject(metadataKey);
                }
            }
        }

        sendTraceActivity(recognizerResult, luisResponse, turnContext);

        return CompletableFuture.completedFuture(recognizerResult);
    }

    private Request buildRequest(RequestBody body) {
        StringBuilder path = new StringBuilder(application.endpoint);
        path.append(String.format("/luis/prediction/v3.0/apps/%s", application.applicationId));

        if (version == null) {
            path.append(String.format("/slots/%s/predict", slot));
        }
        else {
            path.append(String.format("/versions/%s/predict", version));
        }

        HttpUrl.Builder httpBuilder = HttpUrl.parse(path.toString()).newBuilder();

        httpBuilder.addQueryParameter("verbose", Boolean.toString(includeInstanceData));
        httpBuilder.addQueryParameter("log", Boolean.toString(log));
        httpBuilder.addQueryParameter("show-all-intents", Boolean.toString(includeAllIntents));

        Request.Builder requestBuilder = new Request.Builder()
            .url(httpBuilder.build())
            .addHeader("Ocp-Apim-Subscription-Key", application.endpointKey).post(body);
        return requestBuilder.build();
    }

    private Map<String, IntentScore> getIntents(JsonNode prediction) {
        Map<String, IntentScore> intents = new LinkedHashMap<>();

        JsonNode intentsObject= prediction.get("intents");
        if (intentsObject == null) {
            return intents;
        }

        for (Iterator<Map.Entry<String, JsonNode>> it = intentsObject.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> intent = it.next();
            double score = intent.getValue().get("score").asDouble();
            String intentName = intent.getKey().replace(".", "_").replace(" ", "_");
            intents.put(intentName, new IntentScore(){{
                setScore(score);
            }});
        }

        return intents;
    }

    private String normalizeEntity(String entity) {
    // Type::Role -> Role
        String[] type = entity.split(":");
        return type[type.length-1].replace(".", "_").replace(" ", "_");
    }

    private JsonNode getEntities(JsonNode prediction) {
        return  MapEntitiesRecursive(prediction.get("entities"), false);
    }

    private JsonNode MapEntitiesRecursive(JsonNode source, boolean inInstance) {
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
                    Set<String> unique = new HashSet<String>();

                    for (JsonNode elt: timexs) {
                        unique.add(elt.get("timex").textValue());
                    }

                    for (String timex : unique) {
                        arr.add(timex);
                    }

                    nobj.set("timex", arr);
                }

                nobj.set("type", type);
            }
            else {
                // Map or remove properties
                Iterator<Map.Entry<String, JsonNode>> nodes = obj.fields();
                while (nodes.hasNext()) {
                    Map.Entry<String, JsonNode> property = (Map.Entry<String, JsonNode>) nodes.next();
                    String name = normalizeEntity(property.getKey());
                    boolean isObj = property.getValue().isObject();
                    boolean isArray = property.getValue().isArray();
                    boolean isString = property.getValue().isTextual();
                    boolean isInt = property.getValue().isInt();
                    JsonNode val = MapEntitiesRecursive(property.getValue(), inInstance || name.equals(metadataKey));

                    if (name.equals("datetime") && isArray) {
                        nobj.set("datetimeV1", val);
                    }
                    else if (name.equals("datetimeV2") && isArray) {
                        nobj.set("datetime", val);
                    }
                    else if (inInstance) {
                        // Correct $instance issues
                        if (name.equals("length") && isInt) {
                            int value = property.getValue().intValue();
                            if (obj.get("startIndex") != null) {
                                value += obj.get("startIndex").intValue();
                            }
                            nobj.put("endIndex", value);
                        }
                        else if (!((isInt && name.equals("modelTypeId")) ||
                            (isString && name.equals("role")))) {
                            nobj.set(name, val);
                        }
                    }
                    else {
                        // Correct non-$instance values
                        if (name.equals("unit") && isString) {
                            nobj.set("units", val);
                        }
                        else {
                            nobj.set(name, val);
                        }
                    }
                }
            }

            result = nobj;
        }
        else if (source.isArray()) {
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

                    if (props.getKey().contains("type") &&
                        geographySubtypes.contains(props.getValue().textValue())) {
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
                }
                else {
                    narr.add(MapEntitiesRecursive(elt, inInstance));
                }
            }
            result = narr;
        }

        return result;
    }

    private void addProperties(JsonNode prediction, RecognizerResult result){
        JsonNode sentiment = prediction.get("sentiment");
        if (sentiment != null) {
            ObjectNode sentimentNode = JsonNodeFactory.instance.objectNode();
            sentimentNode.set("label", sentiment.get("label"));
            sentimentNode.set("score", sentiment.get("score"));
            result.getProperties().put("sentiment", sentimentNode);
        }
    }

    private void sendTraceActivity(RecognizerResult recognizerResult, JsonNode luisResponse, TurnContext turnContext) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ObjectNode traceInfo = JsonNodeFactory.instance.objectNode();
            traceInfo.put("recognizerResult", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(recognizerResult));
            traceInfo.set("luisResult", luisResponse);
            traceInfo.set("luisModel", JsonNodeFactory.instance.objectNode().put("ModelId", this.application.applicationId));

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

            if (dynamicLists!=null) {
                ArrayNode dynamicListNode = JsonNodeFactory.instance.arrayNode();
                for (DynamicList e : dynamicLists) {
                    dynamicListNode.add(mapper.valueToTree(e));
                }
                luisOptions.put("dynamicLists", dynamicListNode);
            }

            traceInfo.set("luisOptions", luisOptions);

            turnContext.sendActivity(Activity.createTraceActivity("LuisRecognizer", luisTraceType, traceInfo, LuisTraceLabel)).thenApply(resourceResponse -> null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
