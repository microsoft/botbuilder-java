// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response schema sent back from Bot Framework Token Service required to
 * initiate a user single sign on.
 */
    public class TokenExchangeResource {

        @JsonProperty(value = "id")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String id;

        @JsonProperty(value = "uri")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String uri;

        @JsonProperty(value = "providerId")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private String providerId;

        /**
         * Initializes a new instance of the TokenExchangeResource class.
         */
        public TokenExchangeResource() {
            customInit();
        }

        /**
         * Initializes a new instance of the TokenExchangeResource class.
         * @param id The id to initialize this instance to.
         * @param uri The uri to initialize this instance to.
         * @param providerId the providerId to initialize this instance to.
         */
        public TokenExchangeResource(String id, String uri, String providerId) {
            this.id = id;
            this.uri = uri;
            this.providerId = providerId;
            customInit();
        }

        /**
         * An initialization method that performs custom operations like setting
         * defaults.
         */
        void customInit() {
        }

        /**
         * A unique identifier for this token exchange instance.
         * @return the Id value as a String.
         */
        public String getId() {
            return this.id;
        }

        /**
         * A unique identifier for this token exchange instance.
         * @param withId The Id value.
         */
        public void setId(String withId) {
            this.id = withId;
        }
        /**
         * The application D / resource identifier with which to exchange a token.
         * on behalf of
         * @return the Uri value as a String.
         */
        public String getUri() {
            return this.uri;
        }

        /**
         * The application D / resource identifier with which to exchange a token.
         * on behalf of
         * @param withUri The Uri value.
         */
        public void setUri(String withUri) {
            this.uri = withUri;
        }
        /**
         * The identifier of the provider with which to attempt a token exchange A
         * value of null or empty will default to Azure Active Directory.
         * @return the ProviderId value as a String.
         */
        public String getProviderId() {
            return this.providerId;
        }

        /**
         * The identifier of the provider with which to attempt a token exchange A
         * value of null or empty will default to Azure Active Directory.
         * @param withProviderId The ProviderId value.
         */
        public void setProviderId(String withProviderId) {
            this.providerId = withProviderId;
        }
    }
