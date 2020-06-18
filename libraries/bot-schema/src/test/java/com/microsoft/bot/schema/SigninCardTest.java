// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import org.junit.Assert;
import org.junit.Test;

public class SigninCardTest {
    SigninCard card = new SigninCard(){
        {
            setText("Test Signin Text");
        }
    };

    /**
     *Ensures that the SigninCard can be used as an attachment.
     */
    @Test
    public void testToAttachment() {
        Attachment attachment = card.toAttachment();
        Assert.assertNotNull(attachment);
        Assert.assertEquals("application/vnd.microsoft.card.signin", attachment.getContentType());
    }
}
