// 
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
// 
// Microsoft Bot Framework: http://botframework.com
// 
// Bot Builder SDK GitHub:
// https://github.com/Microsoft/botbuilder-java
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
package com.microsoft.bot.builder.dialogs;

import com.microsoft.bot.builder.classic.dialogs.BotData;
import com.microsoft.bot.builder.classic.dialogs.BotDataBag;
import com.microsoft.bot.builder.classic.dialogs.BotToUser;
import com.microsoft.bot.builder.classic.dialogs.DialogStack;
import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.MessageActivity;

import java.util.concurrent.CompletableFuture;

public final class DialogContextImpl implements DialogContext
{
    private final BotToUser botToUser;
    private final BotData botData;
    private final DialogStack stack;
    private final Activity activity;

    // TODO: daveta
    // public DialogContextImpl(BotToUser botToUser, BotData botData, DialogStack stack, Activity activity, CancellationToken token)
    public DialogContextImpl(BotToUser botToUser, BotData botData, DialogStack stack, Activity activity)
    {
        this.botToUser = botToUser;
        this.botData = botData;
        this.stack = stack;
        this.activity = activity;
        /*
        SetField.NotNull(out this.botToUser, nameof(botToUser), botToUser);
        SetField.NotNull(out this.botData, nameof(botData), botData);
        SetField.NotNull(out this.stack, nameof(stack), stack);
        SetField.NotNull(out this.activity, nameof(activity), activity);
        */
    }

    BotDataBag getConversationData() {

            return this.botData.getConversationData();
    }

    BotDataBag getPrivateConversationData() {
            return this.botData.getPrivateConversationData();
    }

    BotDataBag getUserData() {
            return this.botData.getUserData();
    }

    //CompletableFuture IBotToUser.PostAsync(MessageActivity message, CancellationToken cancellationToken)
    CompletableFuture PostAsync(MessageActivity message)
    {
        //this.botToUser.PostAsync(message, cancellationToken).join();
        return this.botToUser.PostAsync(message);
    }

    MessageActivity MakeMessage()
    {
        return this.botToUser.MakeMessage();
    }

    /*IReadOnlyList<Delegate> IDialogStack.Frames
    {
        get
        {
            return this.stack.Frames;
        }
    }

    void IDialogStack.Call<R>(IDialog<R> child, ResumeAfter<R> resume)
    {
        this.stack.Call<R>(child, resume);
    }

    void IDialogStack.Post<E>(E @event, ResumeAfter<E> resume)
    {
        this.stack.Post<E>(@event, resume);
    }

    async Task IDialogStack.Forward<R, T>(IDialog<R> child, ResumeAfter<R> resume, T item, CancellationToken token)
    {
        await this.stack.Forward<R, T>(child, resume, item, token);
    }

    void IDialogStack.Done<R>(R value)
    {
        this.stack.Done<R>(value);
    }

    void IDialogStack.Fail(Exception error)
    {
        this.stack.Fail(error);
    }

    void IDialogStack.Wait<R>(ResumeAfter<R> resume)
    {
        this.stack.Wait(resume);
    }

    void IDialogStack.Reset()
    {
        this.stack.Reset();
    }

    async Task IBotData.LoadAsync(CancellationToken cancellationToken)
    {
        await this.botData.LoadAsync(cancellationToken);
    }

    async Task IBotData.FlushAsync(CancellationToken cancellationToken)
    {
        await this.botData.FlushAsync(cancellationToken);
    }

    CancellationToken IBotContext.CancellationToken => this.token;

    IActivity IBotContext.Activity => this.activity;
    */
}
