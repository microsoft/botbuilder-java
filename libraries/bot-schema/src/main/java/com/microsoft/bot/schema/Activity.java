/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Activity class contains all properties that individual, more specific activities
 * could contain. It is a superset type.
 */
public class Activity {
    /**
     * Content-type for an Activity.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    private boolean historyDisclosed;
    /**
     * A locale name for the contents of the text field.
     * The locale name is a combination of an ISO 639 two- or three-letter culture code associated with a language
     * and an ISO 3166 two-letter subculture code associated with a country or region.
     * <p>
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
    private List<Entity> entities;
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
     * <p>
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
     * Holds the overflow properties that aren't first class
     * properties in the object.  This allows extensibility
     * while maintaining the object.
     */
    private HashMap<String, JsonNode> properties = new HashMap<>();

    /**
     * Default constructor.  Normally this wouldn't be used as the ActivityType is normally required.
     */
    protected Activity() {
        setTimestamp(DateTime.now());
    }

    /**
     * Construct an Activity of the specified type.
     * @param withType The activity type.
     */
    public Activity(ActivityTypes withType) {
        this();
        setType(withType);
    }

    /**
     * Create a TRACE type Activity.
     *
     * @param withName      Name of the operation
     * @param withValueType valueType if helpful to identify the value schema (default is value.GetType().Name)
     */
    public static Activity createTraceActivity(String withName, Object withValue, String withValueType, String withLabel) {
        return new Activity(ActivityTypes.TRACE) {{
            setName(withName);
            setLabel(withLabel);
            setValueType((withValueType == null) ? withValue.getClass().getTypeName() : withValueType);
            setValue(withValue);
        }};
    }

    /**
     * Create a MESSAGE type Activity.
     * @return A message Activity type.
     */
    public static Activity createMessageActivity() {
        return new Activity(ActivityTypes.MESSAGE) {{
            setAttachments(new ArrayList<>());
            setEntities(new ArrayList<>());
        }};
    }

    /**
     * Create a CONTACT_RELATION_UPDATE type Activity.
     * @return A contact relation update type Activity.
     */
    public static Activity createContactRelationUpdateActivity() {
        return new Activity(ActivityTypes.CONTACT_RELATION_UPDATE);
    }

    /**
     * Create a CONVERSATION_UPDATE type Activity.
     * @return A conversation update type Activity.
     */
    public static Activity createConversationUpdateActivity() {
        return new Activity(ActivityTypes.CONVERSATION_UPDATE) {{
            setMembersAdded(new ArrayList<>());
            setMembersRemoved(new ArrayList<>());
        }};
    }

    /**
     * Creates a TYPING type Activity.
     * @return The new typing activity.
     */
    public static Activity createTypingActivity() {
        return new Activity(ActivityTypes.TYPING);
    }

    /**
     * Creates a HANDOFF type Activity.
     * @return The new handoff activity.
     */
    public static Activity createHandoffActivity() {
        return new Activity(ActivityTypes.HANDOFF);
    }

    /**
     * Creates a END_OF_CONVERSATION type of Activity.
     * @return The new end of conversation activity.
     */
    public static Activity createEndOfConversationActivity() {
        return new Activity(ActivityTypes.END_OF_CONVERSATION);
    }

    /**
     * Creates a EVENT type of Activity.
     * @return The new event activity.
     */
    public static Activity createEventActivity() {
        return new Activity(ActivityTypes.EVENT);
    }

    /**
     * Creates a INVOKE type of Activity.
     * @return The new invoke activity.
     */
    public static Activity createInvokeActivity() {
        return new Activity(ActivityTypes.INVOKE);
    }

