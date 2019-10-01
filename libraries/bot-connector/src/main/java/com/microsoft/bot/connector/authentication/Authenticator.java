// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector.authentication;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import java.util.concurrent.CompletableFuture;

public interface Authenticator {
    CompletableFuture<IAuthenticationResult> acquireToken();
}
