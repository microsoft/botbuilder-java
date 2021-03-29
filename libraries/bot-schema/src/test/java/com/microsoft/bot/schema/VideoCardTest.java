// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the VideoCard methods work as expected.
 */
public class VideoCardTest {
    VideoCard getCard() {
        VideoCard card = new VideoCard();
        card.setTitle("Test Video Title");
        card.setSubtitle("Test Video Subtitle");
        card.setText("Test Video Text");
        card.setImage(new ThumbnailUrl());
        return card;
    }

    /**
     *Ensures that the VideoCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = getCard().toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.video", attachment.getContentType());
    }
}
