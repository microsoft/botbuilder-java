// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

/***
 * Tests to ensure the SigninCard methods work as expected.
 */
public class SigninCardTest {
    SigninCard getCard() {
        SigninCard card = new SigninCard();
        card.setText("Test Signin Text");
        return card;
    }

    /**
     *Ensures that the SigninCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = getCard().toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.signin", attachment.getContentType());
    }
}
