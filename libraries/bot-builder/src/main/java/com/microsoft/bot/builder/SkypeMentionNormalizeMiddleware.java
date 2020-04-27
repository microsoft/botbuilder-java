// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Entity;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Middleware to patch mention Entities from Skype since they don't conform to
 * expected values. Bots that interact with Skype should use this middleware if
 * mentions are used.
 * <p>
 * A Skype mention "text" field is of the format: &lt;at
 * id=\"28:2bc5b54d-5d48-4ff1-bd25-03dcbb5ce918\">botname&lt;/at&gt; But
 * Activity.Text doesn't contain those tags and RemoveMentionText can't remove
 * the entity from Activity.Text. This will remove the &lt;at&gt; nodes, leaving
 * just the name.
 */
public class SkypeMentionNormalizeMiddleware implements Middleware {
    /**
     * Fixes incorrect Skype mention text. This will change the text value for all
     * Skype mention entities.
     *
     * @param activity The Activity to correct.
     */
    public static void normalizeSkypeMentionText(Activity activity) {
        if (
            StringUtils.equals(activity.getChannelId(), Channels.SKYPE)
                && StringUtils.equals(activity.getType(), ActivityTypes.MESSAGE)
        ) {

            for (Entity entity : activity.getEntities()) {
                if (StringUtils.equals(entity.getType(), "mention")) {
                    String text = entity.getProperties().get("text").asText();
                    int closingBracket = text.indexOf(">");
                    if (closingBracket != -1) {
                        int openingBracket = text.indexOf("<", closingBracket);
                        if (openingBracket != -1) {
                            String mention = text.substring(closingBracket + 1, openingBracket)
                                .trim();

                            // create new JsonNode with new mention value
                            JsonNode node = JsonNodeFactory.instance.textNode(mention);
                            entity.setProperties("text", node);
                        }
                    }
                }
            }
        }
    }

    /**
     * Middleware implementation which corrects Entity.Mention.Text to a value
     * RemoveMentionText can work with.
     *
     * @param context The context object for this turn.
     * @param next    The delegate to call to continue the bot middleware pipeline.
     * @return
     */
    @Override
    public CompletableFuture<Void> onTurn(TurnContext context, NextDelegate next) {
        normalizeSkypeMentionText(context.getActivity());
        return next.next();
    }
}
