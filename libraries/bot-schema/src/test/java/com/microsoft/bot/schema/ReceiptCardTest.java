// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the AnimationCard methods work as expected.
 */
public class ReceiptCardTest {

    ReceiptCard getCard() {
        ReceiptCard card = new ReceiptCard();
        card.setTitle("John Doe");
        return card;
    }

    /**
     * Ensures that the ReceiptCard can be added as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = getCard().toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.receipt", attachment.getContentType());
    }
}
