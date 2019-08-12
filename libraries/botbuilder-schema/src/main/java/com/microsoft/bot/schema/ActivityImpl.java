package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ActivityTypes;
import com.microsoft.bot.schema.models.Attachment;
import com.microsoft.bot.schema.models.ChannelAccount;
import com.microsoft.bot.schema.models.ConversationAccount;
import com.microsoft.bot.schema.models.ConversationReference;
import com.microsoft.bot.schema.models.ConversationUpdateActivity;
import com.microsoft.bot.schema.models.EndOfConversationCodes;
import com.microsoft.bot.schema.models.InputHints;
import com.microsoft.bot.schema.models.Mention;
import com.microsoft.bot.schema.models.MessageActivity;
import com.microsoft.bot.schema.models.SuggestedActions;
import com.microsoft.bot.schema.models.TextHighlight;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An Activity is the basic communication type for the Bot Framework 3.0 protocol
 *
 * The Activity class contains all properties that individual, more specific activities
 * could contain. It is a superset type.
 *
 */
public class ActivityImpl extends Activity {
    /**
     * Content-type for an Activity
     */
    public final String ContentType = "application/vnd.microsoft.activity";
    private static final ObjectMapper mapper = new ObjectMapper();

    void CustomInit() {
    }

    /**
     * Take a message and create a reply message for it with the routing information
     * set up to correctly route a reply to the source message
     * @param text text you want to reply with
     * @param locale language of your reply
     * @return message set up to route back to the sender
     */
    public ActivityImpl CreateReply() {
        return CreateReply(null, null);
    }

    public ActivityImpl CreateReply(String text) {
        return CreateReply(text, null);
    }

    public ActivityImpl CreateReply(String text, String locale) {
        ActivityImpl reply = new ActivityImpl();
        reply.withType(ActivityTypes.MESSAGE);
        reply.withTimestamp(DateTime.now());
        reply.withFrom(new ChannelAccount()
                        .withId(recipient().id())
                        .withName(recipient().name()));
        reply.withRecipient(new ChannelAccount()
                        .withId(from().id())
                        .withName(from().name()));
        reply.withReplyToId(this.id());
        reply.withServiceUrl(this.serviceUrl());
        reply.withChannelId(channelId());
        reply.withConversation(new ConversationAccount()
                        .withIsGroup(conversation().isGroup())
                        .withId(conversation().id())
                        .withName(conversation().name()));
        reply.withText((text == null) ? "" : text);
        reply.withLocale((locale == null) ? "" : locale);
        reply.withAttachments(new ArrayList<Attachment>());
        reply.withEntities(new ArrayList<EntityImpl>());
        return reply;
    }

    /**
     * Create a trace activity based of this activity
     * @param name Name of the operation
     * @param value value of the operation
     * @param valueType valueType if helpful to identify the value schema (default is value.GetType().Name)
     * @param label descritive label of context. (Default is calling function name)
     * @return
     */
    public TraceActivity CreateTrace(String name) {
        return CreateTrace(name, null, null, null);
    }

    public TraceActivity CreateTrace(String name, Object value) {
        return CreateTrace(name, value, null, null);

    }

    public TraceActivity CreateTrace(String name, Object value, String valueType) {
        return CreateTrace(name, value, valueType, null);
    }

    // public TraceActivity CreateTrace(String name, Object value, String valueType, [CallerMemberName] String label)
    public TraceActivity CreateTrace(String name, Object value, String valueType, String label) {
        TraceActivity reply = new TraceActivity();
        reply.withType(ActivityTypes.TRACE);
        reply.withTimestamp(DateTime.now());
        reply.withFrom(new ChannelAccount()
                .withId(recipient().id())
                .withName(recipient().name()));
        reply.withRecipient(new ChannelAccount()
                .withId(from().id())
                .withName(from().name()));
        reply.withReplyToId(this.id());
        reply.withServiceUrl(this.serviceUrl());
        reply.withChannelId(channelId());
        reply.withConversation(new ConversationAccount()
                .withIsGroup(conversation().isGroup())
                .withId(conversation().id())
                .withName(conversation().name()));
        reply.withName(name);
        reply.withLabel(label);
        reply.withValueType((valueType == null) ? value.getClass().getTypeName() : valueType);
        reply.withValue(value);
        return reply;
    }

