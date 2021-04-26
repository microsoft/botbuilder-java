// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

public class CardActionTest {
    // this really isn't an implicit conversion. it just matches the dotnet
    // test. This tests the CardAction[] SuggestedActions constructor.
    @Test
    public void TestImplicitConversation() {
        SuggestedActions actions = new SuggestedActions(
            new CardAction[] { new CardAction("x"), new CardAction("y"), new CardAction("z") }
        );

        Assert.assertEquals("x", actions.getActions().get(0).getTitle());
        Assert.assertEquals("x", actions.getActions().get(0).getValue());
        Assert.assertEquals("y", actions.getActions().get(1).getTitle());
        Assert.assertEquals("y", actions.getActions().get(1).getValue());
        Assert.assertEquals("z", actions.getActions().get(2).getTitle());
        Assert.assertEquals("z", actions.getActions().get(2).getValue());
    }

    @Test
    public void TestClone() {

        CardAction cardAction =  new CardAction();
        cardAction.setChannelData("channelData");
        cardAction.setDisplayText("displayTest");
        cardAction.setImage("image");
        cardAction.setImageAltText("imageAltText");
        cardAction.setText("text");
        cardAction.setTitle("title");
        cardAction.setType(ActionTypes.CALL);
        cardAction.setValue("value");

        CardAction newCardAction = CardAction.clone(cardAction);

        Assert.assertEquals(cardAction.getChannelData(), newCardAction.getChannelData());
        Assert.assertEquals(cardAction.getDisplayText(), newCardAction.getDisplayText());
        Assert.assertEquals(cardAction.getImage(), newCardAction.getImage());
        Assert.assertEquals(cardAction.getImageAltText(), newCardAction.getImageAltText());
        Assert.assertEquals(cardAction.getText(), newCardAction.getText());
        Assert.assertEquals(cardAction.getTitle(), newCardAction.getTitle());
        Assert.assertEquals(cardAction.getType(), newCardAction.getType());
        Assert.assertEquals(cardAction.getValue(), newCardAction.getValue());
    }
    @Test
    public void TestCloneNull() {
        CardAction newCardAction = CardAction.clone(null);
        Assert.assertNull(newCardAction);
    }

    @Test
    public void TestConstructorTwoParams() {
        CardAction cardAction =  new CardAction(ActionTypes.CALL, "title");
        Assert.assertEquals(cardAction.getType(), ActionTypes.CALL);
        Assert.assertEquals(cardAction.getTitle(), "title");
    }

    @Test
    public void TestConstructorThreeParams() {
        CardAction cardAction =  new CardAction(ActionTypes.CALL, "title", "value");
        Assert.assertEquals(cardAction.getType(), ActionTypes.CALL);
        Assert.assertEquals(cardAction.getTitle(), "title");
        Assert.assertEquals(cardAction.getValue(), "value");
    }

}
