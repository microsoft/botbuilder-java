/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An instance of this class provides Azure policy violation information.
 */
public class PolicyViolation extends TypedErrorInfo {
    /**
     * Policy violation error details.
     */
    private PolicyViolationErrorInfo policyErrorInfo;
    
    /**
     * Initializes a new instance of PolicyViolation.
     * @param type the error type
     * @param policyErrorInfo the error details
     * @throws JsonParseException if the policyErrorInfo has invalid content.
     * @throws JsonMappingException if the policyErrorInfo's JSON does not match the expected schema. 
     * @throws IOException if an IO error occurs.
     */
    public PolicyViolation(String type, ObjectNode policyErrorInfo) throws JsonParseException, JsonMappingException, IOException {
        super(type, policyErrorInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        this.policyErrorInfo = objectMapper.readValue(policyErrorInfo.toString(), PolicyViolationErrorInfo.class);
    }
    
    /**
     * @return the policy violation error details.
     */
    public PolicyViolationErrorInfo policyErrorInfo() {
        return policyErrorInfo;
    }
}