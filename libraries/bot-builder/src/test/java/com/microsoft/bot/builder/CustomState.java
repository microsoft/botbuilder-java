package com.microsoft.bot.builder;

import com.microsoft.bot.builder.StoreItem;

public class CustomState implements StoreItem

{
    private String _customString;

    public String getCustomString() {
        return _customString;
    }

    public void setCustomString(String customString) {
        this._customString = customString;
    }

    private String _eTag;

    public String geteTag()

    {
        return _eTag;
    }

    public void seteTag(String eTag) {
        this._eTag = eTag;
    }
}
