package com.microsoft.bot.connector.authentication;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 *
 * Member variables to this class follow the RFC Naming conventions
 * "properties" house any "extra" properties that aren't used at the moment.
 *
 */

public class OAuthResponse
{
    @JsonProperty
    private String token_type;
    public String getTokenType() {
        return this.token_type;
    }
    @JsonProperty
    private int expires_in;
    public int getExpiresIn() {
        return this.expires_in;
    }
    @JsonProperty
    private String access_token;
    public String getAccessToken() {
        return this.access_token;
    }
    @JsonProperty
    private LocalDateTime expiration_time;
    public LocalDateTime getExpirationTime() {
        return this.expiration_time;
    }
    public OAuthResponse withExpirationTime(LocalDateTime expirationTime) {
        this.expiration_time = expirationTime;
        return this;
    }

    @JsonAnySetter
    public HashMap<String, String> properties;

}
