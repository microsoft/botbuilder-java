// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.complexdialog;

import com.microsoft.bot.builder.StatePropertyAccessor;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.dialogs.ComponentDialog;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.WaterfallDialog;
import com.microsoft.bot.dialogs.WaterfallStep;
import com.microsoft.bot.dialogs.WaterfallStepContext;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;

@Component
public class MainDialog extends ComponentDialog {
    private UserState userState;

    private StatePropertyAccessor<UserProfile> userProfileAccessor;

    public MainDialog(UserState withUserState) {
        super("MainDialog");

        //userProfileAccessor = withUserState.createProperty("UserProfile");
        userState = withUserState;

        addDialog(new TopLevelDialog("TopLevelDialog"));

        WaterfallStep[] waterfallSteps = {
            this::initialStep,
            this::finalStep
        };

        addDialog(new WaterfallDialog("WaterfallDialog", Arrays.asList(waterfallSteps)));

        setInitialDialogId("WaterfallDialog");
    }

    private CompletableFuture<DialogTurnResult> initialStep(WaterfallStepContext stepContext) {
        return stepContext.beginDialog("TopLevelDialog");
    }

    private CompletableFuture<DialogTurnResult> finalStep(WaterfallStepContext stepContext) {
        UserProfile userInfo = (UserProfile) stepContext.getResult();

        String status = String.format("You are signed up to review %s.",
            userInfo.companiesToReview.size() == 0
                ? "no companies"
                : String.join(",", userInfo.companiesToReview));

        return stepContext.getContext().sendActivity(status)
            .thenCompose(resourceResponse -> {
                StatePropertyAccessor<UserProfile> userProfileAccessor = userState.createProperty("UserProfile");
                return userProfileAccessor.set(stepContext.getContext(), userInfo);
            })
            .thenCompose(setResult -> stepContext.endDialog());
    }
}
