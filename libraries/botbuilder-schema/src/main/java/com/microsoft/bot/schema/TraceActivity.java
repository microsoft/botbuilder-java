package com.microsoft.bot.schema;

import com.microsoft.bot.schema.models.Activity;
import com.microsoft.bot.schema.models.ConversationReference;

/// <summary>
/// An activity by which a bot can log internal information into a logged conversation transcript
/// </summary>
public class TraceActivity extends ActivityImpl {
    /// <summary>
    /// Name of the trace activity
    /// </summary>
    private String _name;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /// <summary>
    /// Descriptive label for the trace
    /// </summary>
    private String _label;

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        this._label = label;
    }

    /// <summary>
    /// Unique string which identifies the format of the value object
    /// </summary>
    private String _value_type;

    public String getValueType() {
        return _value_type;
    }

    public void setValueType(String value_type) {
        _value_type = value_type;
    }

    /// <summary>
    /// Open-ended value
    /// </summary>
    private Object _value;

    Object getValue() {
        return _value;
    }

    void setValue(Object value) {
        _value = value;
    }

    /// <summary>
    /// Reference to another conversation or activity
    /// </summary>
    private ConversationReference _relates_to;

    ConversationReference getRelatesTo() {
        return _relates_to;
    }

    void setRelatesTo(ConversationReference relates_to) {
        _relates_to = relates_to;
    }
}
