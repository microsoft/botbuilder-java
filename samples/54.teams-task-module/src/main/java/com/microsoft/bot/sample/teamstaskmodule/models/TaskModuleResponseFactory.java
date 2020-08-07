// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.teamstaskmodule.models;

import com.microsoft.bot.schema.teams.TaskModuleContinueResponse;
import com.microsoft.bot.schema.teams.TaskModuleMessageResponse;
import com.microsoft.bot.schema.teams.TaskModuleResponse;
import com.microsoft.bot.schema.teams.TaskModuleTaskInfo;

public final class TaskModuleResponseFactory {
    public static TaskModuleResponse createResponse(String message) {
        return new TaskModuleResponse() {{
            setTask(new TaskModuleMessageResponse() {{
                setValue(message);
            }});
        }};
    }

    public static TaskModuleResponse createResponse(TaskModuleTaskInfo taskInfo) {
        return new TaskModuleResponse() {{
            setTask(new TaskModuleContinueResponse() {{
                setValue(taskInfo);
            }});
        }};
    }

    public static TaskModuleResponse toTaskModuleResponse(TaskModuleTaskInfo taskInfo) {
        return createResponse(taskInfo);
    }
}
