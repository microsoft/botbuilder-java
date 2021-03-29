// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.HeroCard;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ChoiceFactoryTests {
    private static List<Choice> colorChoices = Arrays.asList(new Choice("red"), new Choice("green"), new Choice("blue"));
    private static List<Choice> extraChoices = Arrays.asList(new Choice("red"), new Choice("green"), new Choice("blue"), new Choice("alpha"));
    private static List<Choice> choicesWithActions = Arrays.asList(
        new Choice("ImBack") {{
            setAction(new CardAction() {{
                setType(ActionTypes.IM_BACK);
                setTitle("ImBack Action");
                setValue("ImBack Value");
            }});
        }},
        new Choice("MessageBack") {{
            setAction(new CardAction() {{
                setType(ActionTypes.MESSAGE_BACK);
                setTitle("MessageBack Action");
                setValue("MessageBack Value");
            }});
        }},
        new Choice("PostBack") {{
            setAction(new CardAction() {{
                setType(ActionTypes.POST_BACK);
                setTitle("PostBack Action");
                setValue("PostBack Value");
            }});
        }}
    );

    @Test
    public void shouldRenderChoicesInline() {
        Activity activity = ChoiceFactory.inline(colorChoices, "select from:");
        Assert.assertEquals("select from: (1) red, (2) green, or (3) blue", activity.getText());
    }

    @Test
    public void shouldRenderChoicesAsAList() {
        Activity activity = ChoiceFactory.list(colorChoices, "select from:");
        Assert.assertEquals("select from:\n\n   1. red\n   2. green\n   3. blue", activity.getText());
    }

    @Test
    public void shouldRenderUnincludedNumbersChoicesAsAList() {
        ChoiceFactoryOptions choiceFactoryOptions  = new ChoiceFactoryOptions();
        choiceFactoryOptions.setIncludeNumbers(false);

        Activity activity = ChoiceFactory.list(colorChoices, "select from:", null, choiceFactoryOptions);
        Assert.assertEquals("select from:\n\n   - red\n   - green\n   - blue", activity.getText());
    }

    @Test
    public void shouldRenderChoicesAsSuggestedActions() {
        Activity activity = ChoiceFactory.suggestedAction(colorChoices, "select from:");
        Assert.assertEquals("select from:", activity.getText());
        Assert.assertNotNull(activity.getSuggestedActions());
        Assert.assertEquals(3, activity.getSuggestedActions().getActions().size());
        Assert.assertEquals(ActionTypes.IM_BACK, activity.getSuggestedActions().getActions().get(0).getType());
        Assert.assertEquals("red", activity.getSuggestedActions().getActions().get(0).getValue());
        Assert.assertEquals("red", activity.getSuggestedActions().getActions().get(0).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, activity.getSuggestedActions().getActions().get(1).getType());
        Assert.assertEquals("green", activity.getSuggestedActions().getActions().get(1).getValue());
        Assert.assertEquals("green", activity.getSuggestedActions().getActions().get(1).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, activity.getSuggestedActions().getActions().get(2).getType());
        Assert.assertEquals("blue", activity.getSuggestedActions().getActions().get(2).getValue());
        Assert.assertEquals("blue", activity.getSuggestedActions().getActions().get(2).getTitle());
    }

    @Test
    public void shouldRenderChoicesAsHeroCard() {
        Activity activity = ChoiceFactory.heroCard(colorChoices, "select from:");

        Assert.assertNotNull(activity.getAttachments());

        HeroCard heroCard = (HeroCard)activity.getAttachments().get(0).getContent();

        Assert.assertEquals(3, heroCard.getButtons().size());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(0).getType());
        Assert.assertEquals("red", heroCard.getButtons().get(0).getValue());
        Assert.assertEquals("red", heroCard.getButtons().get(0).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(1).getType());
        Assert.assertEquals("green", heroCard.getButtons().get(1).getValue());
        Assert.assertEquals("green", heroCard.getButtons().get(1).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(2).getType());
        Assert.assertEquals("blue", heroCard.getButtons().get(2).getValue());
        Assert.assertEquals("blue", heroCard.getButtons().get(2).getTitle());
    }

    @Test
    public void shouldAutomaticallyChooseRenderStyleBasedOnChannelType() {
        Activity activity = ChoiceFactory.forChannel(Channels.EMULATOR, colorChoices, "select from:");
        Assert.assertEquals("select from:", activity.getText());
        Assert.assertNotNull(activity.getSuggestedActions());
        Assert.assertEquals(3, activity.getSuggestedActions().getActions().size());
        Assert.assertEquals(ActionTypes.IM_BACK, activity.getSuggestedActions().getActions().get(0).getType());
        Assert.assertEquals("red", activity.getSuggestedActions().getActions().get(0).getValue());
        Assert.assertEquals("red", activity.getSuggestedActions().getActions().get(0).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, activity.getSuggestedActions().getActions().get(1).getType());
        Assert.assertEquals("green", activity.getSuggestedActions().getActions().get(1).getValue());
        Assert.assertEquals("green", activity.getSuggestedActions().getActions().get(1).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, activity.getSuggestedActions().getActions().get(2).getType());
        Assert.assertEquals("blue", activity.getSuggestedActions().getActions().get(2).getValue());
        Assert.assertEquals("blue", activity.getSuggestedActions().getActions().get(2).getTitle());
    }

    @Test
    public void shouldChooseCorrectStylesForCortana() {
        Activity activity = ChoiceFactory.forChannel(Channels.CORTANA, colorChoices, "select from:");

        Assert.assertNotNull(activity.getAttachments());

        HeroCard heroCard = (HeroCard)activity.getAttachments().get(0).getContent();

        Assert.assertEquals(3, heroCard.getButtons().size());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(0).getType());
        Assert.assertEquals("red", heroCard.getButtons().get(0).getValue());
        Assert.assertEquals("red", heroCard.getButtons().get(0).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(1).getType());
        Assert.assertEquals("green", heroCard.getButtons().get(1).getValue());
        Assert.assertEquals("green", heroCard.getButtons().get(1).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(2).getType());
        Assert.assertEquals("blue", heroCard.getButtons().get(2).getValue());
        Assert.assertEquals("blue", heroCard.getButtons().get(2).getTitle());
    }

    @Test
    public void shouldChooseCorrectStylesForTeams() {
        Activity activity = ChoiceFactory.forChannel(Channels.MSTEAMS, colorChoices, "select from:");

        Assert.assertNotNull(activity.getAttachments());

        HeroCard heroCard = (HeroCard)activity.getAttachments().get(0).getContent();

        Assert.assertEquals(3, heroCard.getButtons().size());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(0).getType());
        Assert.assertEquals("red", heroCard.getButtons().get(0).getValue());
        Assert.assertEquals("red", heroCard.getButtons().get(0).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(1).getType());
        Assert.assertEquals("green", heroCard.getButtons().get(1).getValue());
        Assert.assertEquals("green", heroCard.getButtons().get(1).getTitle());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(2).getType());
        Assert.assertEquals("blue", heroCard.getButtons().get(2).getValue());
        Assert.assertEquals("blue", heroCard.getButtons().get(2).getTitle());
    }

    @Test
    public void shouldIncludeChoiceActionsInSuggestedActions() {
        Activity activity = ChoiceFactory.suggestedAction(choicesWithActions, "select from:");
        Assert.assertEquals("select from:", activity.getText());
        Assert.assertNotNull(activity.getSuggestedActions());
        Assert.assertEquals(3, activity.getSuggestedActions().getActions().size());
        Assert.assertEquals(ActionTypes.IM_BACK, activity.getSuggestedActions().getActions().get(0).getType());
        Assert.assertEquals("ImBack Value", activity.getSuggestedActions().getActions().get(0).getValue());
        Assert.assertEquals("ImBack Action", activity.getSuggestedActions().getActions().get(0).getTitle());
        Assert.assertEquals(ActionTypes.MESSAGE_BACK, activity.getSuggestedActions().getActions().get(1).getType());
        Assert.assertEquals("MessageBack Value", activity.getSuggestedActions().getActions().get(1).getValue());
        Assert.assertEquals("MessageBack Action", activity.getSuggestedActions().getActions().get(1).getTitle());
        Assert.assertEquals(ActionTypes.POST_BACK, activity.getSuggestedActions().getActions().get(2).getType());
        Assert.assertEquals("PostBack Value", activity.getSuggestedActions().getActions().get(2).getValue());
        Assert.assertEquals("PostBack Action", activity.getSuggestedActions().getActions().get(2).getTitle());
    }

    @Test
    public void shouldIncludeChoiceActionsInHeroCards() {
        Activity activity = ChoiceFactory.heroCard(choicesWithActions, "select from:");

        Assert.assertNotNull(activity.getAttachments());

        HeroCard heroCard = (HeroCard)activity.getAttachments().get(0).getContent();

        Assert.assertEquals(3, heroCard.getButtons().size());
        Assert.assertEquals(ActionTypes.IM_BACK, heroCard.getButtons().get(0).getType());
        Assert.assertEquals("ImBack Value", heroCard.getButtons().get(0).getValue());
        Assert.assertEquals("ImBack Action", heroCard.getButtons().get(0).getTitle());
        Assert.assertEquals(ActionTypes.MESSAGE_BACK, heroCard.getButtons().get(1).getType());
        Assert.assertEquals("MessageBack Value", heroCard.getButtons().get(1).getValue());
        Assert.assertEquals("MessageBack Action", heroCard.getButtons().get(1).getTitle());
        Assert.assertEquals(ActionTypes.POST_BACK, heroCard.getButtons().get(2).getType());
        Assert.assertEquals("PostBack Value", heroCard.getButtons().get(2).getValue());
        Assert.assertEquals("PostBack Action", heroCard.getButtons().get(2).getTitle());
    }
}
