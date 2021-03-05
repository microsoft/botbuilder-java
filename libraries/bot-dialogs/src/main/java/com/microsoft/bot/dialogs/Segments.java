package com.microsoft.bot.dialogs;

import java.util.ArrayList;

/**
 * Generic Arraylist of Object.
 */
class Segments extends ArrayList<Object> {

    /**
     * Returns the first item in the collection.
     *
     * @return the first object.
     */
    public Object first() {
        return get(0);
    }

    /**
     * Returns the last item in the collection.
     *
     * @return the last object.
     */
    public Object last() {
        return get(size() - 1);
    }

    /**
     * Gets the SegmentType at the specified index.
     *
     * @param index Index of the requested segment.
     * @return The SegmentType of item at the requested index.
     */
    public SegmentType getSegment(int index) {
        return new SegmentType(get(index));
    }
}
