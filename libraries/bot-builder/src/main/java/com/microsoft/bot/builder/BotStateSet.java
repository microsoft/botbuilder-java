// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages a collection of botState and provides ability to load and save in
 * parallel.
 */
public class BotStateSet {
    /**
     * List of BotStates managed by this BotStateSet.
     */
    private List<BotState> botStates = new ArrayList<>();

    /**
     * Initializes a new instance of the BotStateSet class.
     *
     * @param withBotStates vArgs list of {@link BotState} objects to manage.
     */
    public BotStateSet(BotState... withBotStates) {
        this(Arrays.asList(withBotStates));
    }

    /**
     * Initializes a new instance of the BotStateSet class.
     *
     * @param withBotStates initial list of {@link BotState} objects to manage.
     */
    public BotStateSet(List<BotState> withBotStates) {
        botStates.addAll(withBotStates);
    }

    /**
     * Gets the BotStates list for the BotStateSet.
     *
     * @return The BotState objects managed by this class.
     */
    public List<BotState> getBotStates() {
        return botStates;
    }

    /**
     * Sets the BotStates list for the BotStateSet.
     *
     * @param withBotState The BotState objects managed by this class.
     */
    public void setBotStates(List<BotState> withBotState) {
        botStates = withBotState;
    }

    /**
     * Adds a bot state object to the set.
     *
     * @param botState The bot state object to add.
     * @return The updated BotStateSet, so you can fluently call add(BotState)
     *         multiple times.
     */
    public BotStateSet add(BotState botState) {
        if (botState == null) {
            throw new IllegalArgumentException("botState");
        }

        botStates.add(botState);
        return this;
    }

    /**
     * Load all BotState records in parallel.
     *
     * @param turnContext The TurnContext.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> loadAll(TurnContext turnContext) {
        return loadAll(turnContext, false);
    }

    /**
     * Load all BotState records in parallel.
     *
     * @param turnContext The TurnContext.
     * @param force       should data be forced into cache.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> loadAll(TurnContext turnContext, boolean force) {
        return CompletableFuture.allOf(
            botStates.stream().map(future -> future.load(turnContext, force)).toArray(CompletableFuture[]::new)
        );
    }

    /**
     * Save All BotState changes in parallel.
     *
     * @param turnContext The TurnContext.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> saveAllChanges(TurnContext turnContext) {
        return saveAllChanges(turnContext, false);
    }

    /**
     * Save All BotState changes in parallel.
     *
     * @param turnContext The TurnContext.
     * @param force       should data be forced to save even if no change were
     *                    detected.
     * @return A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> saveAllChanges(TurnContext turnContext, boolean force) {
        CompletableFuture<?>[] allSaves = botStates.stream()
            .map(botState -> botState.saveChanges(turnContext, force))
            .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(allSaves);
    }
}
