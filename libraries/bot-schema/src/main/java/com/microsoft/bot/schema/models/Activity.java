/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 */

package com.microsoft.bot.schema.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.bot.schema.EntityImpl;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An Activity is the basic communication type for the Bot Framework 3.0
 * protocol.
 */
public class Activity {
    /**
     * The type of the activity. Possible values include: 'message',
     * 'contactRelationUpdate', 'conversationUpdate', 'typing', 'ping',
     * 'endOfConversation', 'event', 'invoke', 'deleteUserData',
     * 'messageUpdate', 'messageDelete', 'installationUpdate',
     * 'messageReaction', 'suggestion', 'trace'.
     */
    @JsonProperty(value = "type")
    private ActivityTypes type;

    /**
     * Contains an ID that uniquely identifies the activity on the channel.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * Contains the date and time that the message was sent, in UTC, expressed in ISO-8601 format.
     */
    @JsonProperty(value = "timestamp")
    private DateTime timestamp;

    /**
     * Contains the local date and time of the message, expressed in ISO-8601 format.
     * For example, 2016-09-23T13:07:49.4714686-07:00.
     */
    @JsonProperty(value = "localTimestamp")
    private DateTime localTimestamp;

    /**
     * Contains the name of the local timezone of the message, expressed in IANA Time Zone database format.
     * For example, America/Los_Angeles.
     */
    @JsonProperty(value = "localTimezone")
    private String localTimezone;

    /**
     * A string containing an IRI identifying the caller of a bot. This field is not intended to be transmitted
     * over the wire, but is instead populated by bots and clients based on cryptographically verifiable data
     * that asserts the identity of the callers (e.g. tokens).
     */
    @JsonProperty(value = "callerId")
    private String callerId;

    /**
     * Contains the URL that specifies the channel's service endpoint. Set by the channel.
     */
    @JsonProperty(value = "serviceUrl")
    private String serviceUrl;

    /**
     * Contains an ID that uniquely identifies the channel. Set by the channel.
     */
    @JsonProperty(value = "channelId")
    private String channelId;

    /**
     * Identifies the sender of the message.
     */
    @JsonProperty(value = "from")
    private ChannelAccount from;

    /**
     * Identifies the conversation to which the activity belongs.
     */
    @JsonProperty(value = "conversation")
    private ConversationAccount conversation;

    /**
     * Identifies the recipient of the message.
     */
    @JsonProperty(value = "recipient")
    private ChannelAccount recipient;

    /**
     * Format of text fields Default:markdown. Possible values include:
     * 'markdown', 'plain', 'xml'.
     */
    @JsonProperty(value = "textFormat")
    private TextFormatTypes textFormat;

    /**
     * The layout hint for multiple attachments. Default: list.
     */
    @JsonProperty(value = "attachmentLayout")
    private AttachmentLayoutTypes attachmentLayout;

    /**
     * The collection of members added to the conversation.
     */
    @JsonProperty(value = "membersAdded")
    private List<ChannelAccount> membersAdded;

    /**
     * The collection of members removed from the conversation.
     */
    @JsonProperty(value = "membersRemoved")
    private List<ChannelAccount> membersRemoved;

    /**
     * The collection of reactions added to the conversation.
     */
    @JsonProperty(value = "reactionsAdded")
    private List<MessageReaction> reactionsAdded;

    /**
     * The collection of reactions removed from the conversation.
     */
    @JsonProperty(value = "reactionsRemoved")
    private List<MessageReaction> reactionsRemoved;

    /**
     * The updated topic name of the conversation.
     */
    @JsonProperty(value = "topicName")
    private String topicName;

    /**
     * Indicates whether the prior history of the channel is disclosed.
     */
    @JsonProperty(value = "historyDisclosed")
    private Boolean historyDisclosed;

    /**
     * A locale name for the contents of the text field.
     * The locale name is a combination of an ISO 639 two- or three-letter culture code associated with a language
     * and an ISO 3166 two-letter subculture code associated with a country or region.
     * 
     * The locale name can also correspond to a valid BCP-47 language tag.
     */
    @JsonProperty(value = "locale")
    private String locale;

    /**
     * The text content of the message.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * The text to speak.
     */
    @JsonProperty(value = "speak")
    private String speak;

    /**
     * Indicates whether your bot is accepting, expecting, or ignoring user input after the message 
     * is delivered to the client.
     */
    @JsonProperty(value = "inputHint")
    private InputHints inputHint;

