// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class StorageBaseTests {
    protected void readUnknownTest(Storage storage) {
        Map<String, Object> result = storage.read(new String[]{ "unknown"}).join();
        Assert.assertNotNull("result should not be null", result);
        Assert.assertNull("\"unknown\" key should have returned no value", result.get("unknown"));
    }

    protected void createObjectTest(Storage storage) {
        Map<String, Object> storeItems = new HashMap<String, Object>() {{
            put("createPoco", new PocoItem("1"));
            put("createPocoStoreItem", new PocoStoreItem("1"));
        }};

        storage.write(storeItems).join();

        Map<String, Object> readStoreItems = storage.read(storeItems.keySet().toArray(new String[storeItems.size()])).join();

        Assert.assertTrue(readStoreItems.get("createPoco") instanceof PocoItem);
        Assert.assertTrue(readStoreItems.get("createPocoStoreItem") instanceof PocoStoreItem);

        PocoItem createPoco = (PocoItem) readStoreItems.get("createPoco");

        Assert.assertNotNull("createPoco should not be null", createPoco);
        Assert.assertEquals("createPoco.id should be 1","1", createPoco.getId());

        PocoStoreItem createPocoStoreItem = (PocoStoreItem) readStoreItems.get("createPocoStoreItem");

        Assert.assertNotNull("createPocoStoreItem should not be null", createPocoStoreItem);
        Assert.assertEquals("createPocoStoreItem.id should be 1","1", createPocoStoreItem.getId());
        Assert.assertNotNull("createPocoStoreItem.eTag  should not be null", createPocoStoreItem.getETag());
    }

    protected void handleCrazyKeys(Storage storage) {
        String key = "!@#$%^&*()~/\\><,.?';\"`~";
        PocoStoreItem storeItem = new PocoStoreItem("1");
        Map<String, Object> dict = new HashMap<String, Object>() {{
           put(key, storeItem);
        }};

        storage.write(dict).join();
        Map<String, Object> storeItems = storage.read(new String[]{ key }).join();

        PocoStoreItem pocoStoreItem = (PocoStoreItem) storeItems.get(key);

        Assert.assertNotNull(pocoStoreItem);
        Assert.assertEquals("1", pocoStoreItem.getId());
    }

    protected void updateObjectTest(Storage storage) {
        Map<String, Object> dict = new HashMap<String, Object>() {{
            put("pocoItem", new PocoItem("1", 1));
            put("pocoStoreItem", new PocoStoreItem("1", 1));
        }};

        storage.write(dict).join();
        Map<String, Object> loadedStoreItems = storage.read(new String[] { "pocoItem", "pocoStoreItem" }).join();

        PocoItem updatePocoItem = (PocoItem) loadedStoreItems.get("pocoItem");
        PocoStoreItem updatePocoStoreItem = (PocoStoreItem) loadedStoreItems.get("pocoStoreItem");
        Assert.assertNotNull("updatePocoStoreItem.eTag  should not be null", updatePocoStoreItem.getETag());

        // 2nd write should work, because we have new etag, or no etag
        updatePocoItem.setCount(updatePocoItem.getCount() + 1);
        updatePocoStoreItem.setCount(updatePocoStoreItem.getCount() + 1);

        storage.write(loadedStoreItems).join();

        Map<String, Object> reloadedStoreItems = storage.read(new String[] { "pocoItem", "pocoStoreItem" }).join();

        PocoItem reloeadedUpdatePocoItem = (PocoItem) reloadedStoreItems.get("pocoItem");
        PocoStoreItem reloadedUpdatePocoStoreItem = (PocoStoreItem) reloadedStoreItems.get("pocoStoreItem");

        Assert.assertNotNull("reloadedUpdatePocoStoreItem.eTag  should not be null", reloadedUpdatePocoStoreItem.getETag());
        Assert.assertNotEquals("updatePocoItem.eTag  should be different", updatePocoStoreItem.getETag(), reloadedUpdatePocoStoreItem.getETag());
        Assert.assertEquals("reloeadedUpdatePocoItem.Count should be 2", 2, reloeadedUpdatePocoItem.getCount());
        Assert.assertEquals("reloadedUpdatePocoStoreItem.Count should be 2", 2, reloadedUpdatePocoStoreItem.getCount());

        try {
            updatePocoItem.setCount(123);

            storage.write(new HashMap<String, Object>() {{
                put("pocoItem", updatePocoItem);
            }}).join();
        } catch(Throwable t) {
            Assert.fail("Should not throw exception on write with pocoItem");
        }

        try {
            updatePocoStoreItem.setCount(123);

            storage.write(new HashMap<String, Object>() {{
                put("pocoStoreItem", updatePocoStoreItem);
            }}).join();

            Assert.fail("Should have thrown exception on write with store item because of old etag");
        } catch(Throwable t) {

        }

        Map<String, Object> reloadedStoreItems2 = storage.read(new String[] { "pocoItem", "pocoStoreItem" }).join();

        PocoItem reloadedPocoItem2 = (PocoItem) reloadedStoreItems2.get("pocoItem");
        PocoStoreItem reloadedPocoStoreItem2 = (PocoStoreItem) reloadedStoreItems2.get("pocoStoreItem");

        Assert.assertEquals(123, reloadedPocoItem2.getCount());
        Assert.assertEquals(2, reloadedPocoStoreItem2.getCount());

        // write with wildcard etag should work
        reloadedPocoItem2.setCount(100);
        reloadedPocoStoreItem2.setCount(100);
        reloadedPocoStoreItem2.setETag("*");

        storage.write(new HashMap<String, Object>() {{
            put("pocoItem", reloadedPocoItem2);
            put("pocoStoreItem", reloadedPocoStoreItem2);
        }}).join();

        Map<String, Object> reloadedStoreItems3 = storage.read(new String[] { "pocoItem", "pocoStoreItem" }).join();

        Assert.assertEquals(100, ((PocoItem) reloadedStoreItems3.get("pocoItem")).getCount());
        Assert.assertEquals(100, ((PocoStoreItem) reloadedStoreItems3.get("pocoStoreItem")).getCount());

        // write with empty etag should not work
        try {
            PocoStoreItem reloadedStoreItem4 = (PocoStoreItem) storage.read(new String[] { "pocoItem", "pocoStoreItem" }).join().get("pocoStoreItem");

            reloadedStoreItem4.setETag("");

            storage.write(new HashMap<String, Object>() {{
                put("pocoStoreItem", reloadedStoreItem4);
            }}).join();

            Assert.fail("Should have thrown exception on write with storeitem because of empty etag");
        } catch(Throwable t) {

        }

        Map<String, Object> finalStoreItems = storage.read(new String[] { "pocoItem", "pocoStoreItem" }).join();
        Assert.assertEquals(100, ((PocoItem) finalStoreItems.get("pocoItem")).getCount());
        Assert.assertEquals(100, ((PocoStoreItem) finalStoreItems.get("pocoStoreItem")).getCount());
    }

    protected void deleteObjectTest(Storage storage) {
        Map<String, Object> dict = new HashMap<String, Object>() {{
            put("delete1", new PocoStoreItem("1", 1));
        }};

        storage.write(dict).join();

        Map<String, Object> storeItems = storage.read(new String[]{ "delete1"}).join();
        PocoStoreItem storeItem = (PocoStoreItem) storeItems.get("delete1");

        Assert.assertNotNull("etag should be set", storeItem.getETag());
        Assert.assertEquals(1, storeItem.getCount());

        storage.delete(new String[]{"delete1"}).join();

        Map<String, Object> reloadedStoreItems = storage.read(new String[]{ "delete1"}).join();
        Assert.assertEquals("no store item should have been found because it was deleted", 0, reloadedStoreItems.size());
    }

    protected void deleteUnknownObjectTest(Storage storage) {
        storage.delete(new String[]{"unknown_key"}).join();
    }

    private static class PocoItem {
        public PocoItem() {

        }

        public PocoItem(String withId) {
            id = withId;
        }

        public PocoItem(String withId, int withCount) {
            id = withId;
            count = withCount;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String[] getExtraBytes() {
            return extraBytes;
        }

        public void setExtraBytes(String[] extraBytes) {
            this.extraBytes = extraBytes;
        }

        private String id;
        private int count;
        private String[] extraBytes;
    }

    private static class PocoStoreItem implements StoreItem {
        private String id;
        private int count;
        private String eTag;

        public PocoStoreItem() {

        }

        public PocoStoreItem(String withId) {
            id = withId;
        }

        public PocoStoreItem(String withId, int withCount) {
            id = withId;
            count = withCount;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String getETag() {
            return eTag;
        }

        @Override
        public void setETag(String withETag) {
            eTag = withETag;
        }
    }
}
