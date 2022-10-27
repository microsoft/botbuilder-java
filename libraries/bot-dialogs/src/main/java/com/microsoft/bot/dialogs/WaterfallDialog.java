// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.schema.ActivityTypes;

import org.apache.commons.lang3.StringUtils;

/**
 * Dialog optimized for prompting a user with a series of questions. Waterfalls
 * accept a stack of functions which will be executed in sequence. Each
 * waterfall step can ask a question of the user and the user's response will be
 * passed as an argument to the next waterfall step.
 */
public class WaterfallDialog extends Dialog {

    private static final String PERSISTED_OPTIONS = "options";
    private static final String PERSISTED_VALUES = "values";
    private static final String PERSISTED_INSTANCEID = "instanceId";
    private static final String STEP_INDEX = "stepIndex";

    private final List<WaterfallStep> steps;

    /**
     * Initializes a new instance of the {@link WaterfallDialog} class.
     *
     * @param dialogId The dialog ID.
     * @param actions  Optional actions to be defined by the caller.
     */
    public WaterfallDialog(String dialogId, List<WaterfallStep> actions) {
        super(dialogId);
        steps = actions != null ? actions : new ArrayList<WaterfallStep>();
    }

    /**
     * Gets a unique String which represents the version of this dialog. If the
     * version changes between turns the dialog system will emit a DialogChanged
     * event.
     *
     * @return Version will change when steps count changes (because dialog has no
     *         way of evaluating the content of the steps.
     */
    public String getVersion() {
        return String.format("%s:%d", getId(), steps.size());
    }

    /**
     * Adds a new step to the waterfall.
     *
     * @param step Step to add.
     * @return Waterfall dialog for fluent calls to `AddStep()`.
     */
    public WaterfallDialog addStep(WaterfallStep step) {
        if (step == null) {
            throw new IllegalArgumentException("step cannot be null");
        }
        steps.add(step);
        return this;
    }

