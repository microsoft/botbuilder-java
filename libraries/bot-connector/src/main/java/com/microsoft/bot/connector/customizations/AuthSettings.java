package com.microsoft.bot.connector.customizations;

public final class AuthSettings {
    public static final String REFRESH_ENDPOINT = "https://login.microsoftonline.com/botframework.com/oauth2/v2.0/token";
    public static final String REFRESH_SCOPE = "https://api.botframework.com/.default";
    public static final String BOT_CONNECTOR_OPEN_ID_METADATA = "https://login.botframework.com/v1/.well-known/openidconfiguration";
    public static final String BOT_CONNECTOR_ISSUER = "https://api.botframework.com";
    public static final String EMULATOR_OPEN_ID_METADATA = "https://login.microsoftonline.com/botframework.com/v2.0/.well-known/openid-configuration";
    public static final String EMULATOR_AUTH_V31_ISSUER_V1 = "https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/";
    public static final String EMULATOR_AUTH_V31_ISSUER_V2 = "https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0";
    public static final String EMULATOR_AUTH_V32_ISSUER_V1 = "https://sts.windows.net/f8cdef31-a31e-4b4a-93e4-5f571e91255a/";
    public static final String EMULATOR_AUTH_V32_ISSUER_V2 = "https://login.microsoftonline.com/f8cdef31-a31e-4b4a-93e4-5f571e91255a/v2.0";
}