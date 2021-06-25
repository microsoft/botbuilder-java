// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.restclient.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.microsoft.bot.restclient.CollectionFormat;
import com.microsoft.bot.restclient.protocol.SerializerAdapter;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A serialization helper class wrapped around {@link JacksonConverterFactory} and {@link ObjectMapper}.
 */
public class JacksonAdapter implements SerializerAdapter<ObjectMapper> {
    /**
     * An instance of {@link ObjectMapper} to serialize/deserialize objects.
     */
    private final ObjectMapper mapper;

    /**
     * An instance of {@link ObjectMapper} that does not do flattening.
     */
    private final ObjectMapper simpleMapper;

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonAdapter() {
        simpleMapper = initializeObjectMapper(new ObjectMapper());
        ObjectMapper flatteningMapper = initializeObjectMapper(new ObjectMapper())
                .registerModule(FlatteningSerializer.getModule(simpleMapper()))
                .registerModule(FlatteningDeserializer.getModule(simpleMapper()));
        mapper = initializeObjectMapper(new ObjectMapper())
                // Order matters: must register in reverse order of hierarchy
                .registerModule(AdditionalPropertiesSerializer.getModule(flatteningMapper))
                .registerModule(AdditionalPropertiesDeserializer.getModule(flatteningMapper))
                .registerModule(FlatteningSerializer.getModule(simpleMapper()))
                .registerModule(FlatteningDeserializer.getModule(simpleMapper()));
    }

    /**
     * Gets a static instance of {@link ObjectMapper} that doesn't handle flattening.
     *
     * @return an instance of {@link ObjectMapper}.
     */
    protected ObjectMapper simpleMapper() {
        return simpleMapper;
    }

    @Override
    public ObjectMapper serializer() {
        return mapper;
    }

    @Override
    public JacksonConverterFactory converterFactory() {
        return JacksonConverterFactory.create(serializer());
    }

    @Override
    public String serialize(Object object) throws IOException {
        if (object == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        serializer().writeValue(writer, object);
        return writer.toString();
    }

    @Override
    public String serializeRaw(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return CharMatcher.is('"').trimFrom(serialize(object));
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        if (list == null) {
            return null;
        }
        List<String> serialized = new ArrayList<>();
        for (Object element : list) {
            String raw = serializeRaw(element);
            serialized.add(raw != null ? raw : "");
        }
        return Joiner.on(format.getDelimiter()).join(serialized);
    }

    private JavaType constructJavaType(final Type type) {
        if (type instanceof ParameterizedType) {
            JavaType[] javaTypeArgs = new JavaType[((ParameterizedType) type).getActualTypeArguments().length];
            for (int i = 0; i != ((ParameterizedType) type).getActualTypeArguments().length; ++i) {
                javaTypeArgs[i] = constructJavaType(((ParameterizedType) type).getActualTypeArguments()[i]);
            }
            return mapper.getTypeFactory().constructType(type,
                TypeBindings.create((Class<?>) ((ParameterizedType) type).getRawType(), javaTypeArgs));
        } else {
            return mapper.getTypeFactory().constructType(type);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, final Type type) throws IOException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return serializer().readValue(value, constructJavaType(type));
    }

    /**
     * Initializes an instance of JacksonMapperAdapter with default configurations
     * applied to the object mapper.
     *
     * @param mapper the object mapper to use.
     */
    private static ObjectMapper initializeObjectMapper(ObjectMapper mapper) {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new ParameterNamesModule())
                .registerModule(ByteArraySerializer.getModule())
                .registerModule(Base64UrlSerializer.getModule())
                .registerModule(HeadersSerializer.getModule());
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper;
    }
}
