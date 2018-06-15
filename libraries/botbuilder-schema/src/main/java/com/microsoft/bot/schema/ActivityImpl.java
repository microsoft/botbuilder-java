package com.microsoft.bot.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.schema.ContactRelationUpdateActivity;
import com.microsoft.bot.schema.TraceActivity;
import com.microsoft.bot.schema.models.*;


import jdk.internal.util.xml.impl.Pair;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/// <summary>
/// An Activity is the basic communication type for the Bot Framework 3.0 protocol
/// </summary>
/// <remarks>
/// The Activity class contains all properties that individual, more specific activities
/// could contain. It is a superset type.
/// </remarks>
public class ActivityImpl extends Activity {
    /// <summary>
    /// Content-type for an Activity
    /// </summary>
    public final String ContentType = "application/vnd.microsoft.activity";
    private static final ObjectMapper mapper = new ObjectMapper();

    void CustomInit() {
    }

    /// <summary>
    /// Take a message and create a reply message for it with the routing information
    /// set up to correctly route a reply to the source message
    /// </summary>
    /// <param name="text">text you want to reply with</param>
    /// <param name="locale">language of your reply</param>
    /// <returns>message set up to route back to the sender</returns>
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
        reply.withFrom(new ChannelAccount());
        reply.withId(recipient().id());
        reply.withName(recipient().name());
        reply.withRecipient(new ChannelAccount());
        reply.withId(from().id());
        reply.withName(from().name());
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

    /// <summary>
/// Create a trace activity based of this activity
/// </summary>
/// <param name="name">Name of the operation</param>
/// <param name="value">value of the operation</param>
/// <param name="valueType">valueType if helpful to identify the value schema (default is value.GetType().Name)</param>
/// <param name="label">descritive label of context. (Default is calling function name)</param>
/// <returns></returns>
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

    /// <summary>
    /// Create an instance of the TraceActivity
    /// </summary>
    /// <param name="name">Name of the operation</param>
    /// <param name="value">value of the operation</param>
    /// <param name="valueType">valueType if helpful to identify the value schema (default is value.GetType().Name)</param>
    /// <param name="label">descritive label of context. (Default is calling function name)</param>
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

    /// <summary>
    /// Extension data for overflow of properties
    /// </summary>
    //        [JsonExtensionData(ReadData = true, WriteData = true)]
    //public JObject Properties { get; set; } = new JObject();

    /// <summary>
    /// Create an instance of the Activity class with IMessageActivity masking
    /// </summary>
    public static MessageActivity CreateMessageActivity() {
        MessageActivity reply = new MessageActivity();
        reply.withType(ActivityTypes.TRACE);
        reply.withTimestamp(DateTime.now());
        reply.withAttachments(new ArrayList<Attachment>());
        reply.withEntities(new ArrayList<EntityImpl>());;
        return reply;
    }

    /// <summary>
    /// Create an instance of the Activity class with IContactRelationUpdateActivity masking
    /// </summary>
    public static ContactRelationUpdateActivity CreateContactRelationUpdateActivity() {
        ContactRelationUpdateActivity reply =  new ContactRelationUpdateActivity();
        reply.withType(ActivityTypes.CONTACT_RELATION_UPDATE);
        return reply;
    }

    /// <summary>
    /// Create an instance of the Activity class with IConversationUpdateActivity masking
    /// </summary>
    public static ConversationUpdateActivity CreateConversationUpdateActivity() {
        ConversationUpdateActivity reply = new ConversationUpdateActivity();
        reply.withType(ActivityTypes.CONVERSATION_UPDATE);
        reply.withMembersAdded(new ArrayList<ChannelAccount>());
        reply.withMembersRemoved(new ArrayList<ChannelAccount>());
        return reply;
    }

    /// <summary>
    /// Create an instance of the Activity class with ITypingActivity masking
    /// </summary>
    //public static TypingActivity CreateTypingActivity() { return new Activity(ActivityTypes.Typing); }

    /// <summary>
    /// Create an instance of the Activity class with IActivity masking
    /// </summary>
    public static Activity CreatePingActivity() {
        return new Activity().withType(ActivityTypes.PING);
    }

    /// <summary>
    /// Create an instance of the Activity class with IEndOfConversationActivity masking
    /// </summary>
    //public static IEndOfConversationActivity CreateEndOfConversationActivity() { return new Activity(ActivityTypes.EndOfConversation); }

    /// <summary>
    /// Create an instance of the Activity class with an IEventActivity masking
    /// </summary>
    //public static IEventActivity CreateEventActivity() { return new Activity(ActivityTypes.Event); }

    /// <summary>
    /// Create an instance of the Activity class with IInvokeActivity masking
    /// </summary>
    //public static IInvokeActivity CreateInvokeActivity() { return new Activity(ActivityTypes.Invoke); }


    /// <summary>
    /// True if the Activity is of the specified activity type
    /// </summary>
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

    /// <summary>
    /// Return an IMessageActivity mask if this is a message activity
    /// </summary>
    public MessageActivity AsMessageActivity() {
        return IsActivity(ActivityTypes.MESSAGE.toString()) ? (MessageActivity) (Activity) this : null;
    }

