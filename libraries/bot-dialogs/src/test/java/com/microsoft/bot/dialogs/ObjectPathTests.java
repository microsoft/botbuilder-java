// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.schema.Serialization;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class ObjectPathTests {
    @Test
    public void typed_OnlyDefaultTest() {
        Options defaultOptions = new Options();
        defaultOptions.lastName = "Smith";
        defaultOptions.firstName = "Fred";
        defaultOptions.age = 22;
        defaultOptions.bool = true;
        defaultOptions.location = new Location();
        defaultOptions.location.latitude = 1.2312312F;
        defaultOptions.location.longitude = 3.234234F;

        Options overlay = new Options() { };

        Options result = ObjectPath.merge(defaultOptions, overlay);

        Assert.assertEquals(result.lastName, defaultOptions.lastName);
        Assert.assertEquals(result.firstName, defaultOptions.firstName);
        Assert.assertEquals(result.age, defaultOptions.age);
        Assert.assertEquals(result.bool, defaultOptions.bool);
        Assert.assertEquals(result.location.latitude, defaultOptions.location.latitude, .01);
        Assert.assertEquals(result.location.longitude, defaultOptions.location.longitude, .01);
    }

    @Test
    public void typed_OnlyOverlay() {
        Options defaultOptions = new Options();

        Options overlay = new Options();
        overlay.lastName = "Smith";
        overlay.firstName = "Fred";
        overlay.age = 22;
        overlay.bool = true;
        overlay.location = new Location();
        overlay.location.latitude = 1.2312312F;
        overlay.location.longitude = 3.234234F;

        Options result = ObjectPath.merge(defaultOptions, overlay);

        Assert.assertEquals(result.lastName, overlay.lastName);
        Assert.assertEquals(result.firstName, overlay.firstName);
        Assert.assertEquals(result.age, overlay.age);
        Assert.assertEquals(result.bool, overlay.bool);
        Assert.assertEquals(result.location.latitude, overlay.location.latitude, .01);
        Assert.assertEquals(result.location.longitude, overlay.location.longitude, .01);
    }

    @Test
    public void typed_FullOverlay() {
        Options defaultOptions = new Options();
        defaultOptions.lastName = "Smith";
        defaultOptions.firstName = "Fred";
        defaultOptions.age = 22;
        defaultOptions.location = new Location();
        defaultOptions.location.latitude = 1.2312312F;
        defaultOptions.location.longitude = 3.234234F;

        Options overlay = new Options();
        overlay.lastName = "Grant";
        overlay.firstName = "Eddit";
        overlay.age = 32;
        overlay.bool = true;
        overlay.location = new Location();
        overlay.location.latitude = 2.2312312F;
        overlay.location.longitude = 2.234234F;

        Options result = ObjectPath.merge(defaultOptions, overlay);

        Assert.assertEquals(result.lastName, overlay.lastName);
        Assert.assertEquals(result.firstName, overlay.firstName);
        Assert.assertEquals(result.age, overlay.age);
        Assert.assertEquals(result.bool, overlay.bool);
        Assert.assertEquals(result.location.latitude, overlay.location.latitude, .01);
        Assert.assertEquals(result.location.longitude, overlay.location.longitude, .01);
    }

    @Test
    public void typed_PartialOverlay() {
        Options defaultOptions = new Options();
        defaultOptions.lastName = "Smith";
        defaultOptions.firstName = "Fred";
        defaultOptions.age = 22;
        defaultOptions.location = new Location();
        defaultOptions.location.latitude = 1.2312312F;
        defaultOptions.location.longitude = 3.234234F;

        Options overlay = new Options();
        overlay.lastName = "Grant";

        Options result = ObjectPath.merge(defaultOptions, overlay);

        Assert.assertEquals(result.lastName, overlay.lastName);
        Assert.assertEquals(result.firstName, defaultOptions.firstName);
        Assert.assertEquals(result.age, defaultOptions.age);
        Assert.assertEquals(result.bool, defaultOptions.bool);
        Assert.assertEquals(result.location.latitude, defaultOptions.location.latitude, .01);
        Assert.assertEquals(result.location.longitude, defaultOptions.location.longitude, .01);
    }

    @Test
    public void anonymous_OnlyDefaultTest() throws NoSuchFieldException, IllegalAccessException {
        Object defaultOptions = new Object() {
            public String lastName = "Smith";
            public String firstName = "Fred";
            public Integer age = 22;
            public Boolean bool = true;
            public Object location = new Object() {
                public Float latitude = 1.2312312F;
                public Float longitude = 3.234234F;
            };
        };

        Options overlay = new Options() { };

        Options result = ObjectPath.merge(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, defaultOptions.getClass().getDeclaredField("lastName").get(defaultOptions));
        Assert.assertEquals(result.firstName, defaultOptions.getClass().getDeclaredField("firstName").get(defaultOptions));
        Assert.assertEquals(result.age, defaultOptions.getClass().getDeclaredField("age").get(defaultOptions));
        Assert.assertEquals(result.bool, defaultOptions.getClass().getDeclaredField("bool").get(defaultOptions));

        Object loc = defaultOptions.getClass().getDeclaredField("location").get(defaultOptions);
        Assert.assertEquals(result.location.latitude.floatValue(),
            ((Float) loc.getClass().getDeclaredField("latitude").get(loc)).floatValue(), .01);
        Assert.assertEquals(result.location.longitude.floatValue(), ((Float) loc.getClass().getDeclaredField("longitude").get(loc)).floatValue(), .01);
    }

    @Test
    public void anonymous_OnlyOverlay() throws NoSuchFieldException, IllegalAccessException {
        Options defaultOptions = new Options() { };

        Object overlay = new Object() {
            public String lastName = "Smith";
            public String firstName = "Fred";
            public Integer age = 22;
            public Boolean bool = true;
            public Object location = new Object() {
                public Float latitude = 1.2312312F;
                public Float longitude = 3.234234F;
            };
        };

        Options result = ObjectPath.merge(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, overlay.getClass().getDeclaredField("lastName").get(overlay));
        Assert.assertEquals(result.firstName, overlay.getClass().getDeclaredField("firstName").get(overlay));
        Assert.assertEquals(result.age, overlay.getClass().getDeclaredField("age").get(overlay));
        Assert.assertEquals(result.bool, overlay.getClass().getDeclaredField("bool").get(overlay));

        Object loc = overlay.getClass().getDeclaredField("location").get(overlay);
        Assert.assertEquals(result.location.latitude.floatValue(),
            ((Float) loc.getClass().getDeclaredField("latitude").get(loc)).floatValue(), .01);
        Assert.assertEquals(result.location.longitude.floatValue(), ((Float) loc.getClass().getDeclaredField("longitude").get(loc)).floatValue(), .01);
    }

    @Test
    public void anonymous_FullOverlay() throws NoSuchFieldException, IllegalAccessException {
        Object defaultOptions = new Object() {
            public String lastName = "Smith";
            public String firstName = "Fred";
            public Integer age = 22;
            public Boolean bool = true;
            public Object location = new Object() {
                public Float latitude = 1.2312312F;
                public Float longitude = 3.234234F;
            };
        };

        Object overlay = new Object() {
            public String lastName = "Grant";
            public String firstName = "Eddit";
            public Integer age = 32;
            public Boolean bool = false;
            public Object location = new Object() {
                public Float latitude = 2.2312312F;
                public Float longitude = 2.234234F;
            };
        };

        Options result = ObjectPath.merge(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, overlay.getClass().getDeclaredField("lastName").get(overlay));
        Assert.assertEquals(result.firstName, overlay.getClass().getDeclaredField("firstName").get(overlay));
        Assert.assertEquals(result.age, overlay.getClass().getDeclaredField("age").get(overlay));
        Assert.assertEquals(result.bool, overlay.getClass().getDeclaredField("bool").get(overlay));

        Object loc = overlay.getClass().getDeclaredField("location").get(overlay);
        Assert.assertEquals(result.location.latitude.floatValue(),
            ((Float) loc.getClass().getDeclaredField("latitude").get(loc)).floatValue(), .01);
        Assert.assertEquals(result.location.longitude.floatValue(), ((Float) loc.getClass().getDeclaredField("longitude").get(loc)).floatValue(), .01);
    }

    @Test
    public void anonymous_PartialOverlay() throws NoSuchFieldException, IllegalAccessException {
        Object defaultOptions = new Object() {
            public String lastName = "Smith";
            public String firstName = "Fred";
            public Integer age = 22;
            public Boolean bool = true;
            public Object location = new Object() {
                public Float latitude = 1.2312312F;
                public Float longitude = 3.234234F;
            };
        };

        Object overlay = new Object() {
            public String lastName = "Grant";
        };

        Options result = ObjectPath.merge(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, overlay.getClass().getDeclaredField("lastName").get(overlay));
        Assert.assertEquals(result.firstName, defaultOptions.getClass().getDeclaredField("firstName").get(defaultOptions));
        Assert.assertEquals(result.age, defaultOptions.getClass().getDeclaredField("age").get(defaultOptions));
        Assert.assertEquals(result.bool, defaultOptions.getClass().getDeclaredField("bool").get(defaultOptions));

        Object loc = defaultOptions.getClass().getDeclaredField("location").get(defaultOptions);
        Assert.assertEquals(result.location.latitude.floatValue(),
            ((Float) loc.getClass().getDeclaredField("latitude").get(loc)).floatValue(), .01);
        Assert.assertEquals(result.location.longitude.floatValue(), ((Float) loc.getClass().getDeclaredField("longitude").get(loc)).floatValue(), .01);
    }

    @Test
    public void jsonNode_OnlyDefaultTest() {
        Options options = new Options();
        options.lastName = "Smith";
        options.firstName = "Fred";
        options.age = 22;
        options.bool = true;
        Location location = new Location();
        location.latitude = 1.2312312F;
        location.longitude = 3.234234F;
        options.location = location;
        JsonNode defaultOptions = Serialization.objectToTree(options);

        JsonNode overlay = Serialization.objectToTree(new Options());

        Options result = ObjectPath.assign(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, defaultOptions.get("lastName").asText());
        Assert.assertEquals(result.firstName, defaultOptions.get("firstName").asText());
        Assert.assertEquals(result.age.intValue(), defaultOptions.get("age").asInt());
        Assert.assertEquals(result.bool, defaultOptions.get("bool").asBoolean());
        Assert.assertEquals(
            result.location.latitude, defaultOptions.get("location").findValue("latitude").asDouble(), .01);
        Assert.assertEquals(
            result.location.longitude, defaultOptions.get("location").findValue("longitude").asDouble(), .01);
    }

    @Test
    public void jsonNode_OnlyOverlay() {
        JsonNode defaultOptions = Serialization.objectToTree(new Options());

        Options options = new Options();
        options.lastName = "Smith";
        options.firstName = "Fred";
        options.age = 22;
        options.bool = true;
        Location location = new Location();
        location.latitude = 1.2312312F;
        location.longitude = 3.234234F;
        options.location = location;
        JsonNode overlay = Serialization.objectToTree(options);


        Options result = ObjectPath.assign(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, overlay.get("lastName").asText());
        Assert.assertEquals(result.firstName, overlay.get("firstName").asText());
        Assert.assertEquals(result.age.intValue(), overlay.get("age").asInt());
        Assert.assertEquals(result.bool, overlay.get("bool").asBoolean());
        Assert.assertEquals(
            result.location.latitude, overlay.get("location").findValue("latitude").asDouble(), .01);
        Assert.assertEquals(
            result.location.longitude, overlay.get("location").findValue("longitude").asDouble(), .01);
    }

    @Test
    public void jsonNode_FullOverlay() {
        Options defaultOpts = new Options();
        defaultOpts.lastName = "Smith";
        defaultOpts.firstName = "Fred";
        defaultOpts.age = 22;
        defaultOpts.bool = true;
        Location defaultLocation = new Location();
        defaultLocation.latitude = 1.2312312F;
        defaultLocation.longitude = 3.234234F;
        defaultOpts.location = defaultLocation;
        JsonNode defaultOptions = Serialization.objectToTree(defaultOpts);

        Options overlayOpts = new Options();
        overlayOpts.lastName = "Grant";
        overlayOpts.firstName = "Eddit";
        overlayOpts.age = 32;
        overlayOpts.bool = false;
        Location overlayLocation = new Location();
        overlayLocation.latitude = 2.2312312F;
        overlayLocation.longitude = 2.234234F;
        overlayOpts.location = overlayLocation;
        JsonNode overlay = Serialization.objectToTree(overlayOpts);


        Options result = ObjectPath.assign(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, overlay.get("lastName").asText());
        Assert.assertEquals(result.firstName, overlay.get("firstName").asText());
        Assert.assertEquals(result.age.intValue(), overlay.get("age").asInt());
        Assert.assertEquals(result.bool, overlay.get("bool").asBoolean());
        Assert.assertEquals(
            result.location.latitude, overlay.get("location").findValue("latitude").asDouble(), .01);
        Assert.assertEquals(
            result.location.longitude, overlay.get("location").findValue("longitude").asDouble(), .01);
    }

    @Test
    public void jsonNode_PartialOverlay() {
        Options defaultOpts = new Options();
        defaultOpts.lastName = "Smith";
        defaultOpts.firstName = "Fred";
        defaultOpts.age = 22;
        defaultOpts.bool = true;
        Location defaultLocation = new Location();
        defaultLocation.latitude = 1.2312312F;
        defaultLocation.longitude = 3.234234F;
        defaultOpts.location = defaultLocation;
        JsonNode defaultOptions = Serialization.objectToTree(defaultOpts);

        Options overlayOpts = new Options();
        overlayOpts.lastName = "Grant";
        JsonNode overlay = Serialization.objectToTree(overlayOpts);


        Options result = ObjectPath.assign(defaultOptions, overlay, Options.class);

        Assert.assertEquals(result.lastName, overlay.get("lastName").asText());
        Assert.assertEquals(result.firstName, defaultOptions.get("firstName").asText());
        Assert.assertEquals(result.age.intValue(), defaultOptions.get("age").asInt());
        Assert.assertEquals(result.bool, defaultOptions.get("bool").asBoolean());
        Assert.assertEquals(
            result.location.latitude, defaultOptions.get("location").findValue("latitude").asDouble(), .01);
        Assert.assertEquals(
            result.location.longitude, defaultOptions.get("location").findValue("longitude").asDouble(), .01);
    }

    @Test
    public void nullStartObject() {
        Options defaultOptions = new Options();
        defaultOptions.lastName = "Smith";
        defaultOptions.firstName = "Fred";
        defaultOptions.age = 22;
        defaultOptions.location = new Location();
        defaultOptions.location.latitude = 1.2312312F;
        defaultOptions.location.longitude = 3.234234F;

        Options result = ObjectPath.merge(null, defaultOptions, Options.class);

        Assert.assertEquals(result.lastName, defaultOptions.lastName);
        Assert.assertEquals(result.firstName, defaultOptions.firstName);
        Assert.assertEquals(result.age, defaultOptions.age);
        Assert.assertEquals(result.bool, defaultOptions.bool);
        Assert.assertEquals(result.location.latitude, defaultOptions.location.latitude, .01);
        Assert.assertEquals(result.location.longitude, defaultOptions.location.longitude, .01);
    }

    @Test
    public void nullOverlay() {
        Options defaultOptions = new Options();
        defaultOptions.lastName = "Smith";
        defaultOptions.firstName = "Fred";
        defaultOptions.age = 22;
        defaultOptions.location = new Location();
        defaultOptions.location.latitude = 1.2312312F;
        defaultOptions.location.longitude = 3.234234F;

        Options result = ObjectPath.merge(defaultOptions, null, Options.class);

        Assert.assertEquals(result.lastName, defaultOptions.lastName);
        Assert.assertEquals(result.firstName, defaultOptions.firstName);
        Assert.assertEquals(result.age, defaultOptions.age);
        Assert.assertEquals(result.bool, defaultOptions.bool);
        Assert.assertEquals(result.location.latitude, defaultOptions.location.latitude, .01);
        Assert.assertEquals(result.location.longitude, defaultOptions.location.longitude, .01);
    }

    @Test
    public void tryGetPathValue() throws IOException {
        PathTest test = new PathTest();
        test.test = "test";

        test.options = new Options();
        test.options.age = 22;
        test.options.firstName = "joe";
        test.options.lastName = "blow";
        test.options.bool = true;

        test.bar = new Bar();
        test.bar.numIndex = 2;
        test.bar.strIndex = "FirstName";
        test.bar.objIndex = "options";
        test.bar.options = new Options();
        test.bar.options.age = 1;
        test.bar.options.firstName = "joe";
        test.bar.options.lastName = "blow";
        test.bar.options.bool = false;
        test.bar.numbers = new int[] { 1, 2, 3, 4, 5 };

        // test with pojo
        {
            Assert.assertEquals(test, ObjectPath.getPathValue(test, "", PathTest.class));
            Assert.assertEquals(test.test, ObjectPath.getPathValue(test, "test", String.class));
            Assert.assertEquals(
                test.bar.options.age,
                ObjectPath.getPathValue(test, "bar.options.age", Integer.class)
            );

            Options options = ObjectPath.tryGetPathValue(test, "options", Options.class);
            Assert.assertNotNull(options);
            Assert.assertEquals(test.options.age, options.age);
            Assert.assertEquals(test.options.firstName, options.firstName);

            Options barOptions = ObjectPath.tryGetPathValue(test, "bar.options", Options.class);
            Assert.assertNotNull(barOptions);
            Assert.assertEquals(test.bar.options.age, barOptions.age);
            Assert.assertEquals(test.bar.options.firstName, barOptions.firstName);

            int[] numbers = ObjectPath.tryGetPathValue(test, "bar.numbers", int[].class);
            Assert.assertEquals(5, numbers.length);

            int number = ObjectPath.tryGetPathValue(test, "bar.numbers[1]", Integer.class);
            Assert.assertEquals(2, number);

            number = ObjectPath.tryGetPathValue(test, "bar['options'].Age", Integer.class);
            Assert.assertEquals(1, number);

            number = ObjectPath.tryGetPathValue(test, "bar[\"options\"].Age", Integer.class);
            Assert.assertEquals(1, number);

            number = ObjectPath.tryGetPathValue(test, "bar.numbers[bar.numIndex]", Integer.class);
            Assert.assertEquals(3, number);

            number = ObjectPath
                .tryGetPathValue(test, "bar.numbers[bar[bar.objIndex].Age]", Integer.class);
            Assert.assertEquals(2, number);

            String name = ObjectPath
                .tryGetPathValue(test, "bar.options[bar.strIndex]", String.class);
            Assert.assertEquals("joe", name);

            int age = ObjectPath.tryGetPathValue(test, "bar[bar.objIndex].Age", Integer.class);
            Assert.assertEquals(1, age);
        }

        // test with json
        {
            String json = Serialization.toString(test);
            JsonNode jtest = Serialization.jsonToTree(json);

            Assert.assertEquals(jtest, ObjectPath.getPathValue(test, "", JsonNode.class));
        }
    }

    // Test classes
    //
    // Note: This is different from the support dotnet provides due to Java
    // not having the same notion of dynamic and anonymous types.  Jackson
    // itself does not support anonymous inner class deserialization.  So our
    // support only extends to POJO's which conform to a Bean (public
    // members or accessors).
    public static class PathTest {
        public String test;
        public Options options;
        public Bar bar;
    }

    public static class Location {
        public Float latitude;
        public Float longitude;
    }

    public static class Options {
        public String firstName;
        public String lastName;
        public Integer age;
        public Boolean bool;
        public Location location;
    }

    public static class Bar {
        public Integer numIndex;
        public String strIndex;
        public String objIndex;
        public Options options;
        public int[] numbers;
    }
}
