package com.microsoft.bot.builder;

public class CustomState implements StoreItem {
    private String _customString;
    private String _eTag;

    public String getCustomString() {
        return _customString;
    }

    public void setCustomString(String customString) {
        this._customString = customString;
    }

    public String getETag() {
        return _eTag;
    }

    public void setETag(String eTag) {
        this._eTag = eTag;
    }
}
