// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.microsoft.bot.connector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Provides a common Executor for Future operations.
 */
public final class ExecutorFactory {
    private ExecutorFactory() {

    }

    private static ForkJoinWorkerThreadFactory factory = new ForkJoinWorkerThreadFactory() {
        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread worker =
                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("Bot-" + worker.getPoolIndex());
            return worker;
        }
    };

    private static ExecutorService executor =
        new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2, factory, null, false);

    /**
     * Provides an SDK wide ExecutorService for async calls.
     * 
     * @return An ExecutorService.
     */
    public static ExecutorService getExecutor() {
        return executor;
    }
}
