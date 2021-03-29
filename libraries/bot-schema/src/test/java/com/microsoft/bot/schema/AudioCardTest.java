// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the AudioCard methods work as expected.
 */
public class AudioCardTest {
    ArrayList<MediaUrl> media = new ArrayList<MediaUrl>();

    AudioCard getCard() {
        AudioCard card = new AudioCard();
        card.setText("Test Audio Text");
        card.setMedia(media);
        return card;
    }

    /**
     *Ensures that the AudioCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = getCard().toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.audio", attachment.getContentType());
    }
}
