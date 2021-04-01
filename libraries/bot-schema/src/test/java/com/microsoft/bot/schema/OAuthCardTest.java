// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the OAuthCard methods work as expected.
 */
public class OAuthCardTest {
    OAuthCard getCard() {
        OAuthCard card = new OAuthCard();
        card.setText("Test OAuth Text");
        card.setConnectionName("Test Connection Name");
        return card;
    }

    /**
     *Ensures that the OAuthCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = getCard().toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.oauth", attachment.getContentType());
    }
}