    /**
     * Called when the waterfall dialog is started and pushed onto the dialog stack.
     *
     * @param dc      The
     * @param options Optional, initial information to pass to the dialog.
     * @return A CompletableFuture representing the asynchronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog.
     */
    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {

        if (dc == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "dc cannot be null."
            ));
        }

        // Initialize waterfall state
        Map<String, Object> state = dc.getActiveDialog().getState();
        String instanceId = UUID.randomUUID().toString();
        state.put(PERSISTED_OPTIONS, options);
        state.put(PERSISTED_VALUES, new HashMap<String, Object>());
        state.put(PERSISTED_INSTANCEID, instanceId);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("DialogId", getId());
        properties.put("InstanceId", instanceId);

        getTelemetryClient().trackEvent("WaterfallStart", properties);
        getTelemetryClient().trackDialogView(getId(), null, null);

        // Run first step
        return runStep(dc, 0, DialogReason.BEGIN_CALLED, null);
    }

    /**
     * Called when the waterfall dialog is _continued_, where it is the active
     * dialog and the user replies with a new activity.
     *
     * @param dc The
     * @return A CompletableFuture representing the asynchronous operation.
     *
     *         If the task is successful, the result indicates whether the dialog is
     *         still active after the turn has been processed by the dialog. The
     *         result may also contain a return value.
     */
    @Override
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
        if (dc == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "dc cannot be null."
            ));
        }

        // Don't do anything for non-message activities.
        if (!dc.getContext().getActivity().isType(ActivityTypes.MESSAGE)) {
            return CompletableFuture.completedFuture(END_OF_TURN);
        }

        // Run next step with the message text as the result.
        return resumeDialog(dc, DialogReason.CONTINUE_CALLED);
    }

    /**
     * Called when a child waterfall dialog completed its turn, returning control to
     * this dialog.
     *
     * @param dc     The dialog context for the current turn of the conversation.
     * @param reason Reason why the dialog resumed.
     * @param result Optional, value returned from the dialog that was called. The
     *               type of the value returned is dependent on the child dialog.
     *
     * @return A {@link CompletableFuture} representing the asynchronous operation.
     */
    @Override
    public CompletableFuture<DialogTurnResult> resumeDialog(DialogContext dc, DialogReason reason, Object result) {

        if (dc == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "dc cannot be null."
            ));
        }

        // Increment step index and run step
        Map<String, Object> state = dc.getActiveDialog().getState();
        int index = 0;
        if (state.containsKey(STEP_INDEX)) {
            index = (int) state.get(STEP_INDEX);
        }

        return runStep(dc, index + 1, reason, result);
    }

    /**
     * Called when the dialog is ending.
     *
     * @param turnContext Context for the current turn of the conversation.
     * @param instance    The instance of the current dialog.
     * @param reason      The reason the dialog is ending.
     *
     * @return A task that represents the work queued to execute.
     */
    @Override
    public CompletableFuture<Void> endDialog(TurnContext turnContext, DialogInstance instance, DialogReason reason) {
        if (reason == DialogReason.CANCEL_CALLED) {
            HashMap<String, Object> state = new HashMap<String, Object>((Map<String, Object>) instance.getState());

            // Create step context
            int index = (int) state.get(STEP_INDEX);
            String stepName = waterfallStepName(index);
            String instanceId = (String) state.get(PERSISTED_INSTANCEID);

            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put("DialogId", getId());
            properties.put("StepName", stepName);
            properties.put("InstanceId", instanceId);

            getTelemetryClient().trackEvent("WaterfallCancel", properties);
        } else if (reason == DialogReason.END_CALLED) {
            HashMap<String, Object> state = new HashMap<String, Object>((Map<String, Object>) instance.getState());
            String instanceId = (String) state.get(PERSISTED_INSTANCEID);

            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put("DialogId", getId());
            properties.put("InstanceId", instanceId);
            getTelemetryClient().trackEvent("WaterfallComplete", properties);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Called when an individual waterfall step is being executed.
     *
     * @param stepContext Context for the waterfall step to execute.
     *
     * @return A task that represents the work queued to execute.
     */
    protected CompletableFuture<DialogTurnResult> onStep(WaterfallStepContext stepContext) {
        String stepName = waterfallStepName(stepContext.getIndex());
        String instanceId = (String) stepContext.getActiveDialog().getState().get(PERSISTED_INSTANCEID);

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("DialogId", getId());
        properties.put("StepName", stepName);
        properties.put("InstanceId", instanceId);

        getTelemetryClient().trackEvent("WaterfallStep", properties);

        return steps.get(stepContext.getIndex()).waterfallStep(stepContext);
    }

        /**
     * Excutes a step of the waterfall dialog.
     *
     * @param dc      The {@link DialogContext} for the current turn of conversation.
     * @param index   The index of the current waterfall step to execute.
     * @param reason  The reason the waterfall step is being executed.
     * @param result  Result returned by a dialog called in the previous waterfall step.
     *
     * @return   A task that represents the work queued to execute.
     */
    protected CompletableFuture<DialogTurnResult> runStep(DialogContext dc, int index,
                                                          DialogReason reason, Object result) {

        if (dc == null) {
            return Async.completeExceptionally(new IllegalArgumentException(
                "dc cannot be null."
            ));
        }

        if (index < steps.size()) {
            // Update persisted step index
            Map<String, Object> state = (Map<String, Object>) dc.getActiveDialog().getState();

            state.put(STEP_INDEX, index);

            // Create step context
            Object options = state.get(PERSISTED_OPTIONS);
            Map<String, Object> values = (Map<String, Object>) state.get(PERSISTED_VALUES);
            WaterfallStepContext stepContext =
                new WaterfallStepContext(this, dc, options, values, index, reason, result);

            // Execute step
            return  onStep(stepContext);
        }

        // End of waterfall so just return any result to parent
        return  dc.endDialog(result);
    }

    private String waterfallStepName(int index) {
        // Log Waterfall Step event. Each event has a distinct name to hook up
        // to the Application Insights funnel.
        String stepName = steps.get(index).getClass().getSimpleName();

        // Default stepname for lambdas
        if (StringUtils.isAllBlank(stepName) || stepName.contains("$Lambda$")) {
            stepName = String.format("Step%dof%d", index + 1, steps.size());
        }

        return stepName;
    }
}
