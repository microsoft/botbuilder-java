package com.microsoft.bot.builder.core.base;

import java.util.Map;

public class NetworkCallRecord {
    public String Method;
    public String Uri;
    public String Body;

    public Map<String, String> Headers;
    public Map<String, String> Response;
}
