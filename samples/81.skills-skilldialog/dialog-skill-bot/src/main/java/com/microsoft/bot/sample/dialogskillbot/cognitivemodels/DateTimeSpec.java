// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.sample.dialogskillbot.cognitivemodels;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents the built-in LUS date-time type.
 *
 * LUS recognizes time expressions like "next monday" and converts those to a
 * type and set of timex expressions.More information on timex can be found
 * here:
 * http://www.gettimeml().org/publications/timeMLdocs/timeml_1.get2().get1().h
 * ml#timex3.More information on the library which does the recognition can be
 * found here: https://github.com/Microsoft/Recognizers-Text.
 */
public class DateTimeSpec {

    @JsonProperty(value = "type")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String type;

    @JsonProperty(value = "timex")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> expressions;

    /**
     * Initializes a new instance of the {@link DateTimeSpec} class.
     *
     * @param type         The type of TMEX expression.
     * @param expressions  The TMEX expression.
     */
    public DateTimeSpec(String type, List<String> expressions) {
        if (StringUtils.isEmpty(type)) {
            throw new  IllegalArgumentException("type cannot be null.");
        }

        if (expressions == null) {
            throw new  IllegalArgumentException("expressions cannot be null.");
        }

        this.type = type;
        this.expressions = expressions;
    }

    /**
     */
    @Override
    public String toString() {
        return String.format("DateTimeSpec(%s, [%s]", getType(), String.join(", ", getExpressions()));
    }
    /**
     * Gets type of expression.
     * Example types include: time -- simple time expression like "3pm".
     * @return the Type value as a String.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets Timex expressions.
     * @return the Expressions value as a ReadOnlyList<String>.
     */
    public List<String> getExpressions() {
        return this.expressions;
    }

}
