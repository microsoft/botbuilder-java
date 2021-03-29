// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the HeroCard methods work as expected.
 */
public class HeroCardTest {
    HeroCard getCard() {
        HeroCard card = new HeroCard();
        card.setTitle("Hero Card Title");
        card.setSubtitle("Hero Card Subtitle");
        card.setText("Testing Text.");
        return card;
    }

    /**
     * Ensures that the HeroCard can be added as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = getCard().toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.hero", attachment.getContentType());
    }
}