    /**
     * Clone a activity.
     *
     * @param activity The activity to clone.
     * @return new cloned activity
     */
    public static Activity cloneActivity(Activity activity) {
        //TODO: This isn't a deep copy
        Activity clone = new Activity(activity.getType()) {{
            setId(activity.getId());
            setTimestamp(activity.getTimestamp());
            setLocalTimestamp(activity.getLocalTimestamp());
            setChannelData(activity.getChannelData());
            setFrom(activity.getFrom());
            setRecipient(activity.getRecipient());
            setConversation(activity.getConversation());
            setChannelId(activity.getChannelId());
            setServiceUrl(activity.getServiceUrl());
            setChannelId(activity.getChannelId());
            setEntities(activity.getEntities());
            setReplyToId(activity.getReplyToId());
            setSpeak(activity.getSpeak());
            setText(activity.getText());
            setInputHint(activity.getInputHint());
            setSummary(activity.getSummary());
            setSuggestedActions(activity.getSuggestedActions());
            setAttachments(activity.getAttachments());
            setAction(activity.getAction());
            setLabel(activity.getLabel());
            setValueType(activity.getValueType());
            setValue(activity.getValue());
            setName(activity.getName());
            setRelatesTo(activity.getRelatesTo());
            setCode(activity.getCode());
            setExpiration(activity.getExpiration());
            setImportance(activity.getImportance());
            setDeliveryMode(activity.getDeliveryMode());
            setTextHighlights(activity.getTextHighlights());
        }};

        for (Map.Entry<String, JsonNode> entry : activity.getProperties().entrySet()) {
            clone.setProperties(entry.getKey(), entry.getValue());
        }

        return clone;
    }

    /**
     * @see #type
     */
    public ActivityTypes getType() {
        return this.type;
    }

    /**
     * @see #type
     */
    public void setType(ActivityTypes withType) {
        this.type = withType;
    }

    /**
     * @see #id
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see #id
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * @see #timestamp
     */
    public DateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * @see #timestamp
     */
    public void setTimestamp(DateTime withTimestamp) {
        this.timestamp = withTimestamp;
    }

    /**
     * @see #localTimestamp
     */
    public DateTime getLocalTimestamp() {
        return this.localTimestamp;
    }

    /**
     * @see #localTimestamp
     */
    public void setLocalTimestamp(DateTime withLocalTimestamp) {
        this.localTimestamp = withLocalTimestamp;
    }

    /**
     * @see #localTimezone
     */
    public String getLocalTimezone() {
        return this.localTimezone;
    }

    /**
     * @see #localTimezone
     */
    public void setLocalTimeZone(String withLocalTimezone) {
        this.localTimezone = withLocalTimezone;
    }

    /**
     * @see #callerId
     */
    public String getCallerId() {
        return this.callerId;
    }

    /**
     * @see #callerId
     */
    public void setCallerId(String withCallerId) {
        this.callerId = withCallerId;
    }

    /**
     * @see #serviceUrl
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * @see #serviceUrl
     */
    public void setServiceUrl(String withServiceUrl) {
        this.serviceUrl = withServiceUrl;
    }

    /**
     * @see #channelId
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * @see #channelId
     */
    public void setChannelId(String withChannelId) {
        this.channelId = withChannelId;
    }

    /**
     * @see #from
     */
    public ChannelAccount getFrom() {
        return this.from;
    }

    /**
     * @see #from
     */
    public void setFrom(ChannelAccount withFrom) {
        this.from = withFrom;
    }

    /**
     * @see #conversation
     */
    public ConversationAccount getConversation() {
        return this.conversation;
    }

    /**
     * @see #conversation
     */
    public void setConversation(ConversationAccount withConversation) {
        this.conversation = withConversation;
    }

    /**
     * @see #recipient
     */
    public ChannelAccount getRecipient() {
        return this.recipient;
    }

    /**
     * @see #recipient
     */
    public void setRecipient(ChannelAccount withRecipient) {
        this.recipient = withRecipient;
    }

    /**
     * @see #textFormat
     */
    public TextFormatTypes getTextFormat() {
        return this.textFormat;
    }

    /**
     * @see #textFormat
     */
    public void setTextFormat(TextFormatTypes withTextFormat) {
        this.textFormat = withTextFormat;
    }

    /**
     * @see #attachmentLayout
     */
    public AttachmentLayoutTypes getAttachmentLayout() {
        return this.attachmentLayout;
    }

    /**
     * @see #attachmentLayout
     */
    public void setAttachmentLayout(AttachmentLayoutTypes withAttachmentLayout) {
        this.attachmentLayout = withAttachmentLayout;
    }

    /**
     * @see #reactionsAdded
     */
    public List<MessageReaction> getReactionsAdded() {
        return this.reactionsAdded;
    }

