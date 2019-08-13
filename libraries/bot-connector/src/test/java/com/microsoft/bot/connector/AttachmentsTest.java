package com.microsoft.bot.connector;

import com.microsoft.bot.schema.models.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

public class AttachmentsTest extends BotConnectorTestBase {

    @Test
    public void GetAttachmentInfo() {

        AttachmentData attachment = new AttachmentData()
                .withName("bot-framework.png")
                .withType("image/png")
                .withOriginalBase64(encodeToBase64(new File(getClass().getClassLoader().getResource("bot-framework.png").getFile())));

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse attachmentResponse = connector.conversations().uploadAttachment(conversation.id(), attachment);

        AttachmentInfo response = connector.attachments().getAttachmentInfo(attachmentResponse.id());

        Assert.assertEquals(attachment.name(), response.name());
    }

    @Test
    public void GetAttachment() {

        File attachmentFile = new File(getClass().getClassLoader().getResource("bot_icon.png").getFile());
        byte[] attachmentPayload = encodeToBase64(attachmentFile);

        InputStream attachmentStream = null;
        try {
            attachmentStream = new FileInputStream(attachmentFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AttachmentData attachment = new AttachmentData()
                .withName("bot_icon.png")
                .withType("image/png")
                .withOriginalBase64(attachmentPayload);

        ConversationParameters createMessage = new ConversationParameters()
                .withMembers(Collections.singletonList(user))
                .withBot(bot);

        ConversationResourceResponse conversation = connector.conversations().createConversation(createMessage);

        ResourceResponse attachmentResponse = connector.conversations().uploadAttachment(conversation.id(), attachment);

        AttachmentInfo attachmentInfo = connector.attachments().getAttachmentInfo(attachmentResponse.id());

        for (AttachmentView attView : attachmentInfo.views()) {
            InputStream retrievedAttachment = connector.attachments().getAttachment(attachmentResponse.id(), attView.viewId());

            Assert.assertTrue(isSame(retrievedAttachment, attachmentStream));
        }
    }

    private byte[] encodeToBase64(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] result = new byte[(int)file.length()];
            int size = fis.read(result);
            return result;
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    private boolean isSame(InputStream expected, InputStream actual) {
        boolean error = false;
        try {
            byte[] buffer1 = new byte[1024];
            byte[] buffer2 = new byte[1024];
            try {
                int numRead1 = 0;
                int numRead2 = 0;
                while (true) {
                    numRead1 = expected.read(buffer1);
                    numRead2 = actual.read(buffer2);
                    if (numRead1 > -1) {
                        if (numRead2 != numRead1) return false;
                        // Otherwise same number of bytes read
                        if (!Arrays.equals(buffer1, buffer2)) return false;
                        // Otherwise same bytes read, so continue ...
                    } else {
                        // Nothing more in stream 1 ...
                        return numRead2 < 0;
                    }
                }
            } finally {
                expected.close();
            }
        } catch (IOException | RuntimeException e) {
            error = true; // this error should be thrown, even if there is an error closing stream 2
            return false;
        } finally {
            try {
                actual.close();
            } catch (IOException e) {
                if (!error) return false;
            }
        }
    }
}
