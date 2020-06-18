// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

public class ThumbnailCardTest {
    ThumbnailCard card = new ThumbnailCard(){
        {
            setText("Test Thumbnail Text");
            setTitle("Test Thumbnail Title");
            setSubtitle("Test Thumbnail Subtitle");
            setImage(new CardImage());
            setTap(new CardAction());
        }
    };

    /**
     *Ensures that the ThumbnailCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = card.toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.thumbnail", attachment.getContentType());
    }
}
