// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the ThumbnailCard methods work as expected.
 */
public class ThumbnailCardTest {
    ThumbnailCard getCard() {
        ThumbnailCard card = new ThumbnailCard();
        card.setText("Test Thumbnail Text");
        card.setTitle("Test Thumbnail Title");
        card.setSubtitle("Test Thumbnail Subtitle");
        card.setImage(new CardImage());
        card.setTap(new CardAction());
        return card;
    }

    /**
     *Ensures that the ThumbnailCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = getCard().toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.thumbnail", attachment.getContentType());
    }
}
