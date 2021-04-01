/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

public class EntitySchemaValidationTest {
    @Test
    public void EntityTests_GeoCoordinatesSerializationDeserializationTest() {
        GeoCoordinates geoCoordinates = new GeoCoordinates();
        geoCoordinates.setLatitude(22.00);
        geoCoordinates.setLongitude(23.00);

        Assert.assertEquals("GeoCoordinates", geoCoordinates.getType());

        Entity deserializedEntity = new Entity().setAs(geoCoordinates);
        Assert.assertEquals(deserializedEntity.getType(), geoCoordinates.getType());

        GeoCoordinates geoDeserialized = deserializedEntity.getAs(GeoCoordinates.class);
        Assert.assertEquals(geoCoordinates.getType(), geoDeserialized.getType());
        Assert.assertEquals(
            geoCoordinates.getLatitude(), geoDeserialized.getLatitude(), Double.MAX_VALUE
        );
        Assert.assertEquals(
            geoCoordinates.getLongitude(), geoDeserialized.getLongitude(), Double.MAX_VALUE
        );
    }

    @Test
    public void EntityTests_MentionSerializationDeserializationTest() {
        Mention mentionEntity = new Mention();
        mentionEntity.setText("TESTTEST");

        Assert.assertEquals("mention", mentionEntity.getType());

        Entity deserializedEntity = new Entity().setAs(mentionEntity);
        Assert.assertEquals(deserializedEntity.getType(), mentionEntity.getType());
        Assert.assertEquals(
            deserializedEntity.getProperties().get("text").textValue(), mentionEntity.getText()
        );

        Mention mentionDeserialized = deserializedEntity.getAs(Mention.class);
        Assert.assertEquals(mentionEntity.getType(), mentionDeserialized.getType());
        Assert.assertEquals(
            deserializedEntity.getProperties().get("text").textValue(), mentionEntity.getText()
        );
    }

    @Test
    public void EntityTests_PlaceSerializationDeserializationTest() {
        Place placeEntity = new Place();
        placeEntity.setName("TESTTEST");

        Assert.assertEquals("Place", placeEntity.getType());

        Entity deserializedEntity = new Entity().setAs(placeEntity);
        Assert.assertEquals(deserializedEntity.getType(), placeEntity.getType());

        Place placeDeserialized = deserializedEntity.getAs(Place.class);
        Assert.assertEquals(placeEntity.getType(), placeDeserialized.getType());
    }
}
