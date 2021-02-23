// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.ai.qna;

import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;

public class QnAMakerCardEqualityComparer {

    public Boolean Equals(Activity x, Activity y) {
        if (x == null && y == null) {
            return true;
        }

        if(x == null || y == null) {
            return false;
        }

        if(x.isType(ActivityTypes.MESSAGE) && y.isType(ActivityTypes.MESSAGE)) {
            Activity activity1 = x;
            Activity activity2 = y;

            if (activity1 == null || activity2 == null) {
                return false;
            }

            // Check for attachments
            if (activity1.getAttachments() != null && activity2.getAttachments() != null) {
                if(activity1.getAttachments().size() != activity2.getAttachments().size()) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public Integer getHashCode(Activity obj) {
        return obj.getId().hashCode();
    }

}
