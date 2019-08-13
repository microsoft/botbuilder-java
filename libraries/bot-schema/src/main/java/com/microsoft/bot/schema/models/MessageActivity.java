// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.microsoft.bot.schema.models;

import com.microsoft.bot.schema.ActivityImpl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

    /**
     * A message in a conversation
     */
public class MessageActivity extends ActivityImpl
{
    /**
     * The language code of the Text field
     */
    private String locale;
    public String getLocale() {
        return this.locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;

    }

    /**
     * Content for the message
     */
    private String text;
    public void setText(String text){
        this.text = text;
    }
    public String getText() {

        return this.text;
    }

    /**
     * Speak tag (SSML markup for text to speech)
     */
    private String speak;
    public void setSpeak(String speak){
        this.speak = speak;
    }
    public String getSpeak(){
        return this.speak;
    }

    /**
     * Indicates whether the bot is accepting, expecting, or ignoring input
     */
    private String inputHint;
    public String getInputHint(){
        return this.inputHint;
    }
    public void setInputHint(String inputHint){
        this.inputHint = inputHint;
    }

    /**
     * Text to display if the channel cannot render cards
     */
    private String summary;
    public String getSummary(){
        return this.summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Format of text fields [plain|markdown] Default:markdown
     */
    private String textFormat;
    public String getTextFormat() {

        return this.textFormat;
    }
    public void setTextFormat(String textFormat) {
        this.textFormat = textFormat;
    }

    /**
     * Hint for how to deal with multiple attachments: [list|carousel] Default:list
     */
    private String attachmentLayout;
    public String getAttachmentLayout() {
        return this.attachmentLayout;
    }
    public void setAttachmentLayout(String attachmentLayout) {
        this.attachmentLayout = attachmentLayout;
    }

    /**
     * Attachments
     */
    private List<Attachment> attachments;
    public List<Attachment> getAttachments() {
        return this.attachments;
    }
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * SuggestedActions are used to express actions for interacting with a card like keyboards/quickReplies
     */
    private SuggestedActions suggestedActions;
    public SuggestedActions getSuggestedActions() {
        return this.suggestedActions;
    }
    public void setSuggestedActions(SuggestedActions suggestedActions) {
        this.suggestedActions = suggestedActions;
    }


    /**
     * Importance of the activity
     * Valid values are "low", "normal", and "high". Default value is "normal."
     */
    private String importance;
    public String getImportance() {
        return this.importance;
    }
    public void setImportance(String importance) {
        this.importance = importance;
    }

    /**
     * Hint to describe how this activity should be delivered.
     * null or "default" = default delivery
     * "notification" = notification semantics
     * See DeliveryModes for current constants
     */
    private String deliveryMode;
    public String getDeliveryMode() {
        return this.deliveryMode;
    }
    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    /**
     * DateTime to expire the activity as ISO 8601 encoded datetime
     */
    private OffsetDateTime expiration;
    public OffsetDateTime getExpiration() {
        return this.expiration;
    }
    public void setExpiration(OffsetDateTime expiration) {
        this.expiration = expiration;
    }

    /**
     * Get mentions
     */
    private ArrayList<Mention> mentions;
    public ArrayList<Mention> GetMentions() {
        return this.mentions;
    }

    /**
     * Value provided with CardAction
     */
    private Object value;
    public Object getValue() {
        return this.value;
    }
    public void setValue(Object value) {
        this.value = value;
    }


    /**
     * Create an instance of the Activity class with IConversationUpdateActivity masking
     */
    public static ConversationUpdateActivity CreateConversationUpdateActivity()
    {
        ConversationUpdateActivity conversationActivity =  new ConversationUpdateActivity();
        conversationActivity.withType(ActivityTypes.CONVERSATION_UPDATE);
        conversationActivity.withMembersAdded(new ArrayList<ChannelAccount>());
        conversationActivity.withMembersRemoved(new ArrayList<ChannelAccount>());
        return conversationActivity;
    }

}
