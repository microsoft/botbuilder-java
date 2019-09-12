// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import org.junit.Before;
import org.junit.Test;

public class MemoryStorageTests extends StorageBaseTests {
    private Storage storage;

    @Before
    public void initialize() {
        storage = new MemoryStorage();
    }

    @Test
    public void MemoryStorage_CreateObjectTest() {
        createObjectTest(storage);
    }

    @Test
    public void MemoryStorage_ReadUnknownTest() {
        readUnknownTest(storage);
    }

    @Test
    public void MemoryStorage_UpdateObjectTest() {
        updateObjectTest(storage);
    }

    @Test
    public void MemoryStorage_DeleteObjectTest() {
        deleteObjectTest(storage);
    }

    @Test
    public void MemoryStorage_HandleCrazyKeys() {
        handleCrazyKeys(storage);
    }
}
