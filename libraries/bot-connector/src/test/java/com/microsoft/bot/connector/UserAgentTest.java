package com.microsoft.bot.connector;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class UserAgentTest {
    /**
     * BotBuilder/4.0.0-SNAPSHOT (JVM 1.8.0_172; Windows 10) Middleware(MyMiddleware/v1.0.0) Storage(MyStorage/v1.10.0) LUIS(v1.0.0) Qna(v1.0.0)
     * https://github.com/Microsoft/botbuilder-dotnet/blob/d342cd66d159a023ac435aec0fdf791f93118f5f/doc/UserAgents.md
     */
    @Test
    public void GetAgentString() {
        String userAgent = UserAgent.value();
        Assert.assertTrue(Pattern.matches("^BotBuilder/\\S*\\s\\(JVM.*$", userAgent));
        Assert.assertFalse(userAgent.contains("{"));
        Assert.assertFalse(userAgent.contains("}"));
    }

    @Test
    public void AddMiddlewareComponent() {
        UserAgent.AddMiddlewareComponent("MyMiddleware", "v1.0.0");
        String userAgent = UserAgent.value();
        Assert.assertTrue(userAgent.contains("MyMiddleware"));
    }
    @Test
    public void AddMultipleMiddlewareComponent() {
        UserAgent.AddMiddlewareComponent("MyMiddleware", "v1.0.0");
        UserAgent.AddMiddlewareComponent("MyMiddleware1", "v1.0.0");
        UserAgent.AddMiddlewareComponent("MyMiddleware2", "v1.0.0");
        UserAgent.AddMiddlewareComponent("MyMiddleware3", "v1.0.0");
        String userAgent = UserAgent.value();
        Assert.assertTrue(userAgent.contains("MyMiddleware") );
        Assert.assertTrue(userAgent.contains("MyMiddleware1") );
        Assert.assertTrue(userAgent.contains("MyMiddleware2") );
        Assert.assertTrue(userAgent.contains("MyMiddleware3") );
    }

    @Test
    public void OverrideMultipleMiddlewareComponentVersion() {
        UserAgent.AddMiddlewareComponent("MyMiddleware", "v1.0.0");
        UserAgent.AddMiddlewareComponent("MyMiddleware", "v2.0.0");
        String userAgent = UserAgent.value();
        Assert.assertTrue(userAgent.contains("MyMiddleware/v2.0.0") );
        // Attempt override
        UserAgent.AddMiddlewareComponent("MyMiddleware", "v1.0.0");
        Assert.assertTrue(userAgent.contains("MyMiddleware/v2.0.0") );
    }
    @Test
    public void AddMiddlewareStorageLuisQna() {
        UserAgent.AddMiddlewareComponent("MyMiddleware", "v1.0.0");
        UserAgent.AddStorageComponent("MyStorage", "v1.10.0");
        UserAgent.SetLuisVersion("v1.0.0");
        UserAgent.SetQnaVersion("v1.0.0");
        String userAgent = UserAgent.value();
        Assert.assertTrue(userAgent.contains("MyMiddleware/v1.0.0"));
        Assert.assertTrue(userAgent.contains("MyStorage/v1.10.0"));
        Assert.assertTrue(userAgent.contains("LUIS(v1.0.0)"));
        Assert.assertTrue(userAgent.contains("Qna(v1.0.0)"));
    }

}