    /**
     * The text to display if the channel cannot render cards.
     */
    @JsonProperty(value = "summary")
    private String summary;

    /**
     * The suggested actions for the activity.
     */
    @JsonProperty(value = "suggestedActions")
    private SuggestedActions suggestedActions;

    /**
     * Attachments.
     */
    @JsonProperty(value = "attachments")
    private List<Attachment> attachments;

    /**
     * Represents the entities that were mentioned in the message.
     */
    @JsonProperty(value = "entities")
    private List<EntityImpl> entities;

    /**
     * Contains channel-specific content.
     */
    @JsonProperty(value = "channelData")
    private Object channelData;

    /**
     * Indicates whether the recipient of a contactRelationUpdate was added or removed from the sender's contact list.
     */
    @JsonProperty(value = "action")
    private String action;

    /**
     * Contains the ID of the message to which this message is a reply.
     */
    @JsonProperty(value = "replyToId")
    private String replyToId;

    /**
     * A descriptive label for the activity.
     */
    @JsonProperty(value = "label")
    private String label;

    /**
     * The type of the activity's value object.
     */
    @JsonProperty(value = "valueType")
    private String valueType;

    /**
     * A value that is associated with the activity.
     */
    @JsonProperty(value = "value")
    private Object value;

    /**
     * The name of the operation associated with an invoke or event activity.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * A reference to another conversation or activity.
     */
    @JsonProperty(value = "relatesTo")
    private ConversationReference relatesTo;

    /**
     * The a code for endOfConversation activities that indicates why the conversation ended.
     */
    @JsonProperty(value = "code")
    private EndOfConversationCodes code;

    /**
     * The time at which the activity should be considered to be expired and should not be presented to the recipient.
     */
    @JsonProperty(value = "expiration")
    private DateTime expiration;

    /**
     * The importance of the activity.
     */
    @JsonProperty(value = "importance")
    private String importance;

    /**
     * A delivery hint to signal to the recipient alternate delivery paths for the activity.
     * 
     * The default delivery mode is \"default\".
     */
    @JsonProperty(value = "deliveryMode")
    private String deliveryMode;

    /**
     * List of phrases and references that speech and language priming systems should listen for.
     */
    @JsonProperty(value = "listenFor")
    private List<String> listenFor;

    /**
     * The collection of text fragments to highlight when the activity contains a ReplyToId value.
     */
    @JsonProperty(value = "textHighlights")
    private List<TextHighlight> textHighlights;

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public ActivityTypes type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the Activity object itself.
     */
    public Activity withType(ActivityTypes type) {
        this.type = type;
        return this;
    }

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the Activity object itself.
     */
    public Activity withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the timestamp value.
     *
     * @return the timestamp value
     */
    public DateTime timestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp value.
     *
     * @param timestamp the timestamp value to set
     * @return the Activity object itself.
     */
    public Activity withTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Get the localTimestamp value.
     *
     * @return the localTimestamp value
     */
    public DateTime localTimestamp() {
        return this.localTimestamp;
    }

    /**
     * Set the localTimestamp value.
     *
     * @param localTimestamp the localTimestamp value to set
     * @return the Activity object itself.
     */
    public Activity withLocalTimestamp(DateTime localTimestamp) {
        this.localTimestamp = localTimestamp;
        return this;
    }

    /**
     * Gets the localTimezone.
     * 
     * @return The name of the local timezone of the message, expressed in IANA Time Zone database format.
     */
    public String localTimezone(){
        return this.localTimezone;
    }

    /**
     * Sets the localTimezone.
     * @param localTimezone The name of the local timezone of the message, expressed in IANA Time Zone database format.
     */
    public Activity withLocalTimeZone(String localTimezone){
        this.localTimezone = localTimezone;
        return this;
    }

    /**
     * Gets the callerId
     */
    public String callerId(){
        return this.callerId;
    }

    /**
     * Sets the callerId
     * 
     * @param callerId A string containing an IRI identifying the caller of a bot.
     */
    public Activity withCallerId(String callerId){
        this.callerId = callerId;
        return this;
    }

    /**
     * Get the serviceUrl value.
     *
     * @return the serviceUrl value
     */
    public String serviceUrl() {
        return this.serviceUrl;
    }

