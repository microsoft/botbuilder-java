// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.microsoft.bot.schema.teams.NotificationInfo;
import com.microsoft.bot.schema.teams.TeamInfo;
import com.microsoft.bot.schema.teams.TeamsChannelData;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.schema.teams.TeamsMeetingInfo;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Activity class contains all properties that individual, more specific
 * activities could contain. It is a superset type.
 */
public class Activity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    @JsonProperty(value = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String id;

    @JsonProperty(value = "timestamp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.nXXX", timezone = "UTC")
    private OffsetDateTime timestamp;

    @JsonProperty(value = "localTimestamp")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private OffsetDateTime localTimestamp;

    @JsonProperty(value = "localTimezone")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String localTimezone;

    @JsonProperty(value = "callerId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String callerId;

    @JsonProperty(value = "serviceUrl")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String serviceUrl;

    @JsonProperty(value = "channelId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String channelId;

    @JsonProperty(value = "from")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ChannelAccount from;

    @JsonProperty(value = "conversation")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ConversationAccount conversation;

    @JsonProperty(value = "recipient")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ChannelAccount recipient;

    @JsonProperty(value = "textFormat")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private TextFormatTypes textFormat;

    @JsonProperty(value = "attachmentLayout")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private AttachmentLayoutTypes attachmentLayout;

    @JsonProperty(value = "membersAdded")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ChannelAccount> membersAdded;

    @JsonProperty(value = "membersRemoved")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ChannelAccount> membersRemoved;

    @JsonProperty(value = "reactionsAdded")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MessageReaction> reactionsAdded;

    @JsonProperty(value = "reactionsRemoved")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MessageReaction> reactionsRemoved;

    @JsonProperty(value = "topicName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String topicName;

    @JsonProperty(value = "historyDisclosed")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private boolean historyDisclosed;

    @JsonProperty(value = "locale")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String locale;

    @JsonProperty(value = "text")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String text;

    @JsonProperty(value = "speak")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String speak;

    @JsonProperty(value = "inputHint")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private InputHints inputHint;

    @JsonProperty(value = "summary")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String summary;

    @JsonProperty(value = "suggestedActions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private SuggestedActions suggestedActions;

    @JsonProperty(value = "attachments")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Attachment> attachments;

    @JsonProperty(value = "entities")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Entity> entities;

    @JsonProperty(value = "channelData")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object channelData;

    @JsonProperty(value = "action")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String action;

    @JsonProperty(value = "replyToId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String replyToId;

    @JsonProperty(value = "label")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String label;

    @JsonProperty(value = "valueType")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String valueType;

    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object value;

    @JsonProperty(value = "name")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonProperty(value = "relatesTo")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ConversationReference relatesTo;

    @JsonProperty(value = "code")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EndOfConversationCodes code;

    @JsonProperty(value = "expiration")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private LocalDateTime expiration;

    @JsonProperty(value = "importance")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String importance;

    @JsonProperty(value = "deliveryMode")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String deliveryMode;

    @JsonProperty(value = "listenFor")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> listenFor;

    @JsonProperty(value = "textHighlights")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TextHighlight> textHighlights;

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     */
    private HashMap<String, JsonNode> properties = new HashMap<>();

    /**
     * Default constructor. Normally this wouldn't be used as the ActivityType is
     * normally required.
     */
    protected Activity() {
        final Clock clock = new NanoClockHelper();
        setTimestamp(OffsetDateTime.now(clock));
    }

    /**
     * Construct an Activity of the specified type.
     *
     * @param withType The activity type.
     */
    public Activity(String withType) {
        this();
        setType(withType);
    }

    /**
     * Create a TRACE type Activity.
     *
     * @param withName Name of the operation
     * @return A Trace type Activity.
     */
    public static Activity createTraceActivity(String withName) {
        return createTraceActivity(withName, null, null, null);
    }

    /**
     * Create a TRACE type Activity.
     *
     * @param withName      Name of the operation
     * @param withValueType valueType if helpful to identify the value schema
     *                      (default is value.GetType().Name)
     * @param withValue     The content for this trace operation.
     * @param withLabel     A descriptive label for this trace operation.
     * @return A Trace type Activity.
     */
    public static Activity createTraceActivity(
        String withName,
        String withValueType,
        Object withValue,
        String withLabel
    ) {
        Activity activity = new Activity(ActivityTypes.TRACE);
        activity.setName(withName);
        activity.setLabel(withLabel);
        if (withValue != null) {
            activity.setValueType((withValueType == null) ? withValue.getClass().getTypeName() : withValueType);
        } else {
            activity.setValueType(withValueType);
        }
        activity.setValue(withValue);
        return activity;
    }

    /**
     * Create a MESSAGE type Activity.
     *
     * @return A message Activity type.
     */
    public static Activity createMessageActivity() {
        Activity activity = new Activity(ActivityTypes.MESSAGE);
        activity.setAttachments(new ArrayList<>());
        activity.setEntities(new ArrayList<>());
        return activity;
    }

    /**
     * Create a CONTACT_RELATION_UPDATE type Activity.
     *
     * @return A contact relation update type Activity.
     */
    public static Activity createContactRelationUpdateActivity() {
        return new Activity(ActivityTypes.CONTACT_RELATION_UPDATE);
    }

    /**
     * Create a CONVERSATION_UPDATE type Activity.
     *
     * @return A conversation update type Activity.
     */
    public static Activity createConversationUpdateActivity() {
        Activity activity = new Activity(ActivityTypes.CONVERSATION_UPDATE);
        activity.setMembersAdded(new ArrayList<>());
        activity.setMembersRemoved(new ArrayList<>());
        return activity;
    }

    /**
     * Creates a TYPING type Activity.
     *
     * @return The new typing activity.
     */
    public static Activity createTypingActivity() {
        return new Activity(ActivityTypes.TYPING);
    }

    /**
     * Creates a HANDOFF type Activity.
     *
     * @return The new handoff activity.
     */
    public static Activity createHandoffActivity() {
        return new Activity(ActivityTypes.HANDOFF);
    }

    /**
     * Creates a END_OF_CONVERSATION type of Activity.
     *
     * @return The new end of conversation activity.
     */
    public static Activity createEndOfConversationActivity() {
        return new Activity(ActivityTypes.END_OF_CONVERSATION);
    }

    /**
     * Creates a EVENT type of Activity.
     *
     * @return The new event activity.
     */
    public static Activity createEventActivity() {
        return new Activity(ActivityTypes.EVENT);
    }

    /**
     * Creates a INVOKE type of Activity.
     *
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
    public static Activity clone(Activity activity) {
        Activity cloned = new Activity(activity.getType());
        cloned.setId(activity.getId());
        cloned.setTimestamp(activity.getTimestamp());
        cloned.setLocalTimestamp(activity.getLocalTimestamp());
        cloned.setLocalTimeZone(activity.getLocalTimezone());
        cloned.setChannelData(activity.getChannelData());
        cloned.setFrom(ChannelAccount.clone(activity.getFrom()));
        cloned.setRecipient(ChannelAccount.clone(activity.getRecipient()));
        cloned.setConversation(ConversationAccount.clone(activity.getConversation()));
        cloned.setChannelId(activity.getChannelId());
        cloned.setServiceUrl(activity.getServiceUrl());
        cloned.setChannelId(activity.getChannelId());
        cloned.setEntities(Entity.cloneList(activity.getEntities()));
        cloned.setReplyToId(activity.getReplyToId());
        cloned.setSpeak(activity.getSpeak());
        cloned.setText(activity.getText());
        cloned.setInputHint(activity.getInputHint());
        cloned.setSummary(activity.getSummary());
        cloned.setSuggestedActions(SuggestedActions.clone(activity.getSuggestedActions()));
        cloned.setAttachments(Attachment.cloneList(activity.getAttachments()));
        cloned.setAction(activity.getAction());
        cloned.setLabel(activity.getLabel());
        cloned.setValueType(activity.getValueType());
        cloned.setValue(activity.getValue());
        cloned.setName(activity.getName());
        cloned.setRelatesTo(ConversationReference.clone(activity.getRelatesTo()));
        cloned.setCode(activity.getCode());
        cloned.setExpiration(activity.getExpiration());
        cloned.setImportance(activity.getImportance());
        cloned.setDeliveryMode(activity.getDeliveryMode());
        cloned.setTextHighlights(activity.getTextHighlights());
        cloned.setCallerId(activity.getCallerId());
        cloned.setHistoryDisclosed(activity.getHistoryDisclosed());
        cloned.setLocale(activity.getLocale());
        cloned.setReactionsAdded(MessageReaction.cloneList(activity.getReactionsAdded()));
        cloned.setReactionsRemoved(MessageReaction.cloneList(activity.getReactionsRemoved()));
        cloned.setExpiration(activity.getExpiration());
        cloned.setMembersAdded(ChannelAccount.cloneList(activity.getMembersAdded()));
        cloned.setMembersRemoved(ChannelAccount.cloneList(activity.getMembersRemoved()));
        cloned.setTextFormat(activity.getTextFormat());
        cloned.setAttachmentLayout(activity.getAttachmentLayout());
        cloned.setTopicName(activity.getTopicName());
        if (activity.getListenFor() != null) {
            cloned.setListenFor(new ArrayList<>(activity.getListenFor()));
        }
        for (Map.Entry<String, JsonNode> entry : activity.getProperties().entrySet()) {
            cloned.setProperties(entry.getKey(), entry.getValue());
        }

        if (cloned.getId() == null) {
            cloned.setId(String.format("g_%s", UUID.randomUUID().toString()));
        }

        return cloned;
    }

    /**
     * Gets the {@link ActivityTypes} of the activity.
     *
     * @return The Activity type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the {@link ActivityTypes} of the activity.
     *
     * @param withType The type of the Activity.
     */
    public void setType(String withType) {
        this.type = withType;
    }

    /**
     * Convenience method to return if the Activity is of the specified type.
     *
     * @param compareTo The type to compare to.
     * @return True if the Activity is of the specified type.
     */
    public boolean isType(String compareTo) {
        return StringUtils.equals(type, compareTo);
    }

    /**
     * Returns the ID that uniquely identifies the activity on the channel.
     *
     * @return The activity id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the ID that uniquely identifies the activity on the channel.
     *
     * @param withId The activity ID.
     */
    public void setId(String withId) {
        this.id = withId;
    }

    /**
     * Gets the date and time that the message was sent, in UTC, expressed in
     * ISO-8601 format.
     *
     * @return The UTC timestamp of the activity.
     */
    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the date and time that the message was sent, in UTC, expressed in
     * ISO-8601 format.
     *
     * @param withTimestamp The UTC timestamp of the activity.
     */
    public void setTimestamp(OffsetDateTime withTimestamp) {
        this.timestamp = withTimestamp;
    }

    /**
     * Gets the local date and time of the message, expressed in ISO-8601 format.
     * For example, 2016-09-23T13:07:49.4714686-07:00.
     *
     * @return The local timestamp of the activity.
     */
    public OffsetDateTime getLocalTimestamp() {
        return this.localTimestamp;
    }

    /**
     * Contains the local date and time of the message, expressed in ISO-8601
     * format. For example, 2016-09-23T13:07:49.4714686-07:00.
     *
     * @param withLocalTimestamp The local timestamp of the activity.
     */
    public void setLocalTimestamp(OffsetDateTime withLocalTimestamp) {
        this.localTimestamp = withLocalTimestamp;
    }

    /**
     * Gets the name of the local timezone of the message, expressed in IANA Time
     * Zone database format. For example, America/Los_Angeles.
     *
     * @return The local timezone.
     */
    public String getLocalTimezone() {
        return this.localTimezone;
    }

    /**
     * Sets the name of the local timezone of the message, expressed in IANA Time
     * Zone database format. For example, America/Los_Angeles.
     *
     * @param withLocalTimezone The local timezone.
     */
    public void setLocalTimeZone(String withLocalTimezone) {
        this.localTimezone = withLocalTimezone;
    }

    /**
     * Gets a string containing an IRI identifying the caller of a bot. This field
     * is not intended to be transmitted over the wire, but is instead populated by
     * bots and clients based on cryptographically verifiable data that asserts the
     * identity of the callers (e.g. tokens).
     *
     * @return The caller IRI.
     */
    public String getCallerId() {
        return this.callerId;
    }

    /**
     * Sets the IRI identifying the caller of a bot. This field is not intended to
     * be transmitted over the wire, but is instead populated by bots and clients
     * based on cryptographically verifiable data that asserts the identity of the
     * callers (e.g. tokens).
     *
     * @param withCallerId The caller id.
     */
    public void setCallerId(String withCallerId) {
        this.callerId = withCallerId;
    }

    /**
     * Sets the URL that specifies the channel's service endpoint. Set by the
     * channel.
     *
     * @return The service URL.
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * Sets the URL that specifies the channel's service endpoint. Set by the
     * channel.
     *
     * @param withServiceUrl The service URL of the Activity.
     */
    public void setServiceUrl(String withServiceUrl) {
        this.serviceUrl = withServiceUrl;
    }

    /**
     * Gets the ID that uniquely identifies the channel. Set by the channel.
     *
     * @return The channel ID.
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * Sets the ID that uniquely identifies the channel. Set by the channel.
     *
     * @param withChannelId The channel ID.
     */
    public void setChannelId(String withChannelId) {
        this.channelId = withChannelId;
    }

    /**
     * Identifies the sender of the message.
     *
     * @return The {@link ChannelAccount} of the sender.
     */
    public ChannelAccount getFrom() {
        return this.from;
    }

    /**
     * Identifies the sender of the message.
     *
     * @param withFrom The {@link ChannelAccount} of the sender.
     */
    public void setFrom(ChannelAccount withFrom) {
        this.from = withFrom;
    }

    /**
     * Identifies the conversation to which the activity belongs.
     *
     * @return The {@link ConversationAccount}.
     */
    public ConversationAccount getConversation() {
        return this.conversation;
    }

    /**
     * Identifies the conversation to which the activity belongs.
     *
     * @param withConversation The {@link ConversationAccount}.
     */
    public void setConversation(ConversationAccount withConversation) {
        this.conversation = withConversation;
    }

    /**
     * Identifies the recipient of the message.
     *
     * @return The {@link ChannelAccount} of the recipient.
     */
    public ChannelAccount getRecipient() {
        return this.recipient;
    }

    /**
     * Identifies the recipient of the message.
     *
     * @param withRecipient The {@link ChannelAccount} of the recipient.
     */
    public void setRecipient(ChannelAccount withRecipient) {
        this.recipient = withRecipient;
    }

    /**
     * Format of text fields Default:markdown. Possible values include: 'markdown',
     * 'plain', 'xml'.
     *
     * @return The TextFormatTypes type.
     */
    public TextFormatTypes getTextFormat() {
        return this.textFormat;
    }

    /**
     * Format of text fields.
     *
     * @param withTextFormat The TextFormatTypes type.
     */
    public void setTextFormat(TextFormatTypes withTextFormat) {
        this.textFormat = withTextFormat;
    }

    /**
     * The layout hint for multiple attachments.
     *
     * @return The Attachment type.
     */
    public AttachmentLayoutTypes getAttachmentLayout() {
        return this.attachmentLayout;
    }

    /**
     * Sets the layout hint for multiple attachments.
     *
     * @param withAttachmentLayout The attachment type.
     */
    public void setAttachmentLayout(AttachmentLayoutTypes withAttachmentLayout) {
        this.attachmentLayout = withAttachmentLayout;
    }

    /**
     * Gets the collection of reactions added to the conversation.
     *
     * @return A List of {@link MessageReaction}.
     */
    public List<MessageReaction> getReactionsAdded() {
        return this.reactionsAdded;
    }

    /**
     * Sets the collection of reactions added to the conversation.
     *
     * @param withReactionsAdded A List of {@link MessageReaction}.
     */
    public void setReactionsAdded(List<MessageReaction> withReactionsAdded) {
        this.reactionsAdded = withReactionsAdded;
    }

    /**
     * Gets the collection of reactions removed from the conversation.
     *
     * @return A List of {@link MessageReaction}.
     */
    public List<MessageReaction> getReactionsRemoved() {
        return this.reactionsRemoved;
    }

    /**
     * Sets the collection of reactions removed from the conversation.
     *
     * @param withReactionsRemoved A List of {@link MessageReaction}.
     */
    public void setReactionsRemoved(List<MessageReaction> withReactionsRemoved) {
        this.reactionsRemoved = withReactionsRemoved;
    }

    /**
     * A locale name for the contents of the text field. The locale name is a
     * combination of an ISO 639 two- or three-letter culture code associated with a
     * language and an ISO 3166 two-letter subculture code associated with a country
     * or region.
     * <p>
     * The locale name can also correspond to a valid BCP-47 language tag.
     *
     * @return The content locale.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * A locale name for the contents of the text field. The locale name is a
     * combination of an ISO 639 two- or three-letter culture code associated with a
     * language and an ISO 3166 two-letter subculture code associated with a country
     * or region.
     * <p>
     * The locale name can also correspond to a valid BCP-47 language tag.
     *
     * @param withLocale The content locale.
     */
    public void setLocale(String withLocale) {
        this.locale = withLocale;
    }

    /**
     * Gets the text content of the message.
     *
     * @return The text content.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text content of the message.
     *
     * @param withText The text content.
     */
    public void setText(String withText) {
        this.text = withText;
    }

    /**
     * The text to speak.
     *
     * @return The SSML text to speak.
     */
    public String getSpeak() {
        return this.speak;
    }

    /**
     * Sets the text to speak.
     *
     * @param withSpeak The SSML text to speak.
     */
    public void setSpeak(String withSpeak) {
        this.speak = withSpeak;
    }

    /**
     * Indicates whether your bot is accepting, expecting, or ignoring user input
     * after the message is delivered to the client.
     *
     * @return The input hint for the activity.
     */
    public InputHints getInputHint() {
        return this.inputHint;
    }

    /**
     * Indicates whether your bot is accepting, expecting, or ignoring user input
     * after the message is delivered to the client.
     *
     * @param withInputHint The input hint for the activity.
     */
    public void setInputHint(InputHints withInputHint) {
        this.inputHint = withInputHint;
    }

    /**
     * Gets the text to display if the channel cannot render cards.
     *
     * @return The summary text.
     */
    public String getSummary() {
        return this.summary;
    }

    /**
     * Sets the text to display if the channel cannot render cards.
     *
     * @param withSummary The summary text.
     */
    public void setSummary(String withSummary) {
        this.summary = withSummary;
    }

    /**
     * Gets the suggested actions for the activity.
     *
     * @return The SuggestedActions for the activity.
     */
    public SuggestedActions getSuggestedActions() {
        return this.suggestedActions;
    }

    /**
     * The suggested actions for the activity.
     *
     * @param withSuggestedActions The SuggestedActions for the Activity.
     */
    public void setSuggestedActions(SuggestedActions withSuggestedActions) {
        this.suggestedActions = withSuggestedActions;
    }

    /**
     * Gets the attachments to the Activity.
     *
     * @return A List of {@link Attachment}.
     */
    public List<Attachment> getAttachments() {
        return this.attachments;
    }

    /**
     * Sets the attachments to the Activity.
     *
     * @param withAttachments A List of {@link Attachment}.
     */
    public void setAttachments(List<Attachment> withAttachments) {
        this.attachments = withAttachments;
    }

    /**
     * Sets a single attachment on the Activity.
     *
     * @param withAttachment The Attachment object.
     */
    public void setAttachment(Attachment withAttachment) {
        setAttachments(Collections.singletonList(withAttachment));
    }

    /**
     * Returns payload version of the Entities in an Activity.
     *
     * Entities can vary in the number of fields. The {@link Entity} class holds the
     * additional fields in {@link Entity#getProperties()}.
     *
     * To convert to other entity types, use {@link Entity#getAs(Class)}.
     *
     * @see Mention
     * @see Place
     * @see GeoCoordinates
     * @see Activity#getMentions()
     *
     *      {@code
     * getEntities().stream()
     *             .filter(entity -> entity.getType().equalsIgnoreCase("mention"))
     *             .map(entity -> entity.getAs(Mention.class))
     *             .collect(Collectors.toCollection(ArrayList::new));
     * }
     *
     * @return A List of {@link Entity}.
     */
    public List<Entity> getEntities() {
        return this.entities;
    }

    /**
     * Sets payload version of the Entities in an Activity.
     *
     * @param withEntities The payload entities.
     * @see Entity
     */
    public void setEntities(List<Entity> withEntities) {
        this.entities = withEntities;
    }

    /**
     * Sets payload version of the Mentions in an Activity.
     *
     * @param withMentions The payload entities.
     * @see Entity
     */
    public void setMentions(List<Mention> withMentions) {
        List<Entity> converted = withMentions.stream()
            .filter(entity -> entity.getType().equalsIgnoreCase("mention"))
            .map(entity -> Entity.getAs(entity, Entity.class))
            .collect(Collectors.toCollection(ArrayList::new));
        setEntities(converted);
    }

    /**
     * Gets channel-specific content.
     *
     * @return Channel specific data.
     */
    public Object getChannelData() {
        return this.channelData;
    }

    /**
     * Sets channel-specific content.
     *
     * @param withChannelData Channel specific data as a JsonNode.
     */
    public void setChannelData(Object withChannelData) {
        this.channelData = withChannelData;
    }

    /**
     * Gets the ID of the message to which this message is a reply.
     *
     * @return The reply to ID.
     */
    public String getReplyToId() {
        return this.replyToId;
    }

    /**
     * Sets the ID of the message to which this message is a reply.
     *
     * @param withReplyToId The reply to ID.
     */
    public void setReplyToId(String withReplyToId) {
        this.replyToId = withReplyToId;
    }

    /**
     * Gets the a code for endOfConversation activities that indicates why the
     * conversation ended.
     *
     * @return The endOfConversation code.
     */
    public EndOfConversationCodes getCode() {
        return this.code;
    }

    /**
     * Sets the a code for endOfConversation activities that indicates why the
     * conversation ended.
     *
     * @param withCode The endOfConversation code.
     */
    public void setCode(EndOfConversationCodes withCode) {
        this.code = withCode;
    }

    /**
     * Gets the time at which the activity should be considered to be expired and
     * should not be presented to the recipient.
     *
     * @return the activity expiration.
     */
    public LocalDateTime getExpiration() {
        return this.expiration;
    }

    /**
     * Sets the time at which the activity should be considered to be expired and
     * should not be presented to the recipient.
     *
     * @param withExpiration The activity expiration.
     */
    public void setExpiration(LocalDateTime withExpiration) {
        this.expiration = withExpiration;
    }

    /**
     * Gets the importance of the activity.
     *
     * @return The activity importance.
     */
    public String getImportance() {
        return this.importance;
    }

    /**
     * Sets the importance of the activity.
     *
     * @param withImportance The activity importance.
     */
    public void setImportance(String withImportance) {
        this.importance = withImportance;
    }

    /**
     * A delivery hint to signal to the recipient alternate delivery paths for the
     * activity.
     * <p>
     * The default delivery mode is \"default\".  See {@link DeliveryModes}.
     *
     * @return The delivery mode hint.
     */
    public String getDeliveryMode() {
        return this.deliveryMode;
    }

    /**
     * A delivery hint to signal to the recipient alternate delivery paths for the
     * activity.
     * <p>
     * The default delivery mode is \"default\".
     *
     * @param withDeliveryMode The delivery mode hint.
     */
    public void setDeliveryMode(String withDeliveryMode) {
        this.deliveryMode = withDeliveryMode;
    }

    /**
     * Gets the list of phrases and references that speech and language priming
     * systems should listen for.
     *
     * @return List of phrases to listen for.
     */
    public List<String> getListenFor() {
        return this.listenFor;
    }

    /**
     * Sets the list of phrases and references that speech and language priming
     * systems should listen for.
     *
     * @param withListenFor List of phrases to listen for.
     */
    public void setListenFor(List<String> withListenFor) {
        this.listenFor = withListenFor;
    }

    /**
     * Gets the collection of text fragments to highlight when the activity contains
     * a ReplyToId value.
     *
     * @return List of {@link TextHighlight}.
     */
    public List<TextHighlight> getTextHighlights() {
        return this.textHighlights;
    }

    /**
     * Sets the collection of text fragments to highlight when the activity contains
     * a ReplyToId value.
     *
     * @param withTextHighlights List of {@link TextHighlight}.
     */
    public void setTextHighlights(List<TextHighlight> withTextHighlights) {
        this.textHighlights = withTextHighlights;
    }

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     *
     * @return Map of additional properties.
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getProperties() {
        return this.properties;
    }

    /**
     * Holds the overflow properties that aren't first class properties in the
     * object. This allows extensibility while maintaining the object.
     *
     * @param key       The key of the property to set.
     * @param withValue The value for the property.
     */
    @JsonAnySetter
    public void setProperties(String key, JsonNode withValue) {
        this.properties.put(key, withValue);
    }

    /**
     * Gets the updated topic name of the conversation.
     *
     * @return The topic name.
     */
    public String getTopicName() {
        return this.topicName;
    }

    /**
     * Sets the updated topic name of the conversation.
     *
     * @param withTopicName The topic name.
     */
    public void setTopicName(String withTopicName) {
        this.topicName = withTopicName;
    }

    /**
     * Gets whether the prior history of the channel is disclosed.
     *
     * @return True if the history is disclosed.
     */
    public boolean getHistoryDisclosed() {
        return this.historyDisclosed;
    }

    /**
     * Sets whether the prior history of the channel is disclosed.
     *
     * @param withHistoryDisclosed True if the history is disclosed.
     */
    public void setHistoryDisclosed(boolean withHistoryDisclosed) {
        this.historyDisclosed = withHistoryDisclosed;
    }

    /**
     * Gets the collection of members added to the conversation.
     *
     * @return List of {@link ChannelAccount} of added members.
     */
    public List<ChannelAccount> getMembersAdded() {
        return this.membersAdded;
    }

    /**
     * Sets the collection of members added to the conversation.
     *
     * @param withMembersAdded List of {@link ChannelAccount} of added members.
     */
    public void setMembersAdded(List<ChannelAccount> withMembersAdded) {
        this.membersAdded = withMembersAdded;
    }

    /**
     * Gets the collection of members removed from the conversation.
     *
     * @return List of {@link ChannelAccount} of removed members.
     */
    public List<ChannelAccount> getMembersRemoved() {
        return this.membersRemoved;
    }

    /**
     * Sets the collection of members removed from the conversation.
     *
     * @param withMembersRemoved List of {@link ChannelAccount} of removed members.
     */
    public void setMembersRemoved(List<ChannelAccount> withMembersRemoved) {
        this.membersRemoved = withMembersRemoved;
    }

    /**
     * Gets the descriptive label for the activity.
     *
     * @return The activity label.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the descriptive label for the activity.
     *
     * @param withLabel The activity label.
     */
    public void setLabel(String withLabel) {
        this.label = withLabel;
    }

    /**
     * Gets the type of the activity's value object.
     *
     * @return The value type.
     */
    public String getValueType() {
        return this.valueType;
    }

    /**
     * Sets the type of the activity's value object.
     *
     * @param withValueType The type of Activity value.
     */
    public void setValueType(String withValueType) {
        this.valueType = withValueType;
    }

    /**
     * Gets the value that is associated with the activity.
     *
     * @return The Activity value.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the value that is associated with the activity.
     *
     * @param withValue The Activity value.
     */
    public void setValue(Object withValue) {
        this.value = withValue;
    }

    /**
     * Gets the name of the operation associated with an invoke or event activity.
     *
     * @return The Activity name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the operation associated with an invoke or event activity.
     *
     * @param withName The Activity name.
     */
    public void setName(String withName) {
        this.name = withName;
    }

    /**
     * A reference to another conversation or activity.
     *
     * @return The conversation reference.
     */
    public ConversationReference getRelatesTo() {
        return this.relatesTo;
    }

    /**
     * A reference to another conversation or activity.
     *
     * @param withRelatesTo The conversation reference.
     */
    public void setRelatesTo(ConversationReference withRelatesTo) {
        this.relatesTo = withRelatesTo;
    }

    /**
     * Indicates whether the recipient of a contactRelationUpdate was added or
     * removed from the sender's contact list.
     *
     * @return Recipient action.
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Indicates whether the recipient of a contactRelationUpdate was added or
     * removed from the sender's contact list.
     *
     * @param withAction Recipient action.
     */
    public void setAction(String withAction) {
        this.action = withAction;
    }

    /**
     * Creates an instance of the Activity class as type
     * {@link ActivityTypes#TRACE}.
     *
     * @param withName The name of the trace operation to create.
     * @return The new trace activity.
     */
    public Activity createTrace(String withName) {
        return createTrace(withName, null, null, null);
    }

    /**
     * Creates an instance of the Activity class as type
     * {@link ActivityTypes#TRACE}.
     *
     * @param withName      The name of the trace operation to create.
     * @param withValue     Optional, the content for this trace operation.
     * @param withValueType Optional, identifier for the format of withValue.
     *                      Default is the name of type of the withValue.
     * @param withLabel     Optional, a descriptive label for this trace operation.
     * @return The new trace activity.
     */
    public Activity createTrace(
        String withName,
        Object withValue,
        String withValueType,
        String withLabel
    ) {
        Activity reply = new Activity(ActivityTypes.TRACE);

        reply.setName(withName);
        reply.setLabel(withLabel);

        if (withValueType != null) {
            reply.setValueType(withValueType);
        } else {
            reply.setValueType((withValue != null) ? withValue.getClass().getTypeName() : null);
        }
        reply.setValue(withValue);

        if (this.getRecipient() == null) {
            reply.setFrom(new ChannelAccount());
        } else {
            reply.setFrom(
                new ChannelAccount(this.getRecipient().getId(), this.getRecipient().getName())
            );
        }

        if (this.getFrom() == null) {
            reply.setRecipient(new ChannelAccount());
        } else {
            reply
                .setRecipient(new ChannelAccount(this.getFrom().getId(), this.getFrom().getName()));
        }

        if (!StringUtils.equalsIgnoreCase(this.getType(), ActivityTypes.CONVERSATION_UPDATE)
            || !StringUtils.equalsIgnoreCase(this.getChannelId(), "directline")
            && !StringUtils.equalsIgnoreCase(this.getChannelId(), "webchat")) {
                reply.replyToId = this.getId();
        } else {
            reply.replyToId = null;
        }

        reply.setServiceUrl(this.getServiceUrl());
        reply.setChannelId(this.getChannelId());

        if (this.getConversation() != null) {
            reply.setConversation(
                new ConversationAccount(
                    this.getConversation().isGroup(),
                    this.getConversation().getId(),
                    this.getConversation().getName()
                )
            );
        }

        return reply;
    }

    /**
     * Creates a new message activity as a response to this activity.
     *
     * @return The new message activity.
     */
    public Activity createReply() {
        return createReply(null, null);
    }

    /**
     * Creates a new message activity as a response to this activity.
     *
     * This overload uses this Activity's Locale.
     *
     * @param withText The text of the reply.
     * @return The new message activity.
     */
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

        if (this.getRecipient() == null) {
            result.setFrom(new ChannelAccount());
        } else {
            result.setFrom(
                new ChannelAccount(this.getRecipient().getId(), this.getRecipient().getName())
            );
        }

        if (this.getFrom() == null) {
            result.setRecipient(new ChannelAccount());
        } else {
            result
                .setRecipient(new ChannelAccount(this.getFrom().getId(), this.getFrom().getName()));
        }

        if (!StringUtils.equalsIgnoreCase(this.getType(), ActivityTypes.CONVERSATION_UPDATE)
            || !StringUtils.equalsIgnoreCase(this.getChannelId(), "directline")
                && !StringUtils.equalsIgnoreCase(this.getChannelId(), "webchat")) {
            result.replyToId = this.getId();
        } else {
            result.replyToId = null;
        }

        result.setServiceUrl(this.getServiceUrl());
        result.setChannelId(this.getChannelId());

        if (this.getConversation() == null) {
            result.setConversation(new ConversationAccount());
        } else {
            result.setConversation(
                new ConversationAccount(
                    this.getConversation().isGroup(),
                    this.getConversation().getId(),
                    this.getConversation().getName()
                )
            );
        }

        result.setAttachments(new ArrayList<>());
        result.setEntities(new ArrayList<>());

        return result;
    }

    /**
     * Checks if this (message) activity has content.
     *
     * @return Returns true, if this message has any content to send. False
     *         otherwise.
     */
    public boolean hasContent() {
        if (!StringUtils.isBlank(this.getText())) {
            return true;
        }

        if (!StringUtils.isBlank(this.getSummary())) {
            return true;
        }

        if (this.getAttachments() != null && this.getAttachments().size() > 0) {
            return true;
        }

        if (this.getChannelData() != null) {
            return true;
        }

        return false;
    }

    /**
     * Resolves the mentions from the entities of this activity.
     *
     * This method is defined on the {@link Activity} class, but is only
     * intended for use with a message activity, where the activity
     * {@link Activity#type} is set to {@link ActivityTypes#MESSAGE}.
     *
     * @return The array of mentions; or an empty array, if none are found.
     */
    @JsonIgnore
    public List<Mention> getMentions() {
        if (this.getEntities() == null) {
            return Collections.emptyList();
        }

        return this.getEntities()
            .stream()
            .filter(entity -> entity.getType().equalsIgnoreCase("mention"))
            .map(entity -> entity.getAs(Mention.class))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get channelData as typed structure.
     *
     * @param classType Class of TypeT to use
     * @param <TypeT>   The type of the returned object.
     * @return typed Object or default(TypeT)
     * @throws JsonProcessingException If the channel data can't be converted to
     *                                 TypeT.
     */
    public <TypeT> TypeT getChannelData(Class<TypeT> classType) throws JsonProcessingException {
        if (this.getChannelData() == null) {
            return null;
        }

        if (classType.isInstance(this.getChannelData())) {
            return (TypeT) this.getChannelData();
        }
        JsonNode node = MAPPER.valueToTree(this.getChannelData());
        return MAPPER.treeToValue(node, classType);
    }

    /**
     * Get channelData as typed structure.
     *
     * @param clsType Class of TypeT to use
     * @param <TypeT> The type of the returned object.
     * @return ChannelData as TypeT
     */
    public <TypeT> ResultPair<TypeT> tryGetChannelData(Class<TypeT> clsType) {
        TypeT instance = null;
        if (this.getChannelData() == null) {
            return new ResultPair<>(false, instance);
        }

        try {
            instance = this.getChannelData(clsType);
        } catch (JsonProcessingException e) {
            return new ResultPair<TypeT>(false, instance);
        }
        return new ResultPair<TypeT>(true, instance);
    }

    /**
     * Creates a {@link ConversationReference} based on this activity.
     *
     * @return A conversation reference for the conversation that contains this
     *         activity.
     */
    @JsonIgnore
    public ConversationReference getConversationReference() {
        ConversationReference conversationReference = new ConversationReference();
        if (!StringUtils.equalsIgnoreCase(this.getType(), ActivityTypes.CONVERSATION_UPDATE)
            || !StringUtils.equalsIgnoreCase(this.getChannelId(), "directline")
                && !StringUtils.equalsIgnoreCase(this.getChannelId(), "webchat")) {
                conversationReference.setActivityId(this.getId());
        } else {
            conversationReference.setActivityId(null);
        }

        conversationReference.setUser(getFrom());
        conversationReference.setBot(getRecipient());
        conversationReference.setConversation(getConversation());
        conversationReference.setChannelId(getChannelId());
        conversationReference.setLocale(getLocale());
        conversationReference.setServiceUrl(getServiceUrl());
        return conversationReference;
    }

    /**
     * Create a ConversationReference based on this Activity's Conversation info and
     * the ResourceResponse from sending an activity.
     *
     * @param reply ResourceResponse returned from sendActivity.
     * @return A ConversationReference that can be stored and used later to delete
     *         or update the activity.
     */
    @JsonIgnore
    public ConversationReference getReplyConversationReference(ResourceResponse reply) {
        ConversationReference reference = getConversationReference();
        reference.setActivityId(reply.getId());
        return reference;
    }

    /**
     * True if the Activity is of the specified activity type.
     *
     * @param activityType The type to compare to.
     * @return true if the activity is of the specific type.
     */
    protected boolean isActivity(String activityType) {
        String thisType = getType();

        // If there's no type set then we can't tell if it's the type they're looking
        // for
        if (thisType == null) {
            return false;
        }

        // Check if the full type value starts with the type they're looking for
        boolean result = StringUtils.startsWith(thisType.toLowerCase(), activityType.toLowerCase());

        // If the full type value starts with the type they're looking for, then we need
        // to check a little further
        // to check if it's definitely the right type
        if (result) {
            // If the lengths are equal, then it's the exact type they're looking for
            result = thisType.length() == activityType.length();

            if (!result) {
                // If there is a / separator right after the type they're looking for
                result = thisType.charAt(activityType.length()) == '/';
            }
        }

        return result;
    }

    /**
     * Updates this activity with the outgoing delivery information from an existing
     * {@link ConversationReference}.
     *
     * @param reference The existing conversation reference.
     * @return This activity, updated with the delivery information.
     */
    public final Activity applyConversationReference(ConversationReference reference) {
        return applyConversationReference(reference, false);
    }

    /**
     * Updates this activity with the delivery information from an existing
     * {@link ConversationReference}.
     *
     * Call {@link #getConversationReference} on an incoming activity to get a
     * conversation reference that you can then use to update an outgoing activity
     * with the correct delivery information.
     *
     * @param reference  The existing conversation reference.
     * @param isIncoming true to treat the activity as an incoming activity, where
     *                   the bot is the recipient; otherwise, false.
     * @return This activity, updated with the delivery information.
     */
    public final Activity applyConversationReference(
        ConversationReference reference,
        boolean isIncoming
    ) {
        this.setChannelId(reference.getChannelId());
        this.setServiceUrl(reference.getServiceUrl());
        this.setConversation(reference.getConversation());
        this.setLocale((reference.getLocale() == null) ? this.getLocale() : reference.getLocale());

        if (isIncoming) {
            this.setFrom(reference.getUser());
            this.setRecipient(reference.getBot());
            if (reference.getActivityId() != null) {
                this.setId(reference.getActivityId());
            }
        } else {
            this.setFrom(reference.getBot());
            this.setRecipient(reference.getUser());
            if (reference.getActivityId() != null) {
                this.setReplyToId(reference.getActivityId());
            }
        }

        return this;
    }

    /**
     * Determines if the Activity was sent via an Http/Https connection or Streaming
     * This can be determined by looking at the ServiceUrl property:
     * (1) All channels that send messages via http/https are not streaming
     * (2) Channels that send messages via streaming have a ServiceUrl that does not begin with http/https.
     *
     * @return True if the Activity was originate from a streaming connection.
     */
    public Boolean isFromStreamingConnection() {
        if (serviceUrl != null) {
            Boolean isHttp = this.getServiceUrl().toLowerCase().startsWith("http");
            return !isHttp;
        }

        return false;
    }

    /**
     * Remove recipient mention text from Text property. Use with caution because
     * this function is altering the text on the Activity.
     *
     * @return new .Text property value.
     */
    public String removeRecipientMention() {
        if (getRecipient() == null) {
            return text;
        }

        return removeMentionText(getRecipient().getId());
    }

    /**
     * Remove any mention text for given id from the Activity.Text property. For
     * example, given the message "@echoBot Hi Bot", this will remove "@echoBot",
     * leaving "Hi Bot".
     *
     * Typically this would be used to remove the mention text for the target
     * recipient (the bot usually), though it could be called for each member. For
     * example:
     * turnContext.Activity.RemoveMentionText(turnContext.Activity.Recipient.Id);
     * The format of a mention Activity.Entity is dependent on the Channel. But in
     * all cases we expect the Mention.Text to contain the exact text for the user
     * as it appears in Activity.Text. For example, Teams uses
     * &lt;at&gt;username&lt;/at&gt;, whereas slack use @username. It is expected
     * that text is in Activity.Text and this method will remove that value from
     * Activity.Text.
     *
     * @param withId Mention id to match.
     * @return new Activity.Text property value.
     */
    public String removeMentionText(String withId) {
        setText(removeMentionTextImmutable(this, withId));
        return getText();
    }

    /**
     * Removes recipient mention without modifying the Activity.
     *
     * @param activity The Activity to remove mentions from.
     * @return The Activity.Text with mentions removed.
     */
    public static String removeRecipientMentionImmutable(Activity activity) {
        if (activity.getRecipient() == null) {
            return activity.getText();
        }

        return removeMentionTextImmutable(activity, activity.getRecipient().getId());
    }

    /**
     * Removes the mention from the Activity.Text without modifying the Activity.
     *
     * @param activity The Activity to remove mention text on.
     * @param id       The ID of the recipient.
     * @return The Activity.Text with the mention removed.
     */
    public static String removeMentionTextImmutable(Activity activity, String id) {
        if (StringUtils.isEmpty(id)) {
            return activity.getText();
        }

        String text = activity.getText();
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        for (Mention mention : activity.getMentions()) {
            if (StringUtils.equals(mention.getMentioned().getId(), id)) {
                if (StringUtils.isEmpty(mention.getText())) {
                    text = text.replaceAll("<at>" + mention.getMentioned().getName() + "</at>", "");
                } else {
                    text = text.replaceAll(mention.getText(), "");
                }

                text = text.trim();
            }
        }

        return text;
    }

    /**
     * Check if this actvity is from microsoft teams.
     *
     * @return true if the activity is from microsoft teams.
     */
    public boolean isTeamsActivity() {
        return "msteams".equals(channelId);
    }

    /**
     * Get unique identifier representing a channel.
     *
     * @return If this is a Teams Activity with valid data, the unique identifier
     *         representing a channel.
     */
    public String teamsGetChannelId() {
        String teamsChannelId;

        try {
            TeamsChannelData teamsChannelData = getChannelData(TeamsChannelData.class);
            teamsChannelId = teamsChannelData.getTeamsChannelId();
            if (teamsChannelId == null && teamsChannelData.getChannel() != null) {
                teamsChannelId = teamsChannelData.getChannel().getId();
            }
        } catch (JsonProcessingException jpe) {
            teamsChannelId = null;
        }

        return teamsChannelId;
    }

    /**
     * Gets the TeamsChannelData.
     * @return TeamsChannelData
     */
    public TeamsChannelData teamsGetChannelData() {
        TeamsChannelData teamsChannelData;

        try {
            teamsChannelData = getChannelData(TeamsChannelData.class);
        } catch (JsonProcessingException jpe) {
            teamsChannelData = null;
        }

        return teamsChannelData;
    }

    /**
     * Get unique identifier representing a team.
     *
     * @return If this is a Teams Activity with valid data, the unique identifier
     *         representing a team.
     */
    public String teamsGetTeamId() {
        String teamId;

        try {
            TeamsChannelData teamsChannelData = getChannelData(TeamsChannelData.class);
            if (teamsChannelData == null) {
                return null;
            }

            teamId = teamsChannelData.getTeamsTeamId();
            if (teamId == null && teamsChannelData.getTeam() != null) {
                teamId = teamsChannelData.getTeam().getId();
            }
        } catch (JsonProcessingException jpe) {
            teamId = null;
        }

        return teamId;
    }

    /**
     * Get Teams TeamInfo data.
     *
     * @return If this is a Teams Activity with valid data, the TeamInfo object.
     */
    public TeamInfo teamsGetTeamInfo() {
        TeamsChannelData teamsChannelData;

        try {
            teamsChannelData = getChannelData(TeamsChannelData.class);
        } catch (JsonProcessingException jpe) {
            teamsChannelData = null;
        }

        return teamsChannelData != null ? teamsChannelData.getTeam() : null;
    }

    /**
     * Sets the notification value in the TeamsChannelData to true.
     */
    public void teamsNotifyUser() {
        TeamsChannelData teamsChannelData;

        try {
            teamsChannelData = getChannelData(TeamsChannelData.class);
        } catch (JsonProcessingException jpe) {
            teamsChannelData = null;
        }

        if (teamsChannelData == null) {
            teamsChannelData = new TeamsChannelData();
        }

        teamsChannelData.setNotification(new NotificationInfo(true));
        setChannelData(teamsChannelData);
    }

    /**
     * Sets the notification of a meeting in the TeamsChannelData.
     * @param alertInMeeting True if this is a meeting alert.
     * @param externalResourceUrl The external resource Url.
     */
    public void teamsNotifyUser(boolean alertInMeeting, String externalResourceUrl) {
        TeamsChannelData teamsChannelData;

        try {
            teamsChannelData = getChannelData(TeamsChannelData.class);
        } catch (JsonProcessingException jpe) {
            teamsChannelData = null;
        }

        if (teamsChannelData == null) {
            teamsChannelData = new TeamsChannelData();
        }

        teamsChannelData.setNotification(new NotificationInfo(true, externalResourceUrl));
        setChannelData(teamsChannelData);
    }

    /**
     * Gets the TeamsMeetingInfo object from the current activity.
     * @return The current activity's team's meeting, or null.
     */
    public TeamsMeetingInfo teamsGetMeetingInfo() {
        TeamsChannelData teamsChannelData;

        try {
            teamsChannelData = getChannelData(TeamsChannelData.class);
        } catch (JsonProcessingException jpe) {
            teamsChannelData = null;
        }

        return teamsChannelData != null ? teamsChannelData.getMeeting() : null;
    }
}
