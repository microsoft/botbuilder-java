/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.restclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.restclient.serializer.JsonFlatten;
import com.microsoft.bot.restclient.util.Foo;
import org.junit.Assert;
import org.junit.Test;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatteningSerializerTests {
    public void assertJsonEqualsNonStrict(String json1, String json2) {
        try {
            JSONAssert.assertEquals(json1, json2, false);
        } catch (JSONException jse) {
            throw new IllegalArgumentException(jse.getMessage());
        }   
    }

    @Test
    public void canFlatten() throws Exception {
        Foo foo = new Foo();
        foo.bar = "hello.world";
        foo.baz = new ArrayList<>();
        foo.baz.add("hello");
        foo.baz.add("hello.world");
        foo.qux = new HashMap<>();
        foo.qux.put("hello", "world");
        foo.qux.put("a.b", "c.d");
        foo.qux.put("bar.a", "ttyy");
        foo.qux.put("bar.b", "uuzz");

        JacksonAdapter adapter = new JacksonAdapter();

        // serialization
        String serialized = adapter.serialize(foo);
        String expected = "{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}}}";
        assertJsonEqualsNonStrict(expected, serialized);

        // deserialization
        Foo deserialized = adapter.deserialize(serialized, Foo.class);
        Assert.assertEquals("hello.world", deserialized.bar);
        Assert.assertArrayEquals(new String[]{"hello", "hello.world"}, deserialized.baz.toArray());
        Assert.assertNotNull(deserialized.qux);
        Assert.assertEquals("world", deserialized.qux.get("hello"));
        Assert.assertEquals("c.d", deserialized.qux.get("a.b"));
        Assert.assertEquals("ttyy", deserialized.qux.get("bar.a"));
        Assert.assertEquals("uuzz", deserialized.qux.get("bar.b"));
    }

    @Test
    public void canSerializeMapKeysWithDotAndSlash() throws Exception {
        String serialized = new JacksonAdapter().serialize(prepareSchoolModel());
        String expected = "{\"teacher\":{\"students\":{\"af.B/D\":{},\"af.B/C\":{}}},\"tags\":{\"foo.aa\":\"bar\",\"x.y\":\"zz\"},\"properties\":{\"name\":\"school1\"}}";
        assertJsonEqualsNonStrict(expected, serialized);
    }

    /**
     * Validates decoding and encoding of a type with type id containing dot and no additional properties
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDotAndNoProperties() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();

        String rabbitSerialized = "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}";
        String shelterSerialized = "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}},{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}";

        AnimalWithTypeIdContainingDot rabbitDeserialized = adapter.deserialize(rabbitSerialized, AnimalWithTypeIdContainingDot.class);
        Assert.assertTrue(rabbitDeserialized instanceof RabbitWithTypeIdContainingDot);
        Assert.assertNotNull(rabbitDeserialized);

        AnimalShelter shelterDeserialized = adapter.deserialize(shelterSerialized, AnimalShelter.class);
        Assert.assertTrue(shelterDeserialized instanceof AnimalShelter);
        Assert.assertEquals(2, shelterDeserialized.animalsInfo().size());
        for (FlattenableAnimalInfo animalInfo: shelterDeserialized.animalsInfo()) {
            Assert.assertTrue(animalInfo.animal() instanceof RabbitWithTypeIdContainingDot);
            Assert.assertNotNull(animalInfo.animal());
        }
    }

    /**
     * Validates that decoding and encoding of a type with type id containing dot and can be done.
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDot0() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = adapter.serialize(animalToSerialize);
        //
        String[] results = {
                "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
                "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
         Assert.assertTrue(found);
        // De-Serialize
        //
        AnimalWithTypeIdContainingDot animalDeserialized = adapter.deserialize(serialized, AnimalWithTypeIdContainingDot.class);
        Assert.assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbit = (RabbitWithTypeIdContainingDot) animalDeserialized;
        Assert.assertNotNull(rabbit.meals());
        Assert.assertEquals(rabbit.meals().size(), 2);
    }

    /**
     * Validates that decoding and encoding of a type with type id containing dot and can be done.
     * For decoding and encoding concrete type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithTypeIdContainingDot1() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        String serialized = adapter.serialize(rabbitToSerialize);
        //
        String[] results = {
                "{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}",
                "{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}"
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // De-Serialize
        //
        RabbitWithTypeIdContainingDot rabbitDeserialized = adapter.deserialize(serialized, RabbitWithTypeIdContainingDot.class);
        Assert.assertTrue(rabbitDeserialized instanceof RabbitWithTypeIdContainingDot);
        Assert.assertNotNull(rabbitDeserialized.meals());
        Assert.assertEquals(rabbitDeserialized.meals().size(), 2);
    }


    /**
     * Validates that decoding and encoding of a type with flattenable property and type id containing dot and can be done.
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot0() throws IOException {
        AnimalWithTypeIdContainingDot animalToSerialize = new DogWithTypeIdContainingDot().withBreed("AKITA").withCuteLevel(10);
        JacksonAdapter adapter = new JacksonAdapter();
        // serialization
        String serialized = adapter.serialize(animalToSerialize);
        String[] results = {
                "{\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10}}",
                "{\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
                "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
                "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\"}",
                "{\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\"}",
                "{\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // de-serialization
        AnimalWithTypeIdContainingDot animalDeserialized = adapter.deserialize(serialized, AnimalWithTypeIdContainingDot.class);
        Assert.assertTrue(animalDeserialized instanceof DogWithTypeIdContainingDot);
        DogWithTypeIdContainingDot dogDeserialized = (DogWithTypeIdContainingDot) animalDeserialized;
        Assert.assertNotNull(dogDeserialized);
        Assert.assertEquals(dogDeserialized.breed(), "AKITA");
        Assert.assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of a type with flattenable property and type id containing dot and can be done.
     * For decoding and encoding concrete type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleTypeWithFlattenablePropertyAndTypeIdContainingDot1() throws IOException {
        DogWithTypeIdContainingDot dogToSerialize = new DogWithTypeIdContainingDot().withBreed("AKITA").withCuteLevel(10);
        JacksonAdapter adapter = new JacksonAdapter();
        // serialization
        String serialized = adapter.serialize(dogToSerialize);
        String[] results = {
                "{\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10}}",
                "{\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
                "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\",\"properties\":{\"cuteLevel\":10}}",
                "{\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\"}",
                "{\"properties\":{\"cuteLevel\":10},\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\",\"breed\":\"AKITA\"}",
                "{\"properties\":{\"cuteLevel\":10},\"breed\":\"AKITA\",\"@odata.type\":\"#Favourite.Pet.DogWithTypeIdContainingDot\"}",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // de-serialization
        DogWithTypeIdContainingDot dogDeserialized = adapter.deserialize(serialized, DogWithTypeIdContainingDot.class);
        Assert.assertNotNull(dogDeserialized);
        Assert.assertEquals(dogDeserialized.breed(), "AKITA");
        Assert.assertEquals(dogDeserialized.cuteLevel(), (Integer) 10);
    }

    /**
     * Validates that decoding and encoding of a array of type with type id containing dot and can be done.
     * For decoding and encoding base type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot0() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<AnimalWithTypeIdContainingDot> animalsToSerialize = new ArrayList<>();
        animalsToSerialize.add(animalToSerialize);
        String serialized = adapter.serialize(animalsToSerialize);
        String[] results = {
                "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
                "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // De-serialize
        //
        List<AnimalWithTypeIdContainingDot> animalsDeserialized = adapter.deserialize(serialized, new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { AnimalWithTypeIdContainingDot.class };
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        });
        Assert.assertNotNull(animalsDeserialized);
        Assert.assertEquals(1, animalsDeserialized.size());
        AnimalWithTypeIdContainingDot animalDeserialized = animalsDeserialized.get(0);
        Assert.assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbitDeserialized = (RabbitWithTypeIdContainingDot) animalDeserialized;
        Assert.assertNotNull(rabbitDeserialized.meals());
        Assert.assertEquals(rabbitDeserialized.meals().size(), 2);
    }

    /**
     * Validates that decoding and encoding of a array of type with type id containing dot and can be done.
     * For decoding and encoding concrete type will be used.
     *
     * @throws IOException
     */
    @Test
    public void canHandleArrayOfTypeWithTypeIdContainingDot1() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        // Serialize
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        //
        RabbitWithTypeIdContainingDot rabbitToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        List<RabbitWithTypeIdContainingDot> rabbitsToSerialize = new ArrayList<>();
        rabbitsToSerialize.add(rabbitToSerialize);
        String serialized = adapter.serialize(rabbitsToSerialize);
        String[] results = {
                "[{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}]",
                "[{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}]",
        };
        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // De-serialize
        //
        List<RabbitWithTypeIdContainingDot> rabbitsDeserialized = adapter.deserialize(serialized, new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { RabbitWithTypeIdContainingDot.class };
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        });
        Assert.assertNotNull(rabbitsDeserialized);
        Assert.assertEquals(1, rabbitsDeserialized.size());
        RabbitWithTypeIdContainingDot rabbitDeserialized = rabbitsDeserialized.get(0);
        Assert.assertNotNull(rabbitDeserialized.meals());
        Assert.assertEquals(rabbitDeserialized.meals().size(), 2);
    }


    /**
     * Validates that decoding and encoding of a composed type with type id containing dot and can be done.
     *
     * @throws IOException
     */
    @Test
    public void canHandleComposedTypeWithTypeIdContainingDot0() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        // serialization
        //
        List<String> meals = new ArrayList<>();
        meals.add("carrot");
        meals.add("apple");
        AnimalWithTypeIdContainingDot animalToSerialize = new RabbitWithTypeIdContainingDot().withMeals(meals);
        FlattenableAnimalInfo animalInfoToSerialize = new FlattenableAnimalInfo().withAnimal(animalToSerialize);
        List<FlattenableAnimalInfo> animalsInfoSerialized = ImmutableList.of(animalInfoToSerialize);
        AnimalShelter animalShelterToSerialize = new AnimalShelter().withAnimalsInfo(animalsInfoSerialized);
        String serialized = adapter.serialize(animalShelterToSerialize);
        String[] results = {
                "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"meals\":[\"carrot\",\"apple\"],\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\"}}]}}",
                "{\"properties\":{\"animalsInfo\":[{\"animal\":{\"@odata.type\":\"#Favourite.Pet.RabbitWithTypeIdContainingDot\",\"meals\":[\"carrot\",\"apple\"]}}]}}",
        };

        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // de-serialization
        //
        AnimalShelter shelterDeserialized = adapter.deserialize(serialized, AnimalShelter.class);
        Assert.assertNotNull(shelterDeserialized.animalsInfo());
        Assert.assertEquals(shelterDeserialized.animalsInfo().size(), 1);
        FlattenableAnimalInfo animalsInfoDeserialized = shelterDeserialized.animalsInfo().get(0);
        Assert.assertTrue(animalsInfoDeserialized.animal() instanceof RabbitWithTypeIdContainingDot);
        AnimalWithTypeIdContainingDot animalDeserialized = animalsInfoDeserialized.animal();
        Assert.assertTrue(animalDeserialized instanceof RabbitWithTypeIdContainingDot);
        RabbitWithTypeIdContainingDot rabbitDeserialized = (RabbitWithTypeIdContainingDot) animalDeserialized;
        Assert.assertNotNull(rabbitDeserialized);
        Assert.assertNotNull(rabbitDeserialized.meals());
        Assert.assertEquals(rabbitDeserialized.meals().size(), 2);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithTypeId() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet1());
        Assert.assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        adapter.serialize(composedTurtleDeserialized);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        Assert.assertEquals(10 , (long) composedTurtleDeserialized.turtlesSet1Lead().size());
        Assert.assertEquals(100 , (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        adapter.serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithoutTypeId() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet1());
        Assert.assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        adapter.serialize(composedTurtleDeserialized);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet1Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet1Lead());
        Assert.assertEquals(100 , (long) composedTurtleDeserialized.turtlesSet1Lead().age());
        //
        adapter.serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedSpecificPolymorphicTypeWithAndWithoutTypeId() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet1\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet1());
        Assert.assertEquals(2, composedTurtleDeserialized.turtlesSet1().size());
        //
        adapter.serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithTypeId() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet2());
        Assert.assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assert.assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assert.assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        //
        adapter.serialize(composedTurtleDeserialized);
        //
        // -- Validate scalar property
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"}}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead() instanceof TurtleWithTypeIdContainingDot);
        Assert.assertEquals(10 , (long) ((TurtleWithTypeIdContainingDot) composedTurtleDeserialized.turtlesSet2Lead()).size());
        Assert.assertEquals(100 , (long) composedTurtleDeserialized.turtlesSet2Lead().age());
        //
        adapter.serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithoutTypeId() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10 },{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet2());
        Assert.assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assert.assertFalse(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assert.assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof NonEmptyAnimalWithTypeIdContainingDot);
        Assert.assertFalse(composedTurtleDeserialized.turtlesSet2().get(1) instanceof TurtleWithTypeIdContainingDot);
        Assert.assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof NonEmptyAnimalWithTypeIdContainingDot);
        //
        // -- Validate scalar property
        //
        adapter.serialize(composedTurtleDeserialized);
        //
        String serializedScalarWithTypeId = "{\"turtlesSet2Lead\":{\"age\":100,\"size\":10 }}";
        // de-serialization
        //
        composedTurtleDeserialized = adapter.deserialize(serializedScalarWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead());
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet2Lead() instanceof NonEmptyAnimalWithTypeIdContainingDot);
        //
        adapter.serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleComposedGenericPolymorphicTypeWithAndWithoutTypeId() throws IOException {
        JacksonAdapter adapter = new JacksonAdapter();
        //
        // -- Validate vector property
        //
        String serializedCollectionWithTypeId = "{\"turtlesSet2\":[{\"age\":100,\"size\":10,\"@odata.type\":\"#Favourite.Pet.TurtleWithTypeIdContainingDot\"},{\"age\":200,\"size\":20 }]}";
        // de-serialization
        //
        ComposeTurtles composedTurtleDeserialized = adapter.deserialize(serializedCollectionWithTypeId, ComposeTurtles.class);
        Assert.assertNotNull(composedTurtleDeserialized);
        Assert.assertNotNull(composedTurtleDeserialized.turtlesSet2());
        Assert.assertEquals(2, composedTurtleDeserialized.turtlesSet2().size());
        //
        Assert.assertTrue(composedTurtleDeserialized.turtlesSet2().get(0) instanceof TurtleWithTypeIdContainingDot);
        Assert.assertTrue(composedTurtleDeserialized.turtlesSet2().get(1) instanceof NonEmptyAnimalWithTypeIdContainingDot);
        //
        adapter.serialize(composedTurtleDeserialized);
    }

    @Test
    public void canHandleEscapedProperties() throws IOException {
        FlattenedProduct productToSerialize = new FlattenedProduct();
        productToSerialize.withProductName("drink");
        productToSerialize.withPType("chai");
        JacksonAdapter adapter = new JacksonAdapter();
        // serialization
        //
        String serialized = adapter.serialize(productToSerialize);
        String[] results = {
                "{\"properties\":{\"p.name\":\"drink\",\"type\":\"chai\"}}",
                "{\"properties\":{\"type\":\"chai\",\"p.name\":\"drink\"}}",
        };

        boolean found = false;
        for (String result : results) {
            if (result.equals(serialized)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // de-serialization
        //
        FlattenedProduct productDeserialized = adapter.deserialize(serialized, FlattenedProduct.class);
        Assert.assertNotNull(productDeserialized);
        Assert.assertEquals(productDeserialized.productName(), "drink");
        Assert.assertEquals(productDeserialized.productType, "chai");
    }

    @JsonFlatten
    private class School {
        @JsonProperty(value = "teacher")
        private Teacher teacher;

        @JsonProperty(value = "properties.name")
        private String name;

        @JsonProperty(value = "tags")
        private Map<String, String> tags;

        public School withTeacher(Teacher teacher) {
            this.teacher = teacher;
            return this;
        }

        public School withName(String name) {
            this.name = name;
            return this;
        }

        public School withTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }
    }

    private class Student {
    }

    private class Teacher {
        @JsonProperty(value = "students")
        private Map<String, Student> students;

        public Teacher withStudents(Map<String, Student> students) {
            this.students = students;
            return this;
        }
    }

    private School prepareSchoolModel() {
        Teacher teacher = new Teacher();

        Map<String, Student> students = new HashMap<String, Student>();
        students.put("af.B/C", new Student());
        students.put("af.B/D", new Student());

        teacher.withStudents(students);

        School school = new School().withName("school1");
        school.withTeacher(teacher);

        Map<String, String> schoolTags = new HashMap<String, String>();
        schoolTags.put("foo.aa", "bar");
        schoolTags.put("x.y", "zz");

        school.withTags(schoolTags);

        return school;
    }

    @JsonFlatten
    public static class FlattenedProduct {
        // Flattened and escaped property
        @JsonProperty(value = "properties.p\\.name")
        private String productName;

        @JsonProperty(value = "properties.type")
        private String productType;

        public String productName() {
            return this.productName;
        }

        public FlattenedProduct withProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public String productType() {
            return this.productType;
        }

        public FlattenedProduct withPType(String productType) {
            this.productType = productType;
            return this;
        }
    }
}
