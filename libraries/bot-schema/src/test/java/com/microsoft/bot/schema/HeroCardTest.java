// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the HeroCard methods work as expected.
 */
public class HeroCardTest {
    HeroCard card = new HeroCard(){
        {
            setTitle("Hero Card Title");
            setSubtitle("Hero Card Subtitle");
            setText("Testing Text.");
        }
    };

    /**
     * Ensures that the HeroCard can be added as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = card.toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.hero", attachment.getContentType());
    }
}
