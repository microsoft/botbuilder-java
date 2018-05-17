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

import java.util.concurrent.CompletableFuture;

/// <summary>
/// Private bot data.
/// </summary>
public interface BotData
{
    /// <summary>
    /// Private bot data associated with a user (across all channels and conversations).
    /// </summary>
    BotDataBag getUserData();

    /// <summary>
    /// Private bot data associated with a conversation.
    /// </summary>
    BotDataBag getConversationData();

    /// <summary>
    /// Private bot data associated with a user in a conversation.
    /// </summary>
    BotDataBag getPrivateConversationData();

    /// <summary>
    /// Loads the bot data from <see cref="IBotDataStore{T}"/>
    /// </summary>
    /// <param name="cancellationToken">The cancellation token.</param>
    // TODO: daveta cancellation token
    CompletableFuture LoadAsync();

    /// <summary>
    /// Flushes the bot data to <see cref="IBotDataStore{T}"/>
    /// </summary>
    /// <param name="cancellationToken">The cancellation token.</param>
    // TODO: Cancellation token
    CompletableFuture FlushAsync();
}
