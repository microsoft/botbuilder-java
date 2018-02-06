package com.microsoft.bot.connector.customizations;

import java.util.Map;

public interface ClaimsIdentity {
    boolean isAuthenticated();
    Map<String, String> claims();
    String getIssuer();
}
