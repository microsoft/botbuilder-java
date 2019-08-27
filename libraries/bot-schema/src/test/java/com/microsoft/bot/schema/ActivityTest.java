package com.microsoft.bot.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Test;

public class ActivityTest {
    @Test
    public void GetConversationReference() {

    }

    private Activity createActivity() {
        ChannelAccount account1 = new ChannelAccount() {{
           setId("ChannelAccount_Id_1");
           setName("ChannelAccount_Name_1");
           setProperties("Name", JsonNodeFactory.instance.objectNode());

        }};
    }
}
