package com.microsoft.bot.schema;

import com.microsoft.bot.schema.models.ConversationReference;

/**
 * An activity by which a bot can log internal information into a logged conversation transcript
 */
public class TraceActivity extends ActivityImpl {
    /**
     * Name of the trace activity
     */
    private String _name;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * Descriptive label for the trace
     */
    private String _label;

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        this._label = label;
    }

    /**
     * Unique string which identifies the format of the value object
     */
    private String _value_type;

    public String getValueType() {
        return _value_type;
    }

    public void setValueType(String value_type) {
        _value_type = value_type;
    }

    /**
     * Open-ended value
     */
    private Object _value;

    Object getValue() {
        return _value;
    }

    void setValue(Object value) {
        _value = value;
    }

    /**
     * Reference to another conversation or activity
     */
    private ConversationReference _relates_to;

    ConversationReference getRelatesTo() {
        return _relates_to;
    }

    void setRelatesTo(ConversationReference relates_to) {
        _relates_to = relates_to;
    }
}
