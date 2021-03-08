// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.sample.dialogskillbot.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import com.microsoft.bot.builder.IntentScore;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.dialogs.ComponentDialog;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.DialogTurnStatus;
import com.microsoft.bot.dialogs.WaterfallDialog;
import com.microsoft.bot.dialogs.WaterfallStep;
import com.microsoft.bot.dialogs.WaterfallStepContext;
import com.microsoft.bot.restclient.serializer.JacksonAdapter;
import com.microsoft.bot.sample.dialogskillbot.cognitivemodels.FlightBooking;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.InputHints;

/**
 * A root dialog that can route activities sent to the skill to different
 * sub-dialogs.
 */
public class ActivityRouterDialog extends ComponentDialog {

    private final DialogSkillBotRecognizer _luisRecognizer;

    public ActivityRouterDialog(DialogSkillBotRecognizer luisRecognizer) {
        super("ActivityRouterDialog");
        _luisRecognizer = luisRecognizer;

        addDialog(new BookingDialog());
        List<WaterfallStep> stepList = new ArrayList<WaterfallStep>();
        stepList.add(this::processActivity);
        addDialog(new WaterfallDialog("WaterfallDialog", stepList));

        // The initial child Dialog to run.
        setInitialDialogId("WaterfallDialog");
    }

    private CompletableFuture<DialogTurnResult> processActivity(WaterfallStepContext stepContext) {
        // A skill can send trace activities, if needed.
        TurnContext.traceActivity(stepContext.getContext(), String.format("{%s}.processActivity() Got ActivityType: %s",
                this.getClass().getName(), stepContext.getContext().getActivity().getType()));

        switch (stepContext.getContext().getActivity().getType()) {
        case ActivityTypes.EVENT:
            return onEventActivity(stepContext);

        case ActivityTypes.MESSAGE:
            return onMessageActivity(stepContext);

        default:
            String defaultMessage = String.format("Unrecognized ActivityType: \"%s\".",
                    stepContext.getContext().getActivity().getType());
            // We didn't get an activity type we can handle.
            stepContext.getContext()
                    .sendActivity(MessageFactory.text(defaultMessage, defaultMessage, InputHints.IGNORING_INPUT))
                    .join();
            return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.COMPLETE));
        }
    }

    // This method performs different tasks super. on the event name.
    private CompletableFuture<DialogTurnResult> onEventActivity(WaterfallStepContext stepContext) {
        Activity activity = stepContext.getContext().getActivity();
        TurnContext.traceActivity(stepContext.getContext(),
                    String.format("%s.onEventActivity(), label: %s, Value: %s",
                    this.getClass().getName(),
                    activity.getName(),
                    GetObjectAsJsonString(activity.getValue())));

        // Resolve what to execute super. on the event name.
        switch (activity.getName()) {
            case "BookFlight":
                return  BeginBookFlight(stepContext);

            case "GetWeather":
                return  BeginGetWeather(stepContext);

            default:
            String message = String.format("Unrecognized EventName: \"%s\".", activity.getName());
                // We didn't get an event name we can handle.
                 stepContext.getContext().sendActivity(MessageFactory.text(message,
                                                                           message,
                                                                           InputHints.IGNORING_INPUT));
                return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.COMPLETE));
        }
    }

    // This method just gets a message activity and runs it through LUS.
    private CompletableFuture<DialogTurnResult> onMessageActivity(WaterfallStepContext stepContext) {
        Activity activity = stepContext.getContext().getActivity();
        TurnContext.traceActivity(stepContext.getContext(),
                    String.format("%s.onMessageActivity(), label: %s, Value: %s",
                    this.getClass().getName(),
                    activity.getName(),
                    GetObjectAsJsonString(activity.getValue())));

        if (!_luisRecognizer.getIsConfigured()) {
            String message = "NOTE: LUIS instanceof not configured. To enable all capabilities, add 'LuisAppId',"
                             + " 'LuisAPKey' and 'LuisAPHostName' to the appsettings.json file.";
             stepContext.getContext().sendActivity(MessageFactory.text(message, message, InputHints.IGNORING_INPUT));
        } else {
            // Call LUS with the utterance.
            FlightBooking luisResult =
                    _luisRecognizer.recognize(stepContext.getContext(), FlightBooking.class).join();

            // Create a message showing the LUS results.
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("LUIS results for \"%s\":", activity.getText()));

            FlightBooking.Intent intent = FlightBooking.Intent.None;
            IntentScore intentScore = new IntentScore();
            intentScore.setScore(0.0);
            for(Entry<FlightBooking.Intent, IntentScore> item: luisResult.Intents.entrySet()) {
                if (item.getValue().getScore() > intentScore.getScore()) {
                    intentScore = item.getValue();
                    intent = item.getKey();
                }
            }

            sb.append(String.format("Intent: \"%s\" Score: %s", intent, intentScore.getScore()));

            stepContext.getContext().sendActivity(MessageFactory.text(sb.toString(),
                                                                      sb.toString(),
                                                                      InputHints.IGNORING_INPUT)).join();

            // Start a dialog if we recognize the intent.
            switch (luisResult.TopIntent().getLeft()) {
                case BookFlight:
                    return BeginBookFlight(stepContext);

                case GetWeather:
                    return BeginGetWeather(stepContext);

                default:
                    // Catch all for unhandled intents.
                    String didntUnderstandMessageText = String.format(
                        "Sorry, I didn't get that. Please try asking in a different way (intent was %s)",
                        luisResult.TopIntent().getLeft());
                    Activity didntUnderstandMessage = MessageFactory.text(didntUnderstandMessageText,
                                                                          didntUnderstandMessageText,
                                                                          InputHints.IGNORING_INPUT);
                    stepContext.getContext().sendActivity(didntUnderstandMessage).join();
                    break;
            }
        }

        return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.COMPLETE));
    }

    private static CompletableFuture<DialogTurnResult> BeginGetWeather(WaterfallStepContext stepContext) {
        Activity activity = stepContext.getContext().getActivity();
        Location location = new Location();
        if (activity.getValue() != null && activity.getValue() instanceof Location) {
            location = (Location) activity.getValue();
        }

        // We haven't implemented the GetWeatherDialog so we just display a TODO message.
        String getWeatherMessageText = String.format("TODO: get weather for here (lat: %s, long: %s)"
                                                     ,location.getLatitude(),
                                                     location.getLongitude());
        Activity getWeatherMessage = MessageFactory.text(getWeatherMessageText,
                                                         getWeatherMessageText,
                                                         InputHints.IGNORING_INPUT);
         stepContext.getContext().sendActivity(getWeatherMessage);
        return CompletableFuture.completedFuture(new DialogTurnResult(DialogTurnStatus.COMPLETE));
    }

    private CompletableFuture<DialogTurnResult> BeginBookFlight(WaterfallStepContext stepContext) {
        Activity activity = stepContext.getContext().getActivity();
        BookingDetails bookingDetails = new BookingDetails();
        if (activity.getValue() != null && activity.getValue() instanceof BookingDetails) {
            bookingDetails = (BookingDetails) activity.getValue();
        }

        // Start the booking dialog.
        Dialog bookingDialog = findDialog("BookingDialog");
        return stepContext.beginDialog(bookingDialog.getId(), bookingDetails);
    }

    private String GetObjectAsJsonString(Object value) {
        try {
            return new JacksonAdapter().serialize(value);
        } catch (IOException e) {
            return null;
        }
    }
}
