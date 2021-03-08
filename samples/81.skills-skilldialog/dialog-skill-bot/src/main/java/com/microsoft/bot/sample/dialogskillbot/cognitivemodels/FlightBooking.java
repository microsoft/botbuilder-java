// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.bot.sample.dialogskillbot.cognitivemodels;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.RecognizerResult;

import org.apache.commons.lang3.tuple.Pair;

public class FlightBooking extends RecognizerResult {

    public String Text;
    public String AlteredText;
    public enum Intent {
        BookFlight,
        Cancel,
        GetWeather,
        None
    };

    public Map<FlightBooking.Intent, IntentScore> Intents;

    public class _Entities {

        // Built-in entities
        public DateTimeSpec[] datetime;

        // Lists
        public String[][] Airport;

        // Composites
        public class _InstanceFrom {
            public InstanceData[] Airport;
        }

        public class FromClass {
            public String[][] Airport;

            @JsonProperty(value = "$instance")
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public _InstanceFrom _instance;
        }
        public FromClass[] From;

        public class _InstanceTo {
            public InstanceData[] Airport;
        }

        public class ToClass {
            public String[][] Airport;

            @JsonProperty(value = "$instance")
            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            public _InstanceTo _instance;
        }

        public ToClass[] To;

        // Instance
        public class _Instance {
            public InstanceData[] datetime;
            public InstanceData[] Airport;
            public InstanceData[] From;
            public InstanceData[] To;
        }

        @JsonProperty(value = "$instance")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public _Instance _instance;
    }
    public _Entities Entities;

    private Map<String, Object> properties;

    /**
     * Sets properties that are not otherwise defined by the RecognizerResult type
     * but that might appear in the REST JSON object.
     *
     * <p>
     * With this, properties not represented in the defined type are not dropped
     * when the JSON object is deserialized, but are instead stored in this
     * property. Such properties will be written to a JSON object when the instance
     * is serialized.
     * </p>
     *
     * @param key   The property key.
     * @param value The property value.
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }

    public Pair<Intent, Double> TopIntent() {
        Intent maxIntent = Intent.None;
        Double max = 0.0;
        for(Entry<Intent, IntentScore> entry : Intents.entrySet()) {
            if (entry.getValue().getScore() > max) {
                maxIntent = entry.getKey();
                max = entry.getValue().getScore();
            }
        }
        return Pair.of(maxIntent, max);
    }

    // Extends the partial FlightBooking class with methods and properties that simplify accessing
    // entities in the LUIS results.
    public Pair<String, String> getFromEntities() {
        if (Entities != null && Entities._instance != null && Entities._instance.From != null) {
            String toValue = Entities._instance.From[0].getText();
            String toAirportValue = Entities.From[0].Airport[0][0];
            return Pair.of(toValue, toAirportValue);
        } else {
            return null;
        }

    }

    public Pair<String, String> getToEntities() {
        if (Entities != null && Entities._instance != null && Entities._instance.To != null) {
            String toValue = Entities._instance.To[0].getText();
            String toAirportValue = Entities.To[0].Airport[0][0];
            return Pair.of(toValue, toAirportValue);
        } else {
            return null;
        }
    }


    // This value will be a TIMEX. We are only interested in the Date part, so grab the first
    // result and drop the Time part.TIMEX is a format that represents DateTime expressions that
    // include some ambiguity, such as a missing Year.
    public String getTravelDate() {
        if (Entities != null && Entities.datetime != null && Entities.datetime.length > 0) {
            DateTimeSpec firstDateTimeSpec = Entities.datetime[0];
            if (firstDateTimeSpec.getExpressions() != null) {
                List<String> expressions = firstDateTimeSpec.getExpressions();
                return expressions.get(0).split("T")[0];
            }
        }
        return null;
    }
}