    /**
     * Create an instance of the TraceActivity
     * @param name Name of the operation
     * @param value value of the operation
     * @param valueType valueType if helpful to identify the value schema (default is value.GetType().Name)
     * @param label descritive label of context. (Default is calling function name)
     */
    public static TraceActivity CreateTraceActivity(String name, String valueType) {
        return CreateTraceActivity(name, valueType, null, null);
    }

    public static TraceActivity CreateTraceActivity(String name, String valueType, Object value) {
        return CreateTraceActivity(name, valueType, value, null);
    }

    // public static TraceActivity CreateTraceActivity(String name, String valueType, Object value, [CallerMemberName] String label=null)
    public static TraceActivity CreateTraceActivity(String name, String valueType, Object value, String label) {
        TraceActivity reply = (TraceActivity) new TraceActivity();
        reply.withType(ActivityTypes.TRACE);
        reply.withName(name);
        reply.withLabel(label);
        reply.withValueType((valueType == null) ? value.getClass().getTypeName() : valueType);
        reply.withValue(value);
        return reply;

    }

    /**
     * Extension data for overflow of properties
     */
    //        [JsonExtensionData(ReadData = true, WriteData = true)]
    //public JObject Properties { get; set; } = new JObject();

    /**
     * Create an instance of the Activity class with MessageActivity masking
     */
    public static MessageActivity CreateMessageActivity() {
        MessageActivity reply = new MessageActivity();
        reply.withType(ActivityTypes.TRACE);
        reply.withTimestamp(DateTime.now());
        reply.withAttachments(new ArrayList<Attachment>());
        reply.withEntities(new ArrayList<EntityImpl>());
        return reply;
    }

    /**
     * Create an instance of the Activity class with IContactRelationUpdateActivity masking
     */
    public static ContactRelationUpdateActivity CreateContactRelationUpdateActivity() {
        ContactRelationUpdateActivity reply =  new ContactRelationUpdateActivity();
        reply.withType(ActivityTypes.CONTACT_RELATION_UPDATE);
        return reply;
    }

    /**
     * Create an instance of the Activity class with IConversationUpdateActivity masking
     */
    public static ConversationUpdateActivity CreateConversationUpdateActivity() {
        ConversationUpdateActivity reply = new ConversationUpdateActivity();
        reply.withType(ActivityTypes.CONVERSATION_UPDATE);
        reply.withMembersAdded(new ArrayList<ChannelAccount>());
        reply.withMembersRemoved(new ArrayList<ChannelAccount>());
        return reply;
    }

    /**
     * Create an instance of the Activity class with ITypingActivity masking
     */
    //public static TypingActivity CreateTypingActivity() { return new Activity(ActivityTypes.Typing); }

    /**
     * Create an instance of the Activity class with IEndOfConversationActivity masking
     */
    //public static IEndOfConversationActivity CreateEndOfConversationActivity() { return new Activity(ActivityTypes.EndOfConversation); }

    /**
     * Create an instance of the Activity class with an IEventActivity masking
     */
    //public static IEventActivity CreateEventActivity() { return new Activity(ActivityTypes.Event); }

    /**
     * Create an instance of the Activity class with IInvokeActivity masking
     */
    //public static IInvokeActivity CreateInvokeActivity() { return new Activity(ActivityTypes.Invoke); }


