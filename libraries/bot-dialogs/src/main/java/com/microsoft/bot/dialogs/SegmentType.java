package com.microsoft.bot.dialogs;

/**
 * A class wraps an Object and can assist in determining if it's an integer.
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
class SegmentType {

    public boolean isInt;
    public int intValue;
    public Segments segmentsValue;
    public String stringValue;

    /**
     * @param value The object to create a SegmentType for.
     */
    SegmentType(Object value) {
        try {
            intValue = Integer.parseInt((String) value);
            isInt = true;
        } catch (NumberFormatException e) {
            isInt = false;
        }

        if (!isInt) {
            if (value instanceof Segments) {
                segmentsValue = (Segments) value;
            } else {
                stringValue = (String) value;
            }
        }
    }
}
