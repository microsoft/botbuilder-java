// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector.base;

import java.util.Map;

public class NetworkCallRecord {
    public String Method;
    public String Uri;
    public String Body;

    public Map<String, String> Headers;
    public Map<String, String> Response;
}
