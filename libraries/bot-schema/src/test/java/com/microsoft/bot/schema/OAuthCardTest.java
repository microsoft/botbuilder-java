// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the OAuthCard methods work as expected.
 */
public class OAuthCardTest {
    OAuthCard card = new OAuthCard(){
        {
            setText("Test OAuth Text");
            setConnectionName("Test Connection Name");
        }
    };

    /**
     *Ensures that the OAuthCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = card.toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.oauth", attachment.getContentType());
    }
}
