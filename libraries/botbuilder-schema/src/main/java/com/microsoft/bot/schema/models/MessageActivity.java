// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.microsoft.bot.schema.models;

import java.util.ArrayList;
import java.util.List;

    /// <summary>
    /// A message in a conversation
    /// </summary>
public class MessageActivity extends Activity
{
    /// <summary>
    /// The language code of the Text field
    /// </summary>
    /// <remarks>
    /// See https://msdn.microsoft.com/en-us/library/hh456380.aspx for a list of valid language codes
    /// </remarks>
    String _locale;
    String getLocale() {
        return this._locale;
    }
    void setLocale(String locale) {
        this._locale = locale;

    }

    /// <summary>
    /// Content for the message
    /// </summary>
    void setText(String text){

    }
    String getText() {
        return "";
    }

    /// <summary>
    /// Speak tag (SSML markup for text to speech)
    /// </summary>
    void setSpeak(String speak){

    }
    String getSpeak(){
        return "";
    }

    /// <summary>
    /// Indicates whether the bot is accepting, expecting, or ignoring input
    /// </summary>
    String getInputHint(){
        return "";
    }
    void setInputHint(String inputhint){

    }

    /// <summary>
    /// Text to display if the channel cannot render cards
    /// </summary>
    String getSummary(){
        return "";
    }
    void setSummary(String summary) {

    }

    /// <summary>
    /// Format of text fields [plain|markdown] Default:markdown
    /// </summary>
    String getTextFormat() {
        return "";
    }
    void setTextFormat(String textformat) {

    }

    /// <summary>
    /// Hint for how to deal with multiple attachments: [list|carousel] Default:list
    /// </summary>
    String getAttachmentLayout() {
        return "";
    }
    void setAttachmentLayout(String layout) {

    }

    /// <summary>
    /// Attachments
    /// </summary>
    List<Attachment> getAttachments() {
        return null;
    }
    void setAttachments(List<Attachment> attachments) {

    }

    /// <summary>
    /// SuggestedActions are used to express actions for interacting with a card like keyboards/quickReplies
    /// </summary>
        // TODO: daveta
    // SuggestedActions SuggestedActions { get; set; }

    /// <summary>
    /// Importance of the activity
    /// Valid values are "low", "normal", and "high". Default value is "normal."
    /// </summary>
        // TODO: daveta
    //String Importance { get; set; }

    /// <summary>
    /// Hint to describe how this activity should be delivered.
    /// null or "default" = default delivery
    /// "notification" = notification semantics
    /// See DeliveryModes for current constants
    /// </summary>
        // TODO: daveta
/*
    String DeliveryMode { get; set; }

    /// <summary>
    /// DateTime to expire the activity as ISO 8601 encoded datetime
    /// </summary>
    DateTimeOffset? Expiration { get; set; }

    /// <summary>
    /// Get mentions
    /// </summary>
    Mention[] GetMentions();

    /// <summary>
    /// Value provided with CardAction
    /// </summary>
    object Value { get; set; }

    /// <summary>
    /// True if this activity has text, attachments, or channelData
    /// </summary>
    bool HasContent();
    */
    /// <summary>
    /// Create an instance of the Activity class with IConversationUpdateActivity masking
    /// </summary>
    public static ConversationUpdateActivity CreateConversationUpdateActivity()
    {
        return (ConversationUpdateActivity) new MessageActivity()
                .withType(ActivityTypes.CONVERSATION_UPDATE)
                .withMembersAdded(new ArrayList<ChannelAccount>())
                .withMembersRemoved(new ArrayList<ChannelAccount>());
    }

}
