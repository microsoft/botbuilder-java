package com.microsoft.bot.schema;

import com.microsoft.bot.schema.models.Activity;

public class ContactRelationUpdateActivity extends Activity {
    /// <summary>
    /// add|remove
    /// </summary>
    private String _action;

    public String getAction() {
        return _action;
    }

    public void setAction(String action) {
        this._action = action;
    }
}