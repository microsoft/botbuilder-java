// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs.choices;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Channels;
import org.apache.commons.lang3.StringUtils;

/**
 * Methods for determining channel specific functionality.
 */
public final class Channel {
    private Channel() { }

    /**
     * Determine if a number of Suggested Actions are supported by a Channel.
     *
     * @param channelId The Channel to check the if Suggested Actions are supported in.
     * @return True if the Channel supports the buttonCnt total Suggested Actions, False if
     * the Channel does not support that number of Suggested Actions.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static boolean supportsSuggestedActions(String channelId) {
        return supportsSuggestedActions(channelId, 100);
    }

    /**
     * Determine if a number of Suggested Actions are supported by a Channel.
     *
     * @param channelId The Channel to check the if Suggested Actions are supported in.
     * @param buttonCnt The number of Suggested Actions to check for the Channel.
     * @return True if the Channel supports the buttonCnt total Suggested Actions, False if
     * the Channel does not support that number of Suggested Actions.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static boolean supportsSuggestedActions(String channelId, int buttonCnt) {
        switch (channelId) {
            // https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies
            case Channels.FACEBOOK:
            case Channels.SKYPE:
                return buttonCnt <= 10;

            // https://developers.line.biz/en/reference/messaging-api/#items-object
            case Channels.LINE:
                return buttonCnt <= 13;

            // https://dev.kik.com/#/docs/messaging#text-response-object
            case Channels.KIK:
                return buttonCnt <= 20;

            case Channels.TELEGRAM:
            case Channels.EMULATOR:
            case Channels.DIRECTLINE:
            case Channels.DIRECTLINESPEECH:
            case Channels.WEBCHAT:
                return buttonCnt <= 100;

            default:
                return false;
        }
    }

    /**
     * Determine if a number of Card Actions are supported by a Channel.
     *
     * @param channelId The Channel to check if the Card Actions are supported in.
     * @return True if the Channel supports the buttonCnt total Card Actions, False if the
     * Channel does not support that number of Card Actions.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static boolean supportsCardActions(String channelId) {
        return supportsCardActions(channelId, 100);
    }

    /**
     * Determine if a number of Card Actions are supported by a Channel.
     *
     * @param channelId The Channel to check if the Card Actions are supported in.
     * @param buttonCnt The number of Card Actions to check for the Channel.
     * @return True if the Channel supports the buttonCnt total Card Actions, False if the
     * Channel does not support that number of Card Actions.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static boolean supportsCardActions(String channelId, int buttonCnt) {
        switch (channelId) {
            case Channels.FACEBOOK:
            case Channels.SKYPE:
            case Channels.MSTEAMS:
                return buttonCnt <= 3;

            case Channels.LINE:
                return buttonCnt <= 99;

            case Channels.SLACK:
            case Channels.EMULATOR:
            case Channels.DIRECTLINE:
            case Channels.DIRECTLINESPEECH:
            case Channels.WEBCHAT:
            case Channels.CORTANA:
                return buttonCnt <= 100;

            default:
                return false;
        }
    }

    /**
     * Determine if a Channel has a Message Feed.
     * @param channelId The Channel to check for Message Feed.
     * @return True if the Channel has a Message Feed, False if it does not.
     */
    public static boolean hasMessageFeed(String channelId) {
        switch (channelId) {
            case Channels.CORTANA:
                return false;

            default:
                return true;
        }
    }

    /**
     * Maximum length allowed for Action Titles.
     *
     * @param channelId The Channel to determine Maximum Action Title Length.
     * @return The total number of characters allowed for an Action Title on a specific Channel.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static int maxActionTitleLength(String channelId) {
        return 20;
    }

    /**
     * Get the Channel Id from the current Activity on the Turn Context.
     *
     * @param turnContext The Turn Context to retrieve the Activity's Channel Id from.
     * @return The Channel Id from the Turn Context's Activity.
     */
    public static String getChannelId(TurnContext turnContext) {
        return StringUtils.isEmpty(turnContext.getActivity().getChannelId())
            ? null
            : turnContext.getActivity().getChannelId();
    }
}
