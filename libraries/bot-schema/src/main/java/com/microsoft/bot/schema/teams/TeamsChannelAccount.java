// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema.teams;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamsChannelAccount {
    @JsonProperty(value = "givenName")
    private String givenName;

    @JsonProperty(value = "surname")
    private String surname;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "userPrincipalName")
    private String userPrincipalName;

    @JsonProperty(value = "objectId")
    private String aadObjectId;

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String withGivenName) {
        givenName = withGivenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String withSurname) {
        surname = withSurname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String withEmail) {
        email = withEmail;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String withUserPrincipalName) {
        userPrincipalName = withUserPrincipalName;
    }

    public String getAadObjectId() {
        return aadObjectId;
    }

    public void setAadObjectId(String withAadObjectId) {
        aadObjectId = withAadObjectId;
    }
}
