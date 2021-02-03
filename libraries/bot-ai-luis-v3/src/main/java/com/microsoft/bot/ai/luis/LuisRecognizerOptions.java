package com.microsoft.bot.ai.luis;

import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import okhttp3.OkHttpClient;

import java.util.concurrent.CompletableFuture;

public abstract class LuisRecognizerOptions {
    /// <summary>
    /// Initializes a new instance of the <see cref="LuisRecognizerOptions"/> class.
    /// </summary>
    /// <param name="application">An instance of <see cref="LuisApplication"/>.</param>
    protected LuisRecognizerOptions(LuisApplication application)
    {
        if (application == null) {
            throw new IllegalArgumentException("Luis Application may not be null");
        }
        this.application = application;
    }

    /// <summary>
    /// Gets the LUIS application used to recognize text..
    /// </summary>
    /// <value>
    /// The LUIS application to use to recognize text.
    /// </value>
    public LuisApplication application = null;

    /// <summary>
    /// Gets or sets the time in milliseconds to wait before the request times out.
    /// </summary>
    /// <value>
    /// The time in milliseconds to wait before the request times out. Default is 100000 milliseconds.
    /// </value>
    public double timeout = 100000;

    /// <summary>
    /// Gets or sets the IBotTelemetryClient used to log the LuisResult event.
    /// </summary>
    /// <value>
    /// The client used to log telemetry events.
    /// </value>
    //public IBotTelemetryClient TelemetryClient { get; set; } = new NullBotTelemetryClient();

    /// <summary>
    /// Gets or sets a value indicating whether to log personal information that came from the user to telemetry.
    /// </summary>
    /// <value>If true, personal information is logged to Telemetry; otherwise the properties will be filtered.</value>
    public boolean logPersonalInformation = false;

    /// <summary>
    /// Gets or sets a value indicating whether flag to indicate if full results from the LUIS API should be returned with the recognizer result.
    /// </summary>
    /// <value>A value indicating whether full results from the LUIS API should be returned with the recognizer result.</value>
    public boolean includeAPIResults = false;

    // Support original ITurnContext
    protected abstract CompletableFuture<RecognizerResult> recognizeInternal(TurnContext turnContext, OkHttpClient httpClient);

    // Support DialogContext
    //protected abstract CompletableFuture<RecognizerResult> recognizeInternalAsync(DialogContext context, Activity activity, OkHttpClient httpClient);

}
