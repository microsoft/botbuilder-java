package com.microsoft.bot.dialogs;

//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Bot Framework: http://botframework.com
//
// Bot Builder SDK GitHub:
// https://github.com/Microsoft/BotBuilder
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

/**
 * Encapsulates a method that represents the code to execute after a result is available.
 *
 * The result is often a message from the user.
 *
 * @param T The type of the result.
 * @param context The dialog context.
 * @param result The result.
 * @return A task that represents the code that will resume after the result is available.
 */

/*
public interface ResumeAfter
{
    CompletableFuture invoke(DialogContext contenxt, Available)
}

public delegate Task ResumeAfter<in T>(IDialogContext context, IAwaitable<T> result);
*/

/**
 * Encapsulate a method that represents the code to start a dialog.
 * @param context The dialog context.
 * @return A task that represents the start code for a dialog.
 */
//public delegate Task StartAsync(IDialogContext context);



/**
 * The context for the execution of a dialog's conversational process.
 */
// DAVETA: TODO
// public interface DialogContext extends
public interface DialogContext {
        }


/**
 * Helper methods.
 */
/*
public static partial class Extensions
{*/
    /**
     * Post a message to be sent to the user, using previous messages to establish a conversation context.
     *
     * If the locale parameter is not set, locale of the incoming message will be used for reply.
     *
     * @param botToUser Communication channel to use.
     * @param text The message text.
     * @param locale The locale of the text.
     * @return A task that represents the post operation.
     */
/*
    public static async Task PostAsync(this BotToUser botToUser, string text, string locale = null)
    {
        var message = botToUser.MakeMessage();
        message.Text = text;

        if (!string.IsNullOrEmpty(locale))
        {
            message.Locale = locale;
        }

        await botToUser.PostAsync(message);
    }
*/


    /**
     * Post a message and optional SSML to be sent to the user, using previous messages to establish a conversation context.
     *
     * If the locale parameter is not set, locale of the incoming message will be used for reply.
     *
     * @param botToUser Communication channel to use.
     * @param text The message text.
     * @param speak The SSML markup for text to speech.
     * @param options The options for the message.
     * @param locale The locale of the text.
     * @return A task that represents the post operation.
     */
   /* public static async Task SayAsync(this BotToUser botToUser, string text, string speak = null, MessageOptions options = null, string locale = null)
    {
        var message = botToUser.MakeMessage();

        message.Text = text;
        message.Speak = speak;

        if (!string.IsNullOrEmpty(locale))
        {
            message.Locale = locale;
        }

        if (options != null)
        {
            message.InputHint = options.InputHint;
            message.TextFormat = options.TextFormat;
            message.AttachmentLayout = options.AttachmentLayout;
            message.Attachments = options.Attachments;
            message.Entities = options.Entities;
        }

        await botToUser.PostAsync(message);
    }*/

    /**
     * Suspend the current dialog until the user has sent a message to the bot.
     * @param stack The dialog stack.
     * @param resume The method to resume when the message has been received.
     */
/*
    public static void Wait(this IDialogStack stack, ResumeAfter<MessageActivity> resume)
    {
        stack.Wait<MessageActivity>(resume);
    }
*/

    /**
     * Call a child dialog, add it to the top of the stack and post the message to the child dialog.
     * @param R The type of result expected from the child dialog.
     * @param stack The dialog stack.
     * @param child The child dialog.
     * @param resume The method to resume when the child dialog has completed.
     * @param message The message that will be posted to child dialog.
     * @return A task representing the Forward operation.
     */
/*    public static async Task Forward<R>(this IDialogStack stack, IDialog<R> child, ResumeAfter<R> resume, MessageActivity message)
    {
        await stack.Forward<R, MessageActivity>(child, resume, message, token);
    }
}*/