    /**
     * True if the Activity is of the specified activity type
     */
    protected boolean IsActivity(String activityType) {
        /*
         * NOTE: While it is possible to come up with a fancy looking "one-liner" to solve
         * this problem, this code is purposefully more verbose due to optimizations.
         *
         * This main goal of the optimizations was to make zero allocations because it is called
         * by all of the .AsXXXActivity methods which are used in a pattern heavily upstream to
         * "pseudo-cast" the activity based on its type.
         */

        ActivityTypes type = this.type();

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

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withType(ActivityTypes type) {
        super.withType(type);
        return this;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withId(String id) {
        super.withId(id);
        return this;
    }

    /**
     * Set the timestamp value.
     *
     * @param timestamp the timestamp value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withTimestamp(DateTime timestamp) {
        super.withTimestamp(timestamp);
        return this;
    }
    /**
     * Set the localTimestamp value.
     *
     * @param localTimestamp the localTimestamp value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withLocalTimestamp(DateTime localTimestamp) {
        super.withLocalTimestamp(localTimestamp);
        return this;
    }

    /**
     * Set the serviceUrl value.
     *
     * @param serviceUrl the serviceUrl value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withServiceUrl(String serviceUrl) {
        super.withServiceUrl(serviceUrl);
        return this;
    }

    /**
     * Set the channelId value.
     *
     * @param channelId the channelId value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withChannelId(String channelId) {
        super.withChannelId(channelId);
        return this;
    }
    /**
     * Set the from value.
     *
     * @param from the from value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withFrom(ChannelAccount from) {
        super.withFrom(from);
        return this;
    }
    /**
     * Set the conversation value.
     *
     * @param conversation the conversation value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withConversation(ConversationAccount conversation) {
        super.withConversation(conversation);
        return this;
    }
    /**
     * Set the recipient value.
     *
     * @param recipient the recipient value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withRecipient(ChannelAccount recipient) {
        super.withRecipient(recipient);
        return this;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withText(String text) {
        super.withText(text);
        return this;
    }

    /**
     * Set the speak value.
     *
     * @param speak the speak value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withSpeak(String speak) {
        super.withSpeak(speak);
        return this;
    }


    /**
     * Set the inputHint value.
     *
     * @param inputHint the inputHint value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withInputHint(InputHints inputHint) {
        super.withInputHint(inputHint);
        return this;
    }

    /**
     * Set the summary value.
     *
     * @param summary the summary value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withSummary(String summary) {
        super.withSummary(summary);
        return this;
    }


    /**
     * Set the suggestedActions value.
     *
     * @param suggestedActions the suggestedActions value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withSuggestedActions(SuggestedActions suggestedActions) {
        super.withSuggestedActions(suggestedActions);
        return this;
    }


    /**
     * Set the attachments value.
     *
     * @param attachments the attachments value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withAttachments(List<Attachment> attachments) {
        super.withAttachments(attachments);
        return this;
    }


    /**
     * Set the entities value.
     *
     * @param entities the entities value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withEntities(List<EntityImpl> entities) {
        super.withEntities(entities);
        return this;
    }


    /**
     * Set the channelData value.
     *
     * @param channelData the channelData value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withChannelData(Object channelData) {
        super.withChannelData(channelData);
        return this;
    }


    /**
     * Set the action value.
     *
     * @param action the action value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withAction(String action) {
        super.withAction(action);
        return this;
    }

    /**
     * Set the replyToId value.
     *
     * @param replyToId the replyToId value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withReplyToId(String replyToId) {
        super.withReplyToId(replyToId);
        return this;
    }

    /**
     * Set the label value.
     *
     * @param label the label value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withLabel(String label) {
        super.withLabel(label);
        return this;
    }

    /**
     * Set the valueType value.
     *
     * @param valueType the valueType value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withValueType(String valueType) {
        super.withValueType(valueType);
        return this;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withValue(Object value) {
        super.withValue(value);
        return this;
    }


    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withName(String name) {
        super.withName(name);
        return this;
    }


    /**
     * Set the relatesTo value.
     *
     * @param relatesTo the relatesTo value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withRelatesTo(ConversationReference relatesTo) {
        super.withRelatesTo(relatesTo);
        return this;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withCode(EndOfConversationCodes code) {
        super.withCode(code);
        return this;
    }

    /**
     * Set the expiration value.
     *
     * @param expiration the expiration value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withExpiration(DateTime expiration) {
        super.withExpiration(expiration);
        return this;
    }

    /**
     * Set the importance value.
     *
     * @param importance the importance value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withImportance(String importance) {
        super.withImportance(importance);
        return this;
    }

    /**
     * Set the deliveryMode value.
     *
     * @param deliveryMode the deliveryMode value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withDeliveryMode(String deliveryMode) {
        super.withDeliveryMode(deliveryMode);
        return this;
    }

    /**
     * Set the textHighlights value.
     *
     * @param textHighlights the textHighlights value to set
     * @return the Activity object itself.
     */
    public ActivityImpl withTextHighlights(List<TextHighlight> textHighlights) {
        super.withTextHighlights(textHighlights);
        return this;
    }

    /**
     * Return an MessageActivity mask if this is a message activity
     */
    public MessageActivity AsMessageActivity() {
        return IsActivity(ActivityTypes.MESSAGE.toString()) ? (MessageActivity) (Activity) this : null;
    }

    /**
     * Return an ContactRelationUpdateActivity mask if this is a contact relation update activity
     */
    public ContactRelationUpdateActivity AsContactRelationUpdateActivity() {
        return IsActivity(ActivityTypes.CONTACT_RELATION_UPDATE.toString()) ? (ContactRelationUpdateActivity) (Activity) this : null;
    }



    /**
     * Return an InstallationUpdateActivity mask if this is a installation update activity
     */
    //public InstallationUpdateActivity AsInstallationUpdateActivity() { return IsActivity(ActivityTypes.INSTALLATION_UPDATE.toString()) ? this : null; }

    /**
     * Return an ConversationUpdateActivity mask if this is a conversation update activity
     */
    //public ConversationUpdateActivity AsConversationUpdateActivity() { return IsActivity(ActivityTypes.ConversationUpdate) ? this : null; }

    /**
     * Return an TypingActivity mask if this is a typing activity
     */
    // public TypingActivity AsTypingActivity() { return IsActivity(ActivityTypes.TYPING.toString()) ? (TypingActivity)(Activity)this : null; }

    /**
     * Return an IEndOfConversationActivity mask if this is an end of conversation activity
     */
    //public IEndOfConversationActivity AsEndOfConversationActivity() { return IsActivity(ActivityTypes.EndOfConversation) ? this : null; }

    /**
     * Return an IEventActivity mask if this is an event activity
     */
    //public IEventActivity AsEventActivity() { return IsActivity(ActivityTypes.Event) ? this : null; }

    /**
     * Return an IInvokeActivity mask if this is an invoke activity
     */
    //public IInvokeActivity AsInvokeActivity() { return IsActivity(ActivityTypes.Invoke) ? this : null; }

    /**
     * Return an IMessageUpdateAcitvity if this is a MessageUpdate activity
     * @return
     */
    //public IMessageUpdateActivity AsMessageUpdateActivity() { return IsActivity(ActivityTypes.MessageUpdate) ? this : null; }

    /**
     * Return an IMessageDeleteActivity if this is a MessageDelete activity
     * @return
     */
    //public IMessageDeleteActivity AsMessageDeleteActivity() { return IsActivity(ActivityTypes.MessageDelete) ? this : null; }

    /**
     * Return an IMessageReactionActivity if this is a MessageReaction activity
     * @return
     */
    //public IMessageReactionActivity AsMessageReactionActivity() { return IsActivity(ActivityTypes.MessageReaction) ? this : null; }

    /**
     * Return an ISuggestionActivity if this is a Suggestion activity
     * @return
     */
    //public ISuggestionActivity AsSuggestionActivity() { return IsActivity(ActivityTypes.Suggestion) ? this : null; }

    /**
     * Return an ITraceActivity if this is a Trace activity
     * @return
     */
    //public ITraceActivity AsTraceActivity() { return IsActivity(ActivityTypes.Trace) ? this : null; }

    /**
     * Checks if this (message) activity has content.
     * @return Returns true, if this message has any content to send. False otherwise.
     */
    public boolean HasContent() {
        if (!StringUtils.isBlank(this.text()))
            return true;

        if (!StringUtils.isBlank(this.summary()))
            return true;

        if (this.attachments() != null && this.attachments().size() > 0)
            return true;

        if (this.channelData() != null)
            return true;

        return false;
    }

    public Mention convertToMention(JsonNode node) {
        try {
            return ActivityImpl.mapper.treeToValue(node, Mention.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;

    }
    /**
     * Resolves the mentions from the entities of this (message) activity.
     * @return The array of mentions or an empty array, if none found.
     * TODO: Need to see how mentions are formated in the message
     */
    public ArrayList<Mention> GetMentions() {
        ArrayList<Mention> list = null;
//        (ArrayList) this.entities().stream()
//                .filter(entity -> entity.type().equalsIgnoreCase("mention"))
//                .map(entity -> convertToMention(entity.getProperties()))
//                .collect(Collectors.toCollection(ArrayList::new)); // create mutable list
        return list;
    }

    /**
     * Get channeldata as typed structure
     * @param activity
     * @param TypeT type to use
     * @return typed Object or default(TypeT)
     */
    public <TypeT> TypeT GetChannelData(Class<TypeT> classType) throws JsonProcessingException {
        if (this.channelData() == null)
            return null;

        if (classType.isInstance(this.channelData())) {
            return (TypeT) this.channelData();
        }
        JsonNode node = mapper.valueToTree(this.channelData());
        return mapper.treeToValue((TreeNode) node, classType);
    }

    /**
     * Get channeldata as typed structure
     * @param activity
     * @param TypeT type to use
     * @param instance The resulting instance, if possible
     * @return
     * {@code true} if value of {@linkalso Activity.ChannelData} was coerceable to {@code TypeT}, {@code false} otherwise.
     */

    public <TypeT> ResultPair<Boolean, TypeT> TryGetChannelData(Class<TypeT> clsType) {
        TypeT instance = null;
        if (this.channelData() == null)
            return new ResultPair<>(false, instance);

        try {
            instance = this.<TypeT>GetChannelData(clsType);
        } catch (JsonProcessingException e) {
            return new ResultPair<Boolean, TypeT>(false, instance);
        }
        return new ResultPair<Boolean, TypeT>(true, instance);
    }
    /**
     * Clone a activity
     * @param activity
     * @return new cloned activity
     */
    public static Activity CloneActity(Activity activity) {
        Activity clone = new Activity()
                .withType(activity.type())
                .withId(activity.id())
                .withTimestamp(activity.timestamp())
                .withLocalTimestamp(activity.localTimestamp())
                .withText(activity.text())
                .withFrom(activity.from())
                .withRecipient(activity.recipient())
                .withConversation(activity.conversation())
                .withChannelId(activity.channelId())
                .withServiceUrl(activity.serviceUrl())
                .withChannelId(activity.channelId())
                .withText(activity.text())
                .withSpeak(activity.speak())
                .withInputHint(activity.inputHint())
                .withSummary(activity.summary())
                .withSuggestedActions(activity.suggestedActions())
                .withAttachments(activity.attachments())
                .withEntities(activity.entities())
                .withChannelData(activity.channelData())
                .withAction(activity.action())
                .withReplyToId(activity.replyToId())
                .withLabel(activity.label())
                .withValueType(activity.valueType())
                .withValue(activity.value())
                .withName(activity.name())
                .withRelatesTo(activity.relatesTo())
                .withCode(activity.code())
                .withExpiration(activity.expiration())
                .withImportance(activity.importance())
                .withDeliveryMode(activity.deliveryMode())
                .withTextHighlights(activity.textHighlights());
        for (Map.Entry<String, JsonNode> entry : activity.properties().entrySet()) {
            clone.setProperties(entry.getKey(), entry.getValue());
        }
        return clone;

    }

}
