package com.microsoft.bot.builder.dialogs;

import com.microsoft.bot.schema.models.AttachmentLayoutTypes;
import com.microsoft.bot.schema.models.TextFormatTypes;

/// <summary>
/// Optional message properties that can be sent <see cref="Extensions.SayAsync(IBotToUser, string, string, MessageOptions, string, CancellationToken)"/>
/// </summary>
public class MessageOptions
{
    public MessageOptions()
    {
        this.setTextFormat(TextFormatTypes.MARKDOWN.toString());
        this.setAttachmentLayout(AttachmentLayoutTypes.LIST.toString());
        // this.Attachments = new ArrayList<Attachment>();
        // this.Entities = new ArrayList<Entity>();
    }

    /// <summary>
    /// Indicates whether the bot is accepting, expecting, or ignoring input
    /// </summary>
    //public string InputHint { get; set; }

    /// <summary>
    /// Format of text fields [plain|markdown] Default:markdown
    /// </summary>
    String textFormat;
    public String getTextFormat() {
        return this.textFormat;
    }
    public void setTextFormat(String textFormat) {
        this.textFormat = textFormat;
    }



    /// <summary>
    /// Hint for how to deal with multiple attachments: [list|carousel] Default:list
    /// </summary>
    String attachmentLayout;
    public String getAttachmentLayout() {
        return this.attachmentLayout;
    }
    public void setAttachmentLayout(String attachmentLayout) {
        this.attachmentLayout = attachmentLayout;
    }

    /// <summary>
    /// Attachments
    /// </summary>
    //public IList<Attachment> Attachments { get; set; }

    /// <summary>
    /// Collection of Entity objects, each of which contains metadata about this activity. Each Entity object is typed.
    /// </summary>
    //public IList<Microsoft.Bot.Schema.Entity> Entities { get; set; }
}