    /**
     * @see #reactionsAdded
     */
    public void setReactionsAdded(List<MessageReaction> withReactionsAdded) {
        this.reactionsAdded = withReactionsAdded;
    }

    /**
     * @see #reactionsRemoved
     */
    public List<MessageReaction> getReactionsRemoved() {
        return this.reactionsRemoved;
    }

    /**
     * @see #reactionsRemoved
     */
    public void setReactionsRemoved(List<MessageReaction> withReactionsRemoved) {
        this.reactionsRemoved = withReactionsRemoved;
    }

    /**
     * @see #locale
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * @see #locale
     */
    public void setLocale(String withLocale) {
        this.locale = withLocale;
    }

    /**
     * @see #text
     */
    public String getText() {
        return this.text;
    }

    /**
     * @see #text
     */
    public void setText(String withText) {
        this.text = withText;
    }

    /**
     * @see #speak
     */
    public String getSpeak() {
        return this.speak;
    }

    /**
     * @see #speak
     */
    public void setSpeak(String withSpeak) {
        this.speak = withSpeak;
    }

    /**
     * @see #inputHint
     */
    public InputHints getInputHint() {
        return this.inputHint;
    }

    /**
     * @see #inputHint
     */
    public void setInputHint(InputHints withInputHint) {
        this.inputHint = withInputHint;
    }

    /**
     * @see #summary
     */
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see #summary
     */
    public void setSummary(String withSummary) {
        this.summary = withSummary;
    }

    /**
     * @see #suggestedActions
     */
    public SuggestedActions getSuggestedActions() {
        return this.suggestedActions;
    }

    /**
     * @see #suggestedActions
     */
    public void setSuggestedActions(SuggestedActions withSuggestedActions) {
        this.suggestedActions = withSuggestedActions;
    }

    /**
     * @see #attachments
     */
    public List<Attachment> getAttachments() {
        return this.attachments;
    }

    /**
     * @see #attachments
     */
    public void setAttachments(List<Attachment> withAttachments) {
        this.attachments = withAttachments;
    }

    /**
     * @see #entities
     */
    public List<Entity> getEntities() {
        return this.entities;
    }

    /**
     * @see #entities
     */
    public void setEntities(List<Entity> withEntities) {
        this.entities = withEntities;
    }

    /**
     * @see #channelData
     */
    public Object getChannelData() {
        return this.channelData;
    }

    /**
     * @see #channelData
     */
    public void setChannelData(Object withChannelData) {
        this.channelData = withChannelData;
    }

    /**
     * @see #replyToId
     */
    public String getReplyToId() {
        return this.replyToId;
    }

    /**
     * @see #replyToId
     */
    public void setReplyToId(String withReplyToId) {
        this.replyToId = withReplyToId;
    }

    /**
     * @see #code
     */
    public EndOfConversationCodes getCode() {
        return this.code;
    }

    /**
     * @see #code
     */
    public void setCode(EndOfConversationCodes withCode) {
        this.code = withCode;
    }

    /**
     * @see #expiration
     */
    public DateTime getExpiration() {
        return this.expiration;
    }

    /**
     * @see #expiration
     */
    public void setExpiration(DateTime withExpiration) {
        this.expiration = withExpiration;
    }

    /**
     * @see #importance
     */
    public String getImportance() {
        return this.importance;
    }

    /**
     * @see #importance
     */
    public void setImportance(String withImportance) {
        this.importance = withImportance;
    }

    /**
     * @see #deliveryMode
     */
    public String getDeliveryMode() {
        return this.deliveryMode;
    }

    /**
     * @see #deliveryMode
     */
    public void setDeliveryMode(String withDeliveryMode) {
        this.deliveryMode = withDeliveryMode;
    }

    /**
     * @see #listenFor
     */
    public List<String> getListenFor() {
        return this.listenFor;
    }

    /**
     * @see #listenFor
     */
    public void setListenFor(List<String> withListenFor) {
        this.listenFor = withListenFor;
    }

    /**
     * @see #textHighlights
     */
    public List<TextHighlight> getTextHighlights() {
        return this.textHighlights;
    }

    /**
     * @see #textHighlights
     */
    public void setTextHighlights(List<TextHighlight> withTextHighlights) {
        this.textHighlights = withTextHighlights;
    }

