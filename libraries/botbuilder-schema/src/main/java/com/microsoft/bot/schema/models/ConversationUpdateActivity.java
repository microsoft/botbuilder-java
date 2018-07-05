package com.microsoft.bot.schema.models;

// Note: In C# implementation, main Activity interface does not contain complete wire format.

import java.util.List;

public class ConversationUpdateActivity extends MessageActivity {
    /**
     * Members added to the conversation
     */
    private List<ChannelAccount> membersAdded;

    @Override
    public List<ChannelAccount> membersAdded() {
        return this.membersAdded;
    }
    @Override
    public ConversationUpdateActivity withMembersAdded(List<ChannelAccount> membersAdded) {
        this.membersAdded = membersAdded;
        return this;
    }

    /**
     * Members removed from the conversation
     */
    private List<ChannelAccount> membersRemoved;
    public List<ChannelAccount> membersRemoved() {
        return this.membersRemoved;
    }
    @Override
    public ConversationUpdateActivity withMembersRemoved(List<ChannelAccount> membersRemoved) {

        this.membersRemoved = membersRemoved;
        return this;
    }

    /**
     * The conversation's updated topic name
     */
    private String topicName;
    @Override
    public String topicName() {
        return this.topicName;
    }
    @Override
    public ConversationUpdateActivity withTopicName(String topicname) {
        this.topicName = topicname;
        return this;
    }


    /**
     * True if prior history of the channel is disclosed
     * Note: Boolean (class) is used, may be null
     */
    private Boolean historyDisclosed;
    public Boolean historyDisclosed() {
        return this.historyDisclosed;
    }
    public ConversationUpdateActivity withHistoryDisclosed(Boolean historydisclosed) {
        this.historyDisclosed = historydisclosed;
        return this;
    }
}