    /// <summary>
    /// Return an IContactRelationUpdateActivity mask if this is a contact relation update activity
    /// </summary>
    public ContactRelationUpdateActivity AsContactRelationUpdateActivity() {
        return IsActivity(ActivityTypes.CONTACT_RELATION_UPDATE.toString()) ? (ContactRelationUpdateActivity) (Activity) this : null;
    }

    /// <summary>
    /// Return an IInstallationUpdateActivity mask if this is a installation update activity
    /// </summary>
    //public InstallationUpdateActivity AsInstallationUpdateActivity() { return IsActivity(ActivityTypes.INSTALLATION_UPDATE.toString()) ? this : null; }

    /// <summary>
    /// Return an IConversationUpdateActivity mask if this is a conversation update activity
    /// </summary>
    //public ConversationUpdateActivity AsConversationUpdateActivity() { return IsActivity(ActivityTypes.ConversationUpdate) ? this : null; }

    /// <summary>
    /// Return an ITypingActivity mask if this is a typing activity
    /// </summary>
    // public TypingActivity AsTypingActivity() { return IsActivity(ActivityTypes.TYPING.toString()) ? (TypingActivity)(Activity)this : null; }

    /// <summary>
    /// Return an IEndOfConversationActivity mask if this is an end of conversation activity
    /// </summary>
    //public IEndOfConversationActivity AsEndOfConversationActivity() { return IsActivity(ActivityTypes.EndOfConversation) ? this : null; }

    /// <summary>
    /// Return an IEventActivity mask if this is an event activity
    /// </summary>
    //public IEventActivity AsEventActivity() { return IsActivity(ActivityTypes.Event) ? this : null; }

    /// <summary>
    /// Return an IInvokeActivity mask if this is an invoke activity
    /// </summary>
    //public IInvokeActivity AsInvokeActivity() { return IsActivity(ActivityTypes.Invoke) ? this : null; }

    /// <summary>
    /// Return an IMessageUpdateAcitvity if this is a MessageUpdate activity
    /// </summary>
    /// <returns></returns>
    //public IMessageUpdateActivity AsMessageUpdateActivity() { return IsActivity(ActivityTypes.MessageUpdate) ? this : null; }

    /// <summary>
    /// Return an IMessageDeleteActivity if this is a MessageDelete activity
    /// </summary>
    /// <returns></returns>
    //public IMessageDeleteActivity AsMessageDeleteActivity() { return IsActivity(ActivityTypes.MessageDelete) ? this : null; }

    /// <summary>
    /// Return an IMessageReactionActivity if this is a MessageReaction activity
    /// </summary>
    /// <returns></returns>
    //public IMessageReactionActivity AsMessageReactionActivity() { return IsActivity(ActivityTypes.MessageReaction) ? this : null; }

    /// <summary>
    /// Return an ISuggestionActivity if this is a Suggestion activity
    /// </summary>
    /// <returns></returns>
    //public ISuggestionActivity AsSuggestionActivity() { return IsActivity(ActivityTypes.Suggestion) ? this : null; }

    /// <summary>
    /// Return an ITraceActivity if this is a Trace activity
    /// </summary>
    /// <returns></returns>
    //public ITraceActivity AsTraceActivity() { return IsActivity(ActivityTypes.Trace) ? this : null; }

    /// <summary>
    /// Checks if this (message) activity has content.
    /// </summary>
    /// <returns>Returns true, if this message has any content to send. False otherwise.</returns>
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

    private Mention convertToMention(ObjectNode node) {
        try {
            return ActivityImpl.mapper.treeToValue(node, Mention.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;

    }
    /// <summary>
    /// Resolves the mentions from the entities of this (message) activity.
    /// </summary>
    /// <returns>The array of mentions or an empty array, if none found.</returns>
    public ArrayList<Mention> GetMentions() {
        ArrayList<Mention> list = (ArrayList) this.entities().stream()
                .filter(entity -> entity.type().equalsIgnoreCase("mention"))
                .map(entity -> convertToMention(entity.getProperties()))
                .collect(Collectors.toCollection(ArrayList::new)); // create mutable list
        return list;
    }

    /// <summary>
    /// Get channeldata as typed structure
    /// </summary>
    /// <param name="activity"></param>
    /// <typeparam name="TypeT">type to use</typeparam>
    /// <returns>typed Object or default(TypeT)</returns>
    public <TypeT> TypeT GetChannelData(Class<TypeT> classType) throws JsonProcessingException {
        if (this.channelData() == null)
            return null;

        if (classType.isInstance(this.channelData())) {
            return ((TypeT) this.channelData());
        }
        JsonNode node = mapper.valueToTree(this.channelData());
        return mapper.treeToValue((TreeNode) node, classType);
    }

    /// <summary>
    /// Get channeldata as typed structure
    /// </summary>
    /// <param name="activity"></param>
    /// <typeparam name="TypeT">type to use</typeparam>
    /// <param name="instance">The resulting instance, if possible</param>
    /// <returns>
    /// <c>true</c> if value of <seealso cref="IActivity.ChannelData"/> was coerceable to <typeparamref name="TypeT"/>, <c>false</c> otherwise.
    /// </returns>

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
}
