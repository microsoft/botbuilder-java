package com.microsoft.bot.connector.customizations;

public class BotCredentials {
    protected String appId;
    protected String appPassword;

    public String appId() { return this.appId; }

    public String appPassword() { return this.appPassword; }

    public BotCredentials withAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public BotCredentials withAppPassword(String appPassword) {
        this.appPassword = appPassword;
        return this;
    }
}
