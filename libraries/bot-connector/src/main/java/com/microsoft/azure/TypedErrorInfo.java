/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An instance of this class provides Azure error type and information.
 */
public class TypedErrorInfo {
    /**
     * The error type.
     */
    private String type;
    
    /**
     * The error information.
     */
    private ObjectNode info;
    
    /**
     * Initializes a new instance of TypedErrorInfo.
     * @param type the error type.
     * @param info the error information.
     */
    public TypedErrorInfo(String type, ObjectNode info) {
        this.type = type;
        this.info = info;
    }
    
    /**
     * @return the error type.
     */
    public String type() {
        return type;
    }
    
    /**
     * @return the error information.
     */
    public ObjectNode info() {
        return info;
    }
}