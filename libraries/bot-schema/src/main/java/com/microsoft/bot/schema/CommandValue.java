// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The value field of a CommandActivity contains metadata related to a
 * command.An optional extensible data payload may be included if defined by
 * the command activity name.
 * @param <T> The type of the CommandValue.
 */
public class CommandValue<T> {

    @JsonProperty(value = "commandId")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String commandId;

    @JsonProperty(value = "data")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private T data;

    /**
     * Gets the id of the command.
     * @return the CommandId value as a String.
     */
    public String getCommandId() {
        return this.commandId;
    }

    /**
     * Sets the id of the command.
     * @param withCommandId The CommandId value.
     */
    public void setCommandId(String withCommandId) {
        this.commandId = withCommandId;
    }

    /**
     * Gets the data field containing optional parameters specific to
     * this command activity, as defined by the name. The value of the data
     * field is a complex type.
     * @return the Data value as a T.
     */
    public T getData() {
        return this.data;
    }

    /**
     * Sets the data field containing optional parameters specific to
     * this command activity, as defined by the name. The value of the data
     * field is a complex type.
     * @param withData The Data value.
     */
    public void setData(T withData) {
        this.data = withData;
    }

}
