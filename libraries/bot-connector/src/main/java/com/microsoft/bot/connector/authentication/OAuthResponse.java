package com.microsoft.bot.connector.authentication;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.HashMap;

/// <remarks>
/// Member variables to this class follow the RFC Naming conventions
/// "properties" house any "extra" properties that aren't used at the moment.
/// </remarks>

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
    private DateTime expiration_time;
    public DateTime getExpirationTime() {
        return this.expiration_time;
    }
    public OAuthResponse withExpirationTime(DateTime expirationTime) {
        this.expiration_time = expirationTime;
        return this;
    }

    @JsonAnySetter
    private HashMap<String, String> properties;

}
