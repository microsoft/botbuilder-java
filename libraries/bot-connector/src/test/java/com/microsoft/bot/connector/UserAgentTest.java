// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.connector;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class UserAgentTest {
    /**
     * BotBuilder/4.0.0-SNAPSHOT (JVM 1.8.0_172; Windows 10)
     * https://github.com/Microsoft/botbuilder-dotnet/blob/d342cd66d159a023ac435aec0fdf791f93118f5f/doc/UserAgents.md
     */
    @Test
    public void GetAgentString() {
        String userAgent = UserAgent.value();
        Assert.assertTrue(Pattern.matches("^BotBuilder/\\S*\\s\\(JVM.*$", userAgent));
        Assert.assertFalse(userAgent.contains("{"));
        Assert.assertFalse(userAgent.contains("}"));
    }

}
