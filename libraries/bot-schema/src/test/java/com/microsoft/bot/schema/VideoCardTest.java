// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the VideoCard methods work as expected.
 */
public class VideoCardTest {
    VideoCard card = new VideoCard(){
        {
            setTitle("Test Video Title");
            setSubtitle("Test Video Subtitle");
            setText("Test Video Text");
            setImage(new ThumbnailUrl());
        }
    };

    /**
     *Ensures that the VideoCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = card.toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.video", attachment.getContentType());
    }
}
