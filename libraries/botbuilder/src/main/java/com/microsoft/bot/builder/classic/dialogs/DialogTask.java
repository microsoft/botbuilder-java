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
package com.microsoft.bot.builder.classic.dialogs;
/// <summary>
/// The stack of dialogs in the conversational process.
/// </summary>
// TODO: daveta
// public interface IDialogTask extends IDialogStack, IEventLoop, IEventProducer<IActivity>
public interface DialogTask
{
}

/*
public static partial class Extensions
{
    /// <summary>
    /// Interrupt the waiting dialog with a new dialog
    /// </summary>
    /// <typeparam name="T">The type of result expected from the dialog.</typeparam>
    /// <typeparam name="R">The type of the item posted to dialog.</typeparam>
    /// <param name="task">The dialog task.</param>
    /// <param name="dialog">The new interrupting dialog.</param>
    /// <param name="item">The item to forward to the new interrupting dialog.</param>
    /// <param name="token">The cancellation token.</param>
    /// <returns>A task that represents the interruption operation.</returns>
    public static async Task InterruptAsync<T, R>(this IDialogTask task, IDialog<T> dialog, R item, CancellationToken token)
    {
        await task.Forward(dialog.Void<T, R>(), null, item, token);
        await task.PollAsync(token);
    }
}
*/