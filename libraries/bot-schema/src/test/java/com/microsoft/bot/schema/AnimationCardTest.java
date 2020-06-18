// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the AnimationCard methods work as expected.
 */
public class AnimationCardTest {
    ArrayList<MediaUrl> media = new ArrayList<MediaUrl>();

    AnimationCard card = new AnimationCard(){
        {
            setText("Test Animation Text");
            setMedia(media);
        }
    };

    @Test
    /**
     *Ensures that the AnimationCard can be used as an attachment.
     */
    public void testToAttachment() {
        Attachment attachment = card.toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.animation", attachment.getContentType());
    }
}