    /**
     * @see #properties
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * @see #properties
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode value) {
        this.properties.put(key, value);
    }

    /**
     * @see #topicName
     */
    public String getTopicName() {
        return this.topicName;
    }

    /**
     * @see #topicName
     */
    public void setTopicName(String withTopicName) {
        this.topicName = withTopicName;
    }

    /**
     * @see #historyDisclosed
     */
    public boolean getHistoryDisclosed() {
        return this.historyDisclosed;
    }

    /**
     * @see #historyDisclosed
     */
    public void setHistoryDisclosed(boolean withHistoryDisclosed) {
        this.historyDisclosed = withHistoryDisclosed;
    }

    /**
     * @see #membersAdded
     */
    public List<ChannelAccount> getMembersAdded() {
        return this.membersAdded;
    }

    /**
     * @see #membersAdded
     */
    public void setMembersAdded(List<ChannelAccount> withMembersAdded) {
        this.membersAdded = withMembersAdded;
    }

    /**
     * @see #membersRemoved
     */
    public List<ChannelAccount> getMembersRemoved() {
        return this.membersRemoved;
    }

    /**
     * @see #membersRemoved
     */
    public void setMembersRemoved(List<ChannelAccount> withMembersRemoved) {
        this.membersRemoved = withMembersRemoved;
    }

    /**
     * @see #label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @see #label
     */
    public void setLabel(String withLabel) {
        this.label = withLabel;
    }

    /**
     * @see #valueType
     */
    public String getValueType() {
        return this.valueType;
    }

    /**
     * @see #valueType
     */
    public void setValueType(String withValueType) {
        this.valueType = withValueType;
    }

    /**
     * @see #value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * @see #value
     */
    public void setValue(Object withValue) {
        this.value = withValue;
    }

    /**
     * @see #name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see #name
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * @see #relatesTo
     */
    public ConversationReference getRelatesTo() {
        return this.relatesTo;
    }

    /**
     * @see #relatesTo
     */
    public void setRelatesTo(ConversationReference withRelatesTo) {
        this.relatesTo = withRelatesTo;
    }

    /**
     * @see #action
     */
    public String getAction() {
        return this.action;
    }

    /**
     * @see #action
     */
    public void setAction(String withAction) {
        this.action = withAction;
    }

    public Activity createTrace(String withName, Object withValue, String withValueType, String withLabel) {
        Activity reply = new Activity(ActivityTypes.TRACE);

        reply.setName(withName);
        reply.setLabel(withLabel);
        reply.setValueType((withValueType == null) ? withValue.getClass().getTypeName() : withValueType);
        reply.setValue(withValue);

        reply.setFrom(new ChannelAccount(this.getRecipient().getId(), this.getRecipient().getName()));
        reply.setRecipient(new ChannelAccount(this.getFrom().getId(), this.getFrom().getName()));
        reply.setReplyToId(this.getId());
        reply.setServiceUrl(this.getServiceUrl());
        reply.setChannelId(this.getChannelId());
        reply.setConversation(new ConversationAccount(
            this.getConversation().isGroup(),
            this.getConversation().getId(),
            this.getConversation().getName()));

        return reply;
    }

    public Activity createReply(String withText) {
        return createReply(withText, null);
    }

    /**
     * Creates a new message activity as a response to this activity.
     *
     * @param withText   The text of the reply.
     * @param withLocale The language code for the text.
     * @return The new message activity.
     */
    public Activity createReply(String withText, String withLocale) {
        Activity result = new Activity(ActivityTypes.MESSAGE);

        result.setText((withText == null) ? "" : withText);
        result.setLocale((withLocale == null) ? this.getLocale() : withLocale);
        result.setFrom(new ChannelAccount(
            this.getRecipient().getId(),
            this.getRecipient().getName()));
        result.setRecipient(new ChannelAccount(
            this.getFrom().getId(),
            this.getFrom().getName()));
        result.setReplyToId(this.getId());
        result.setServiceUrl(this.getServiceUrl());
        result.setChannelId(this.getChannelId());
        result.setConversation(new ConversationAccount(
            this.getConversation().isGroup(),
            this.getConversation().getId(),
            this.getConversation().getName()));
        result.setAttachments(new ArrayList<>());
        result.setEntities(new ArrayList<>());

        return result;
    }

