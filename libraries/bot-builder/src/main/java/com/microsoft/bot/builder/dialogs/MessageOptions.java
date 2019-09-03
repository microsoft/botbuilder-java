package com.microsoft.bot.builder.dialogs;

import com.microsoft.bot.schema.AttachmentLayoutTypes;
import com.microsoft.bot.schema.TextFormatTypes;

/**
 * Optional message properties that can be sent {@link Extensions.SayAsync(BotToUser, String MessageOptions,)}
 */
public class MessageOptions
{
    public MessageOptions()
    {
        this.setTextFormat(TextFormatTypes.MARKDOWN.toString());
        this.setAttachmentLayout(AttachmentLayoutTypes.LIST.toString());
        // this.Attachments = new ArrayList<Attachment>();
        // this.Entities = new ArrayList<Entity>();
    }

    /**
     * Indicates whether the bot is accepting, expecting, or ignoring input
     */
    //public string InputHint { get; set; }

    /**
     * Format of text fields [plain|markdown] Default:markdown
     */
    String textFormat;
    public String getTextFormat() {
        return this.textFormat;
    }
    public void setTextFormat(String textFormat) {
        this.textFormat = textFormat;
    }



    /**
     * Hint for how to deal with multiple attachments: [list|carousel] Default:list
     */
    String attachmentLayout;
    public String getAttachmentLayout() {
        return this.attachmentLayout;
    }
    public void setAttachmentLayout(String attachmentLayout) {
        this.attachmentLayout = attachmentLayout;
    }

    /**
     * Attachments
     */
    //public IList<Attachment> Attachments { get; set; }

    /**
     * Collection of Entity objects, each of which contains metadata about this activity. Each Entity object is typed.
     */
    //public IList<Microsoft.Bot.Schema.Entity> Entities { get; set; }
}
