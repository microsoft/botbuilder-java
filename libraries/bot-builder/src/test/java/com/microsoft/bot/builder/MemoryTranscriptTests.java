// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.junit.Test;

public class MemoryTranscriptTests extends TranscriptBaseTests {
    public MemoryTranscriptTests() {
        store = new MemoryTranscriptStore();
    }

    @Test
    public void MemoryTranscript_BadArgs() {
        super.BadArgs();
    }

    @Test
    public void MemoryTranscript_LogActivity() {
        super.LogActivity();
    }

    @Test
    public void MemoryTranscript_LogMultipleActivities() {
        super.LogMultipleActivities();
    }

    @Test
    public void MemoryTranscript_GetConversationActivities() {
        super.GetTranscriptActivities();
    }

    @Test
    public void MemoryTranscript_GetConversationActivitiesStartDate() {
        super.GetTranscriptActivitiesStartDate();
    }

    @Test
    public void MemoryTranscript_ListConversations() {
        super.ListTranscripts();
    }

    @Test
    public void MemoryTranscript_DeleteConversation() {
        super.DeleteTranscript();
    }
}