    /**
     * Checks if this (message) activity has content.
     *
     * @return Returns true, if this message has any content to send. False otherwise.
     */
    public boolean hasContent() {
        if (!StringUtils.isBlank(this.getText()))
            return true;

        if (!StringUtils.isBlank(this.getSummary()))
            return true;

        if (this.getAttachments() != null && this.getAttachments().size() > 0)
            return true;

        return this.getChannelData() != null;
    }

    /**
     * Resolves the mentions from the entities of this activity.
     *
     * This method is defined on the <see cref="Activity"/> class, but is only intended for use with a
     * message activity, where the activity {@link Activity#type} is set to {@link ActivityTypes#MESSAGE}.
     *
     * @return The array of mentions; or an empty array, if none are found.
     */
    public List<Mention> getMentions() {
        return this.getEntities().stream()
            .filter(entity -> entity.getType().equalsIgnoreCase("mention"))
            .map(entity -> entity.getAs(Mention.class))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get channelData as typed structure
     *
     * @param classType type of TypeT to use
     * @return typed Object or default(TypeT)
     */
    public <TypeT> TypeT getChannelData(Class<TypeT> classType) throws JsonProcessingException {
        if (this.getChannelData() == null)
            return null;

        if (classType.isInstance(this.getChannelData())) {
            return (TypeT) this.getChannelData();
        }
        JsonNode node = MAPPER.valueToTree(this.getChannelData());
        return MAPPER.treeToValue(node, classType);
    }

    /**
     * Get channelData as typed structure
     *
     * @param clsType type of TypeT to use
     * @return
     */
    public <TypeT> ResultPair<Boolean, TypeT> tryGetChannelData(Class<TypeT> clsType) {
        TypeT instance = null;
        if (this.getChannelData() == null)
            return new ResultPair<>(false, instance);

        try {
            instance = this.getChannelData(clsType);
        } catch (JsonProcessingException e) {
            return new ResultPair<Boolean, TypeT>(false, instance);
        }
        return new ResultPair<Boolean, TypeT>(true, instance);
    }

    /**
     * True if the Activity is of the specified activity type
     */
    protected boolean isActivity(String activityType) {
        /*
         * NOTE: While it is possible to come up with a fancy looking "one-liner" to solve
         * this problem, this code is purposefully more verbose due to optimizations.
         *
         * This main goal of the optimizations was to make zero allocations because it is called
         * by all of the .AsXXXActivity methods which are used in a pattern heavily upstream to
         * "pseudo-cast" the activity based on its type.
         */

        ActivityTypes type = this.getType();

        // If there's no type set then we can't tell if it's the type they're looking for
        if (type == null) {
            return false;
        }

        // Check if the full type value starts with the type they're looking for


        boolean result = StringUtils.startsWith(type.toString().toLowerCase(), activityType.toLowerCase());

        // If the full type value starts with the type they're looking for, then we need to check a little further to check if it's definitely the right type
        if (result) {
            // If the lengths are equal, then it's the exact type they're looking for
            result = type.toString().length() == activityType.length();

            if (!result) {
                // Finally, if the type is longer than the type they're looking for then we need to check if there's a / separator right after the type they're looking for
                result = type.toString().length() > activityType.length()
                    &&
                    type.toString().indexOf(activityType.length()) == '/';
            }
        }

        return result;
    }

    public final Activity applyConversationReference(ConversationReference reference) {
        return applyConversationReference(reference, false);
    }

    public final Activity applyConversationReference(ConversationReference reference, boolean isIncoming) {
        this.setChannelId(reference.getChannelId());
        this.setServiceUrl(reference.getServiceUrl());
        this.setConversation(reference.getConversation());

        if (isIncoming) {
            this.setFrom(reference.getUser());
            this.setRecipient(reference.getBot());
            if (reference.getActivityId() != null) {
                this.setId(reference.getActivityId());
            }
        } else // Outgoing
        {
            this.setFrom(reference.getBot());
            this.setRecipient(reference.getUser());
            if (reference.getActivityId() != null) {
                this.setReplyToId(reference.getActivityId());
            }
        }
        return this;
    }
}
