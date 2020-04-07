// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamsfileupload;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.ResultPair;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.TextFormatTypes;
import com.microsoft.bot.schema.teams.FileConsentCard;
import com.microsoft.bot.schema.teams.FileConsentCardResponse;
import com.microsoft.bot.schema.teams.FileDownloadInfo;
import com.microsoft.bot.schema.teams.FileInfoCard;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>This is where application specific logic for interacting with the users would be
 * added.  For this sample, the {@link #onMessageActivity(TurnContext)} echos the text
 * back to the user.  The {@link #onMembersAdded(List, TurnContext)} will send a greeting
 * to new conversation participants.</p>
 */
@Component
public class TeamsFileUploadBot extends TeamsActivityHandler {
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        if (messageWithDownload(turnContext.getActivity())) {
            return downloadAttachment(turnContext.getActivity().getAttachments().get(0))
                .thenCompose(result -> !result.result()
                                       ? fileDownloadFailed(turnContext, result.value())
                                       : CompletableFuture.completedFuture(null));
        } else {
            File filePath = new File("files", "teams-logo.png");
            return sendFileCard(turnContext, filePath.getName(), filePath.length());
        }
    }

    @Override
    protected CompletableFuture<Void> onTeamsFileConsentAccept(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse
    ) {
        return upload(fileConsentCardResponse)
            .thenCompose(result -> !result.result()
                                   ? fileUploadFailed(turnContext, result.value())
                                   : fileUploadCompleted(turnContext, fileConsentCardResponse)
            );
    }

    @Override
    protected CompletableFuture<Void> onTeamsFileConsentDecline(
        TurnContext turnContext,
        FileConsentCardResponse fileConsentCardResponse
    ) {
        Map<String, String> context = (Map<String, String>) fileConsentCardResponse.getContext();

        Activity reply = MessageFactory.text(String.format(
            "Declined. We won't upload file <b>{context[%s]}</b>.", context.get("filename"))
        );
        reply.setTextFormat(TextFormatTypes.XML);

        return turnContext.sendActivityBlind(reply);
    }

    private CompletableFuture<Void> sendFileCard(TurnContext turnContext, String filename, long filesize) {
        Map<String, String> consentContext = new HashMap<>();
        consentContext.put("filename", filename);

        FileConsentCard fileCard = new FileConsentCard() {{
            setDescription("This is the file I want to send you");
            setSizeInBytes(filesize);
            setAcceptContext(consentContext);
            setDeclineContext(consentContext);
        }};

        Attachment asAttachment = new Attachment() {{
            setContent(fileCard);
            setContentType(FileConsentCard.CONTENT_TYPE);
            setName(filename);
        }};

        Activity reply = turnContext.getActivity().createReply();
        reply.setAttachments(Collections.singletonList(asAttachment));

        return turnContext.sendActivityBlind(reply);
    }

    private CompletableFuture<Void> fileUploadCompleted(
        TurnContext turnContext, FileConsentCardResponse fileConsentCardResponse
    ) {
        FileInfoCard downloadCard = new FileInfoCard() {{
            setUniqueId(fileConsentCardResponse.getUploadInfo().getUniqueId());
            setFileType(fileConsentCardResponse.getUploadInfo().getFileType());
        }};

        Attachment asAttachment = new Attachment() {{
            setContent(downloadCard);
            setContentType(FileInfoCard.CONTENT_TYPE);
            setName(fileConsentCardResponse.getUploadInfo().getName());
            setContentUrl(fileConsentCardResponse.getUploadInfo().getContentUrl());
        }};

        Activity reply = MessageFactory.text(
            String.format(
                "<b>File uploaded.</b> Your file <b>%s</b> is ready to download",
                fileConsentCardResponse.getUploadInfo().getName()
            )
        );
        reply.setTextFormat(TextFormatTypes.XML);
        reply.setAttachment(asAttachment);

        return turnContext.sendActivityBlind(reply);
    }

    private CompletableFuture<Void> fileUploadFailed(TurnContext turnContext, String error) {
        Activity reply = MessageFactory.text("<b>File upload failed.</b> Error: <pre>" + error + "</pre>");
        reply.setTextFormat(TextFormatTypes.XML);
        return turnContext.sendActivityBlind(reply);
    }

    private CompletableFuture<Void> fileDownloadFailed(TurnContext turnContext, String error) {
        Activity reply = MessageFactory.text("<b>File download failed.</b> Error: <pre>" + error + "</pre>");
        reply.setTextFormat(TextFormatTypes.XML);
        return turnContext.sendActivityBlind(reply);
    }

    private boolean messageWithDownload(Activity activity) {
        boolean messageWithFileDownloadInfo = false;
        if (activity.getAttachments() != null
            && activity.getAttachments().size() > 0
        ) {
            messageWithFileDownloadInfo = StringUtils.equalsIgnoreCase(
                activity.getAttachments().get(0).getContentType(),
                FileDownloadInfo.CONTENT_TYPE
            );
        }
        return messageWithFileDownloadInfo;
    }

    private CompletableFuture<ResultPair<String>> upload(FileConsentCardResponse fileConsentCardResponse) {
        AtomicReference<ResultPair<String>> result = new AtomicReference<>();

        return CompletableFuture.runAsync(() -> {
            Map<String, String> context = (Map<String, String>) fileConsentCardResponse.getContext();
            File filePath = new File("files", context.get("filename"));
            HttpURLConnection connection = null;
            FileInputStream fileStream = null;

            try {
                URL url = new URL(fileConsentCardResponse.getUploadInfo().getUploadUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length", Long.toString(filePath.length()));
                connection.setRequestProperty(
                    "Content-Range",
                    String.format("bytes 0-%d/%d", filePath.length() - 1, filePath.length())
                );

                fileStream = new FileInputStream(filePath);
                OutputStream uploadStream = connection.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytes_read;
                while ((bytes_read = fileStream.read(buffer)) != -1) {
                    uploadStream.write(buffer, 0, bytes_read);
                }

                result.set(new ResultPair<String>(true, null));
            } catch (Throwable t) {
                result.set(new ResultPair<String>(false, t.getLocalizedMessage()));
            } finally {
                if (connection != null) { connection.disconnect(); }
                if (fileStream != null) { try { fileStream.close(); } catch (Throwable ignored) { } }
            }
        })
            .thenApply(aVoid -> result.get());
    }

    private CompletableFuture<ResultPair<String>> downloadAttachment(Attachment attachment) {
        AtomicReference<ResultPair<String>> result = new AtomicReference<>();

        return CompletableFuture.runAsync(() -> {
            FileDownloadInfo fileDownload = Serialization.getAs(attachment.getContent(), FileDownloadInfo.class);
            String filePath = "files/" + attachment.getName();

            FileOutputStream fileStream = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(fileDownload.getDownloadUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);

                fileStream = new FileOutputStream(filePath);
                InputStream downloadStream = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int bytes_read;
                while ((bytes_read = downloadStream.read(buffer)) != -1) {
                    fileStream.write(buffer, 0, bytes_read);
                }

                result.set(new ResultPair<>(true, null));
            } catch (Throwable t) {
                result.set(new ResultPair<>(false, t.getLocalizedMessage()));
            } finally {
                if (connection != null) { connection.disconnect(); }
                if (fileStream != null) { try { fileStream.close(); } catch (Throwable ignored) { } }
            }
        })
            .thenApply(aVoid -> result.get());
    }
}