    /**
     * Set the serviceUrl value.
     *
     * @param serviceUrl the serviceUrl value to set
     * @return the Activity object itself.
     */
    public Activity withServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        return this;
    }

    /**
     * Get the channelId value.
     *
     * @return the channelId value
     */
    public String channelId() {
        return this.channelId;
    }

    /**
     * Set the channelId value.
     *
     * @param channelId the channelId value to set
     * @return the Activity object itself.
     */
    public Activity withChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Get the from value.
     *
     * @return the from value
     */
    public ChannelAccount from() {
        return this.from;
    }

    /**
     * Set the from value.
     *
     * @param from the from value to set
     * @return the Activity object itself.
     */
    public Activity withFrom(ChannelAccount from) {
        this.from = from;
        return this;
    }

    /**
     * Get the conversation value.
     *
     * @return the conversation value
     */
    public ConversationAccount conversation() {
        return this.conversation;
    }

    /**
     * Set the conversation value.
     *
     * @param conversation the conversation value to set
     * @return the Activity object itself.
     */
    public Activity withConversation(ConversationAccount conversation) {
        this.conversation = conversation;
        return this;
    }

    /**
     * Get the recipient value.
     *
     * @return the recipient value
     */
    public ChannelAccount recipient() {
        return this.recipient;
    }

    /**
     * Set the recipient value.
     *
     * @param recipient the recipient value to set
     * @return the Activity object itself.
     */
    public Activity withRecipient(ChannelAccount recipient) {
        this.recipient = recipient;
        return this;
    }

    /**
     * Get the textFormat value.
     *
     * @return the textFormat value
     */
    public TextFormatTypes textFormat() {
        return this.textFormat;
    }

    /**
     * Set the textFormat value.
     *
     * @param textFormat the textFormat value to set
     * @return the Activity object itself.
     */
    public Activity withTextFormat(TextFormatTypes textFormat) {
        this.textFormat = textFormat;
        return this;
    }

    /**
     * Get the attachmentLayout value.
     *
     * @return the attachmentLayout value
     */
    public AttachmentLayoutTypes attachmentLayout() {
        return this.attachmentLayout;
    }

    /**
     * Set the attachmentLayout value.
     *
     * @param attachmentLayout the attachmentLayout value to set
     * @return the Activity object itself.
     */
    public Activity withAttachmentLayout(AttachmentLayoutTypes attachmentLayout) {
        this.attachmentLayout = attachmentLayout;
        return this;
    }

    /**
     * Get the membersAdded value.
     *
     * @return the membersAdded value
     */
    public List<ChannelAccount> membersAdded() {
        return this.membersAdded;
    }

    /**
     * Set the membersAdded value.
     *
     * @param membersAdded the membersAdded value to set
     * @return the Activity object itself.
     */
    public Activity withMembersAdded(List<ChannelAccount> membersAdded) {
        this.membersAdded = membersAdded;
        return this;
    }

    /**
     * Get the membersRemoved value.
     *
     * @return the membersRemoved value
     */
    public List<ChannelAccount> membersRemoved() {
        return this.membersRemoved;
    }

    /**
     * Set the membersRemoved value.
     *
     * @param membersRemoved the membersRemoved value to set
     * @return the Activity object itself.
     */
    public Activity withMembersRemoved(List<ChannelAccount> membersRemoved) {
        this.membersRemoved = membersRemoved;
        return this;
    }

    /**
     * Get the reactionsAdded value.
     *
     * @return the reactionsAdded value
     */
    public List<MessageReaction> reactionsAdded() {
        return this.reactionsAdded;
    }

    /**
     * Set the reactionsAdded value.
     *
     * @param reactionsAdded the reactionsAdded value to set
     * @return the Activity object itself.
     */
    public Activity withReactionsAdded(List<MessageReaction> reactionsAdded) {
        this.reactionsAdded = reactionsAdded;
        return this;
    }

    /**
     * Get the reactionsRemoved value.
     *
     * @return the reactionsRemoved value
     */
    public List<MessageReaction> reactionsRemoved() {
        return this.reactionsRemoved;
    }

    /**
     * Set the reactionsRemoved value.
     *
     * @param reactionsRemoved the reactionsRemoved value to set
     * @return the Activity object itself.
     */
    public Activity withReactionsRemoved(List<MessageReaction> reactionsRemoved) {
        this.reactionsRemoved = reactionsRemoved;
        return this;
    }

    /**
     * Get the topicName value.
     *
     * @return the topicName value
     */
    public String topicName() {
        return this.topicName;
    }

    /**
     * Set the topicName value.
     *
     * @param topicName the topicName value to set
     * @return the Activity object itself.
     */
    public Activity withTopicName(String topicName) {
        this.topicName = topicName;
        return this;
    }

    /**
     * Get the historyDisclosed value.
     *
     * @return the historyDisclosed value
     */
    public Boolean historyDisclosed() {
        return this.historyDisclosed;
    }

    /**
     * Set the historyDisclosed value.
     *
     * @param historyDisclosed the historyDisclosed value to set
     * @return the Activity object itself.
     */
    public Activity withHistoryDisclosed(Boolean historyDisclosed) {
        this.historyDisclosed = historyDisclosed;
        return this;
    }

    /**
     * Get the locale value.
     *
     * @return the locale value
     */
    public String locale() {
        return this.locale;
    }

    /**
     * Set the locale value.
     *
     * @param locale the locale value to set
     * @return the Activity object itself.
     */
    public Activity withLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the Activity object itself.
     */
    public Activity withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the speak value.
     *
     * @return the speak value
     */
    public String speak() {
        return this.speak;
    }

    /**
     * Set the speak value.
     *
     * @param speak the speak value to set
     * @return the Activity object itself.
     */
    public Activity withSpeak(String speak) {
        this.speak = speak;
        return this;
    }

    /**
     * Get the inputHint value.
     *
     * @return the inputHint value
     */
    public InputHints inputHint() {
        return this.inputHint;
    }

    /**
     * Set the inputHint value.
     *
     * @param inputHint the inputHint value to set
     * @return the Activity object itself.
     */
    public Activity withInputHint(InputHints inputHint) {
        this.inputHint = inputHint;
        return this;
    }

    /**
     * Get the summary value.
     *
     * @return the summary value
     */
    public String summary() {
        return this.summary;
    }

    /**
     * Set the summary value.
     *
     * @param summary the summary value to set
     * @return the Activity object itself.
     */
    public Activity withSummary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * Get the suggestedActions value.
     *
     * @return the suggestedActions value
     */
    public SuggestedActions suggestedActions() {
        return this.suggestedActions;
    }

    /**
     * Set the suggestedActions value.
     *
     * @param suggestedActions the suggestedActions value to set
     * @return the Activity object itself.
     */
    public Activity withSuggestedActions(SuggestedActions suggestedActions) {
        this.suggestedActions = suggestedActions;
        return this;
    }

    /**
     * Get the attachments value.
     *
     * @return the attachments value
     */
    public List<Attachment> attachments() {
        return this.attachments;
    }

    /**
     * Set the attachments value.
     *
     * @param attachments the attachments value to set
     * @return the Activity object itself.
     */
    public Activity withAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * Get the entities value.
     *
     * @return the entities value
     */
    public List<EntityImpl> entities() {
        return this.entities;
    }

    /**
     * Set the entities value.
     *
     * @param entities the entities value to set
     * @return the Activity object itself.
     */
    public Activity withEntities(List<EntityImpl> entities) {
        this.entities = entities;
        return this;
    }

    /**
     * Get the channelData value.
     *
     * @return the channelData value
     */
    public Object channelData() {
        return this.channelData;
    }

    /**
     * Set the channelData value.
     *
     * @param channelData the channelData value to set
     * @return the Activity object itself.
     */
    public Activity withChannelData(Object channelData) {
        this.channelData = channelData;
        return this;
    }

    /**
     * Get the action value.
     *
     * @return the action value
     */
    public String action() {
        return this.action;
    }

    /**
     * Set the action value.
     *
     * @param action the action value to set
     * @return the Activity object itself.
     */
    public Activity withAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Get the replyToId value.
     *
     * @return the replyToId value
     */
    public String replyToId() {
        return this.replyToId;
    }

    /**
     * Set the replyToId value.
     *
     * @param replyToId the replyToId value to set
     * @return the Activity object itself.
     */
    public Activity withReplyToId(String replyToId) {
        this.replyToId = replyToId;
        return this;
    }

    /**
     * Get the label value.
     *
     * @return the label value
     */
    public String label() {
        return this.label;
    }

    /**
     * Set the label value.
     *
     * @param label the label value to set
     * @return the Activity object itself.
     */
    public Activity withLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Get the valueType value.
     *
     * @return the valueType value
     */
    public String valueType() {
        return this.valueType;
    }

    /**
     * Set the valueType value.
     *
     * @param valueType the valueType value to set
     * @return the Activity object itself.
     */
    public Activity withValueType(String valueType) {
        this.valueType = valueType;
        return this;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public Object value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the Activity object itself.
     */
    public Activity withValue(Object value) {
        this.value = value;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the Activity object itself.
     */
    public Activity withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the relatesTo value.
     *
     * @return the relatesTo value
     */
    public ConversationReference relatesTo() {
        return this.relatesTo;
    }

    /**
     * Set the relatesTo value.
     *
     * @param relatesTo the relatesTo value to set
     * @return the Activity object itself.
     */
    public Activity withRelatesTo(ConversationReference relatesTo) {
        this.relatesTo = relatesTo;
        return this;
    }

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public EndOfConversationCodes code() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     * @return the Activity object itself.
     */
    public Activity withCode(EndOfConversationCodes code) {
        this.code = code;
        return this;
    }

    /**
     * Get the expiration value.
     *
     * @return the expiration value
     */
    public DateTime expiration() {
        return this.expiration;
    }

    /**
     * Set the expiration value.
     *
     * @param expiration the expiration value to set
     * @return the Activity object itself.
     */
    public Activity withExpiration(DateTime expiration) {
        this.expiration = expiration;
        return this;
    }

    /**
     * Get the importance value.
     *
     * @return the importance value
     */
    public String importance() {
        return this.importance;
    }

    /**
     * Set the importance value.
     *
     * @param importance the importance value to set
     * @return the Activity object itself.
     */
    public Activity withImportance(String importance) {
        this.importance = importance;
        return this;
    }

    /**
     * Get the deliveryMode value.
     *
     * @return the deliveryMode value
     */
    public String deliveryMode() {
        return this.deliveryMode;
    }

    /**
     * Set the deliveryMode value.
     *
     * @param deliveryMode the deliveryMode value to set
     * @return the Activity object itself.
     */
    public Activity withDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
        return this;
    }

    /**
     * Gets listenFor value.
     */
    public List<String> listenFor(){
        return this.listenFor;
    }

    /**
     * Sets listenFor value on this object.
     */
    public Activity withListenFor(List<String> listenFor){
        this.listenFor = listenFor;
        return this;
    }

    /**
     * Get the textHighlights value.
     *
     * @return the textHighlights value
     */
    public List<TextHighlight> textHighlights() {
        return this.textHighlights;
    }

    /**
     * Set the textHighlights value.
     *
     * @param textHighlights the textHighlights value to set
     * @return the Activity object itself.
     */
    public Activity withTextHighlights(List<TextHighlight> textHighlights) {
        this.textHighlights = textHighlights;
        return this;
    }
    /**
     * Holds the overflow properties that aren't first class
     * properties in the object.  This allows extensibility
     * while maintaining the object.
     *
     */
    private HashMap<String, JsonNode> properties = new HashMap<String, JsonNode>();

    /**
     * Overflow properties.
     * Properties that are not modelled as first class properties in the object are accessible here.
     * Note: A property value can be be nested.
     *
     * @return A Key-Value map of the properties
     */
    @JsonAnyGetter
    public Map<String, JsonNode> properties() {
        return this.properties;
    }

    /**
     * Set overflow properties.
     *
     * @param key Key for the property
     * @param value JsonNode of value (can be nested)
     *
     */

    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }

    /**
     Updates this activity with the delivery information from an existing
     conversation reference.

     @param reference The conversation reference.
     @param isIncoming (Optional) <c>true</c> to treat the activity as an
     incoming activity, where the bot is the recipient; otherwaire <c>false</c>.
     Default is <c>false</c>, and the activity will show the bot as the sender.
     Call <see cref="GetConversationReference()"/> on an incoming
     activity to get a conversation reference that you can then use to update an
     outgoing activity with the correct delivery information.

     */


    public final Activity applyConversationReference(ConversationReference reference)
    {
        return applyConversationReference(reference, false);
    }

    public final Activity applyConversationReference(ConversationReference reference, boolean isIncoming)
    {
        this.withChannelId(reference.channelId());
        this.withServiceUrl(reference.serviceUrl());
        this.withConversation(reference.conversation());

        if (isIncoming)
        {
            this.withFrom(reference.user());
            this.withRecipient(reference.bot());
            if (reference.activityId() != null)
            {
                this.withId(reference.activityId());
            }
        }
        else // Outgoing
        {
            this.withFrom(reference.bot());
            this.withRecipient(reference.user());
            if (reference.activityId() != null)
            {
                this.withReplyToId(reference.activityId());
            }
        }
        return this;
    }

}
