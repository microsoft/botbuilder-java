// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import java.util.concurrent.CompletionException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TranscriptBaseTests {
    protected TranscriptStore store;
    private ObjectMapper mapper;

    protected TranscriptBaseTests() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
    }

    protected void BadArgs() {
        try {
            store.logActivity(null).join();
            Assert.fail("logActivity Should have thrown on null");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        } catch (Throwable t) {
            Assert.fail("logActivity Should have thrown ArgumentNull exception on null");
        }

        try {
            store.getTranscriptActivities(null, null).join();
            Assert.fail("getTranscriptActivities Should have thrown on null");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        } catch (Throwable t) {
            Assert.fail(
                "getTranscriptActivities Should have thrown ArgumentNull exception on null"
            );
        }

        try {
            store.getTranscriptActivities("asdfds", null).join();
            Assert.fail("getTranscriptActivities Should have thrown on null");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        } catch (Throwable t) {
            Assert.fail(
                "getTranscriptActivities Should have thrown ArgumentNull exception on null"
            );
        }

        try {
            store.listTranscripts(null).join();
            Assert.fail("listTranscripts Should have thrown on null");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        } catch (Throwable t) {
            Assert.fail("listTranscripts Should have thrown ArgumentNull exception on null");
        }

        try {
            store.deleteTranscript(null, null).join();
            Assert.fail("deleteTranscript Should have thrown on null channelId");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        } catch (Throwable t) {
            Assert.fail(
                "deleteTranscript Should have thrown ArgumentNull exception on null channelId"
            );
        }

        try {
            store.deleteTranscript("test", null).join();
            Assert.fail("deleteTranscript Should have thrown on null conversationId");
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        } catch (Throwable t) {
            Assert.fail(
                "deleteTranscript Should have thrown ArgumentNull exception on null conversationId"
            );
        }
    }

    protected void LogActivity() {
        String conversationId = "_LogActivity";
        List<Activity> activities = createActivities(
            conversationId,
            OffsetDateTime.now(ZoneId.of("UTC"))
        );
        Activity activity = activities.get(0);
        store.logActivity(activity).join();

        PagedResult<Activity> results = store.getTranscriptActivities(
            "test",
            conversationId
        ).join();
        Assert.assertEquals(1, results.getItems().size());

        String src;
        String transcript;
        try {
            src = mapper.writeValueAsString(activity);
            transcript = mapper.writeValueAsString(results.getItems().get(0));
        } catch (Throwable t) {
            Assert.fail("Throw during json compare");
            return;
        }

        Assert.assertEquals(src, transcript);
    }

    protected void LogMultipleActivities() {
        String conversationId = "LogMultipleActivities";
        OffsetDateTime start = OffsetDateTime.now(ZoneId.of("UTC"));
        List<Activity> activities = createActivities(conversationId, start);

        for (Activity activity : activities) {
            store.logActivity(activity).join();
        }

        // make sure other channels and conversations don't return results
        PagedResult<Activity> pagedResult = store.getTranscriptActivities(
            "bogus",
            conversationId
        ).join();
        Assert.assertNull(pagedResult.getContinuationToken());
        Assert.assertEquals(0, pagedResult.getItems().size());

        // make sure other channels and conversations don't return results
        pagedResult = store.getTranscriptActivities("test", "bogus").join();
        Assert.assertNull(pagedResult.getContinuationToken());
        Assert.assertEquals(0, pagedResult.getItems().size());

        // make sure the original and transcript result activities are the same
        int indexActivity = 0;
        for (Activity result : pagedResult.getItems().stream().sorted(
            Comparator.comparing(Activity::getTimestamp)
        ).collect(Collectors.toList())) {
            String src;
            String transcript;
            try {
                src = mapper.writeValueAsString(activities.get(indexActivity++));
                transcript = mapper.writeValueAsString(result);
            } catch (Throwable t) {
                Assert.fail("Throw during json compare");
                return;
            }

            Assert.assertEquals(src, transcript);
        }

        pagedResult = store.getTranscriptActivities(
            "test",
            conversationId,
            null,
            start.plusMinutes(5)
        ).join();
        Assert.assertEquals(activities.size() / 2, pagedResult.getItems().size());

        // make sure the original and transcript result activities are the same
        indexActivity = 5;
        for (Activity result : pagedResult.getItems().stream().sorted(
            Comparator.comparing(Activity::getTimestamp)
        ).collect(Collectors.toList())) {
            String src;
            String transcript;
            try {
                src = mapper.writeValueAsString(activities.get(indexActivity++));
                transcript = mapper.writeValueAsString(result);
            } catch (Throwable t) {
                Assert.fail("Throw during json compare");
                return;
            }

            Assert.assertEquals(src, transcript);
        }
    }

    protected void DeleteTranscript() {
        String conversationId = "_DeleteConversation";
        OffsetDateTime start = OffsetDateTime.now(ZoneId.of("UTC"));
        List<Activity> activities = createActivities(conversationId, start);
        activities.forEach(a -> store.logActivity(a).join());

        String conversationId2 = "_DeleteConversation2";
        start = OffsetDateTime.now(ZoneId.of("UTC"));
        List<Activity> activities2 = createActivities(conversationId2, start);
        activities2.forEach(a -> store.logActivity(a).join());

        PagedResult<Activity> pagedResult = store.getTranscriptActivities(
            "test",
            conversationId
        ).join();
        PagedResult<Activity> pagedResult2 = store.getTranscriptActivities(
            "test",
            conversationId2
        ).join();

        Assert.assertEquals(activities.size(), pagedResult.getItems().size());
        Assert.assertEquals(activities2.size(), pagedResult2.getItems().size());

        store.deleteTranscript("test", conversationId).join();

        pagedResult = store.getTranscriptActivities("test", conversationId).join();
        pagedResult2 = store.getTranscriptActivities("test", conversationId2).join();

        Assert.assertEquals(0, pagedResult.getItems().size());
        Assert.assertEquals(activities.size(), pagedResult2.getItems().size());
    }

    protected void GetTranscriptActivities() {
        String conversationId = "_GetConversationActivitiesPaging";
        OffsetDateTime start = OffsetDateTime.now(ZoneId.of("UTC"));
        List<Activity> activities = createActivities(conversationId, start, 50);

        // log in parallel batches of 10
        int[] pos = new int[] { 0 };
        for (List<Activity> group : activities.stream().collect(
            Collectors.groupingBy(a -> pos[0]++ / 10)
        ).values()) {
            group.stream().map(a -> store.logActivity(a)).collect(
                CompletableFutures.toFutureList()
            ).join();
        }

        Set<String> seen = new HashSet<>();
        PagedResult<Activity> pagedResult = null;
        int pageSize = 0;
        do {
            pagedResult = store.getTranscriptActivities(
                "test",
                conversationId,
                pagedResult != null ? pagedResult.getContinuationToken() : null
            ).join();
            Assert.assertNotNull(pagedResult);
            Assert.assertNotNull(pagedResult.getItems());

            // NOTE: Assumes page size is consistent
            if (pageSize == 0) {
                pageSize = pagedResult.getItems().size();
            } else if (pageSize == pagedResult.getItems().size()) {
                Assert.assertTrue(!StringUtils.isEmpty(pagedResult.getContinuationToken()));
            }

            for (Activity item : pagedResult.getItems()) {
                Assert.assertFalse(seen.contains(item.getId()));
                seen.add(item.getId());
            }
        } while (pagedResult.getContinuationToken() != null);

        Assert.assertEquals(activities.size(), seen.size());
    }

    protected void GetTranscriptActivitiesStartDate() {
        String conversationId = "_GetConversationActivitiesStartDate";
        OffsetDateTime start = OffsetDateTime.now(ZoneId.of("UTC"));
        List<Activity> activities = createActivities(conversationId, start, 50);

        // log in parallel batches of 10
        int[] pos = new int[] { 0 };
        for (List<Activity> group : activities.stream().collect(
            Collectors.groupingBy(a -> pos[0]++ / 10)
        ).values()) {
            group.stream().map(a -> store.logActivity(a)).collect(
                CompletableFutures.toFutureList()
            ).join();
        }

        Set<String> seen = new HashSet<>();
        OffsetDateTime startDate = start.plusMinutes(50);
        PagedResult<Activity> pagedResult = null;
        int pageSize = 0;
        do {
            pagedResult = store.getTranscriptActivities(
                "test",
                conversationId,
                pagedResult != null ? pagedResult.getContinuationToken() : null,
                startDate
            ).join();
            Assert.assertNotNull(pagedResult);
            Assert.assertNotNull(pagedResult.getItems());

            // NOTE: Assumes page size is consistent
            if (pageSize == 0) {
                pageSize = pagedResult.getItems().size();
            } else if (pageSize == pagedResult.getItems().size()) {
                Assert.assertTrue(!StringUtils.isEmpty(pagedResult.getContinuationToken()));
            }

            for (Activity item : pagedResult.getItems()) {
                Assert.assertFalse(seen.contains(item.getId()));
                seen.add(item.getId());
            }
        } while (pagedResult.getContinuationToken() != null);

        Assert.assertEquals(activities.size() / 2, seen.size());

        for (Activity a : activities.stream().filter(
            a -> a.getTimestamp().compareTo(startDate) >= 0
        ).collect(Collectors.toList())) {
            Assert.assertTrue(seen.contains(a.getId()));
        }

        for (Activity a : activities.stream().filter(
            a -> a.getTimestamp().compareTo(startDate) < 0
        ).collect(Collectors.toList())) {
            Assert.assertFalse(seen.contains(a.getId()));
        }
    }

    protected void ListTranscripts() {
        OffsetDateTime start = OffsetDateTime.now(ZoneId.of("UTC"));

        List<String> conversationIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            conversationIds.add("_ListConversations" + i);
        }

        List<Activity> activities = new ArrayList<>();
        for (String conversationId : conversationIds) {
            activities.addAll(createActivities(conversationId, start, 1));
        }

        // log in parallel batches of 10
        int[] pos = new int[] { 0 };
        for (List<Activity> group : activities.stream().collect(
            Collectors.groupingBy(a -> pos[0]++ / 10)
        ).values()) {
            group.stream().map(a -> store.logActivity(a)).collect(
                CompletableFutures.toFutureList()
            ).join();
        }

        Set<String> seen = new HashSet<>();
        PagedResult<TranscriptInfo> pagedResult = null;
        int pageSize = 0;
        do {
            pagedResult = store.listTranscripts(
                "test",
                pagedResult != null ? pagedResult.getContinuationToken() : null
            ).join();
            Assert.assertNotNull(pagedResult);
            Assert.assertNotNull(pagedResult.getItems());

            // NOTE: Assumes page size is consistent
            if (pageSize == 0) {
                pageSize = pagedResult.getItems().size();
            } else if (pageSize == pagedResult.getItems().size()) {
                Assert.assertTrue(!StringUtils.isEmpty(pagedResult.getContinuationToken()));
            }

            for (TranscriptInfo item : pagedResult.getItems()) {
                Assert.assertFalse(seen.contains(item.getId()));
                seen.add(item.getId());
            }
        } while (pagedResult.getContinuationToken() != null);

        Assert.assertEquals(conversationIds.size(), seen.size());
        Assert.assertTrue(conversationIds.stream().allMatch(seen::contains));
    }

    private List<Activity> createActivities(String conversationId, OffsetDateTime ts) {
        return createActivities(conversationId, ts, 5);
    }

    private List<Activity> createActivities(String conversationId, OffsetDateTime ts, int count) {
        List<Activity> activities = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Activity activity = new Activity(ActivityTypes.MESSAGE);
            activity.setTimestamp(ts);
            activity.setId(UUID.randomUUID().toString());
            activity.setText(Integer.toString(i));
            activity.setChannelId("test");
            activity.setFrom(new ChannelAccount("User" + i));
            activity.setConversation(new ConversationAccount(conversationId));
            activity.setRecipient(new ChannelAccount("Bot1", "2"));
            activity.setServiceUrl("http://foo.com/api/messages");
            activities.add(activity);
            ts = ts.plusMinutes(1);

            activity = new Activity(ActivityTypes.MESSAGE);
            activity.setTimestamp(ts);
            activity.setId(UUID.randomUUID().toString());
            activity.setText(Integer.toString(i));
            activity.setChannelId("test");
            activity.setFrom(new ChannelAccount("Bot1", "2"));
            activity.setConversation(new ConversationAccount(conversationId));
            activity.setRecipient(new ChannelAccount("User" + i));
            activity.setServiceUrl("http://foo.com/api/messages");
            activities.add(activity);
            ts = ts.plusMinutes(1);
        }
        ;

        return activities;
    }
}
