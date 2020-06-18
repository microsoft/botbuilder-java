// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

public class ReceiptCardTest {

    ReceiptCard card = new ReceiptCard() {
        {
            setTitle("John Doe");
        }
    };

    /**
     * Ensures that the ReceiptCard can be added as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = card.toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.receipt", attachment.getContentType());
    }
}
