// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.
package com.microsoft.bot.builder;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Support the DirectLine speech and telephony channels to ensure the
 * appropriate SSML tags are set on the Activity Speak property.
 */
public class SetSpeakMiddleware implements Middleware {

    private final String voiceName;
    private final boolean fallbackToTextForSpeak;

    /**
     * Initializes a new instance of the {@link SetSpeakMiddleware} class.
     *
     * @param voiceName              The SSML voice name attribute value.
     * @param fallbackToTextForSpeak true if an empt Activity.Speak is populated
     *                               with Activity.getText().
     */
    public SetSpeakMiddleware(String voiceName, boolean fallbackToTextForSpeak) {
        this.voiceName = voiceName;
        this.fallbackToTextForSpeak = fallbackToTextForSpeak;
    }

    /**
     * Processes an incoming activity.
     *
     * @param turnContext The context Object for this turn.
     * @param next        The delegate to call to continue the bot middleware
     *                    pipeline.
     *
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> onTurn(TurnContext turnContext, NextDelegate next) {
        turnContext.onSendActivities((ctx, activities, nextSend) -> {
            for (Activity activity : activities) {
                if (activity.getType().equals(ActivityTypes.MESSAGE)) {
                    if (fallbackToTextForSpeak && StringUtils.isBlank(activity.getSpeak())) {
                        activity.setSpeak(activity.getText());
                    }

                    if (StringUtils.isNotBlank(activity.getSpeak()) && StringUtils.isNotBlank(voiceName)
                            && (StringUtils.compareIgnoreCase(turnContext.getActivity().getChannelId(),
                                    Channels.DIRECTLINESPEECH) == 0
                                    || StringUtils.compareIgnoreCase(turnContext.getActivity().getChannelId(),
                                            Channels.EMULATOR) == 0
                                    || StringUtils.compareIgnoreCase(turnContext.getActivity().getChannelId(),
                                            "telephony") == 0)) {
                        if (!hasTag("speak", activity.getSpeak()) && !hasTag("voice", activity.getSpeak())) {
                                activity.setSpeak(
                                        String.format("<voice name='%s'>%s</voice>", voiceName, activity.getSpeak()));
                            }
                            activity.setSpeak(String
                                    .format("<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' "
                                            + "xml:lang='%s'>%s</speak>",
                                            activity.getLocale() != null ? activity.getLocale() : "en-US",
                                            activity.getSpeak()));
                        }
                    }
                }
                return nextSend.get();
            });
        return next.next();
    }

    private boolean hasTag(String tagName, String speakText) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(speakText);

            if (doc.getElementsByTagName(tagName).getLength() > 0) {
                return true;
            }

            return false;
        } catch (SAXException | ParserConfigurationException | IOException ex) {
            return false;
        }
    }
}
