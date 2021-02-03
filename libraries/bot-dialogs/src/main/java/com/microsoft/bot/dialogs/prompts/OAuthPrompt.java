// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.dialogs.prompts;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.BotAssert;
import com.microsoft.bot.builder.ConnectorClientBuilder;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.TurnStateConstants;
import com.microsoft.bot.builder.UserTokenProvider;
import com.microsoft.bot.connector.Async;
import com.microsoft.bot.connector.Channels;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.authentication.ClaimsIdentity;
import com.microsoft.bot.connector.authentication.JwtTokenValidation;
import com.microsoft.bot.connector.authentication.SkillValidation;
import com.microsoft.bot.dialogs.Dialog;
import com.microsoft.bot.dialogs.DialogContext;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.bot.schema.OAuthCard;
import com.microsoft.bot.schema.SignInConstants;
import com.microsoft.bot.schema.SignInResource;
import com.microsoft.bot.schema.SigninCard;
import com.microsoft.bot.schema.TokenExchangeInvokeRequest;
import com.microsoft.bot.schema.TokenExchangeInvokeResponse;
import com.microsoft.bot.schema.TokenExchangeRequest;
import com.microsoft.bot.schema.TokenExchangeResource;
import com.microsoft.bot.schema.TokenResponse;

import org.apache.commons.lang3.StringUtils;

import io.netty.util.internal.logging.CommonsLoggerFactory;

/**
 * Creates a new prompt that asks the user to sign in using the Bot Frameworks
 * Single Sign On (SSO)service.
 *
 * The prompt will attempt to retrieve the users current token and if the user
 * isn't signed in, itwill send them an `OAuthCard` containing a button they can
 * press to signin. Depending on thechannel, the user will be sent through one
 * of two possible signin flows:- The automatic signin flow where once the user
 * signs in and the SSO service will forward the botthe users access token using
 * either an `event` or `invoke` activity.- The "magic code" flow where once the
 * user signs in they will be prompted by the SSOservice to send the bot a six
 * digit code confirming their identity. This code will be sent as astandard
 * `message` activity. Both flows are automatically supported by the
 * `OAuthPrompt` and the only thing you need to becareful of is that you don't
 * block the `event` and `invoke` activities that the prompt mightbe waiting on.
 * **Note**:You should avoid persisting the access token with your bots other
 * state. The Bot FrameworksSSO service will securely store the token on your
 * behalf. If you store it in your bots stateit could expire or be revoked in
 * between turns. When calling the prompt from within a waterfall step you
 * should use the token within the stepfollowing the prompt and then let the
 * token go out of scope at the end of your function.
 */
public class OAuthPrompt extends Dialog {

    private static final String PersistedOptions = "options";
    private static final String PersistedState = "state";
    private static final String PersistedExpires = "expires";
    private static final String PersistedCaller = "caller";

    private final OAuthPromptSettings _settings;
    private final PromptValidator<TokenResponse> _validator;

    /**
     * Initializes a new instance of the {@link OAuthPrompt} class.
     *
     * @param dialogId  The D to assign to this prompt.
     * @param settings  Additional OAuth settings to use with this instance of the
     *                  prompt.
     * @param validator Optional, a {@link PromptValidator{FoundChoice}} that
     *                  contains additional, custom validation for this prompt.
     *
     *                  The value of {@link dialogId} must be unique within the
     *                  {@link DialogSet} or {@link ComponentDialog} to which the
     *                  prompt is added.
     */
    public OAuthPrompt(String dialogId, OAuthPromptSettings settings, PromptValidator<TokenResponse> validator) {
        super(dialogId);

        if (StringUtils.isEmpty(dialogId)) {
            throw new IllegalArgumentException("dialogId cannot be null.");
        }

        if (settings == null) {
            throw new IllegalArgumentException("settings cannot be null.");
        }

        _settings = settings;
        _validator = validator;
    }

    /**
     * Shared implementation of the SendOAuthCard function. This is intended for
     * internal use, to consolidate the implementation of the OAuthPrompt and
     * OAuthInput. Application logic should use those dialog classes.
     *
     * @param settings    OAuthSettings.
     * @param turnContext TurnContext.
     * @param prompt      MessageActivity.
     *
     * @return A {@link CompletableFuture} representing the result of the hronous
     *         operation.
     */
    public static CompletableFuture<Void> sendOAuthCard(OAuthPromptSettings settings, TurnContext turnContext,
            Activity prompt) {
        BotAssert.contextNotNull(turnContext);

        BotAdapter adapter = turnContext.getAdapter();

        if (!(adapter instanceof UserTokenProvider)) {
            return Async.completeExceptionally(
                    new UnsupportedOperationException("OAuthPrompt.Prompt(): not supported by the current adapter"));
        }

        UserTokenProvider tokenAdapter = (UserTokenProvider) adapter;

        // Ensure prompt initialized
        if (prompt == null) {
            prompt = Activity.createMessageActivity();
        }

        if (prompt.getAttachments() == null) {
            prompt.setAttachments(new ArrayList<>());
        }

        // Append appropriate card if missing
        if (!ChannelSupportsOAuthCard(turnContext.getActivity().getChannelId())) {
            if (!prompt.getAttachments().stream().anyMatch(s -> s.getContent() instanceof SigninCard)) {
                SignInResource signInResource = tokenAdapter
                        .getSignInResource(turnContext, settings.getOAuthAppCredentials(), settings.getConnectionName(),
                                turnContext.getActivity().getFrom().getId(), null)
                        .join();
                SigninCard signInCard = new SigninCard();
                signInCard.setText(settings.getText());
                CardAction cardAction = new CardAction();
                cardAction.setTitle(settings.getTitle());
                cardAction.setValue(signInResource.getSignInLink());
                prompt.getAttachments().add(new Attachment() {
                    {
                        setContentType(OAuthCard.CONTENTTYPE);
                        setContent(new OAuthCard() {
                            {
                                setText(settings.getText());
                                setButtons(new ArrayList<CardAction>() {
                                    {
                                        new CardAction() {
                                            {
                                                setTitle(settings.getTitle());
                                                setValue(signInResource.getSignInLink());
                                                setType(ActionTypes.SIGNIN);
                                            }
                                        };
                                    }
                                });
                            }
                        });
                    }
                });
            }
        } else if (!prompt.getAttachments().stream().anyMatch(s -> s.getContent() instanceof OAuthCard)) {
            ActionTypes cardActionType = ActionTypes.SIGNIN;
            SignInResource signInResource = tokenAdapter
                    .getSignInResource(turnContext, settings.getOAuthAppCredentials(), settings.getConnectionName(),
                            turnContext.getActivity().getFrom().getId(), null)
                    .join();
            String value = signInResource.getSignInLink();

            // use the SignInLink when
            // in speech channel or
            // bot is a skill or
            // an extra OAuthAppCredentials is being passed in
            ClaimsIdentity botIdentity = turnContext.getTurnState().get(BotAdapter.BOT_IDENTITY_KEY);
            if (turnContext.getActivity().isFromStreamingConnection()
                    || (botIdentity != null && SkillValidation.isSkillClaim(botIdentity.claims()))
                    || settings.getOAuthAppCredentials() != null) {
                if (turnContext.getActivity().getChannelId() == Channels.EMULATOR) {
                    cardActionType = ActionTypes.OPEN_URL;
                }
            } else if (!ChannelRequiresSignInLink(turnContext.getActivity().getChannelId())) {
                value = null;
            }

            // new references to these values that are 'effectively final' so they can be used
            // within an initializer, which is essentially a lamdba expression.
            ActionTypes finalCardActionType = cardActionType;
            String finalValue = value;

            prompt.getAttachments().add(new Attachment() {
                {
                    setContentType(OAuthCard.CONTENTTYPE);
                    setContent(new OAuthCard() {
                        {
                            setText(settings.getText());
                            setButtons(new ArrayList<CardAction>() {
                                {
                                    new CardAction() {
                                        {
                                            setTitle(settings.getTitle());
                                            setText(settings.getText());
                                            setType(finalCardActionType);
                                            setValue(finalValue);
                                        }
                                    };
                                }
                            });
                            setTokenExchangeResource(signInResource.getTokenExchangeResource());
                        }
                    });
                }
            });
        }

        // Add the login timeout specified in OAuthPromptSettings to TurnState so it can
        // be referenced if polling is needed
        if (!turnContext.getTurnState().containsKey(TurnStateConstants.OAUTH_LOGIN_TIMEOUT_KEY)
                && settings.getTimeout() != null) {
            turnContext.getTurnState().add(TurnStateConstants.OAUTH_LOGIN_TIMEOUT_KEY,
                    Duration.ofMillis(settings.getTimeout()));
        }

        // Set input hint
        if (prompt.getInputHint() == null) {
            prompt.setInputHint(InputHints.ACCEPTING_INPUT);
        }

        return turnContext.sendActivity(prompt).thenApply(result -> null);
    }

    /**
     * Shared implementation of the RecognizeToken function. This is intended for internal use, to
     * consolidate the implementation of the OAuthPrompt and OAuthInput. Application logic should
     * use those dialog classes.
     *
     * @param settings  OAuthPromptSettings.
     * @param dc        DialogContext.
     *
     * @return   PromptRecognizerResult.
     */
    public static CompletableFuture<PromptRecognizerResult<TokenResponse>> recognizeToken(
                                                                                OAuthPromptSettings settings,
                                                                                DialogContext dc) {
        TurnContext turnContext = dc.getContext();
        PromptRecognizerResult<TokenResponse> result = new PromptRecognizerResult<TokenResponse>();
        if (isTokenResponseEvent(turnContext)) {
            Object tokenResponseObject = turnContext.getActivity().getValue();
            TokenResponse token = null;
            if (tokenResponseObject != null) {
                token = (TokenResponse) tokenResponseObject;
            }
            result.setSucceeded(true);
            result.setValue(token);

            // fixup the turnContext's state context if this was received from a skill host caller
            CallerInfo callerInfo = (CallerInfo) dc.getActiveDialog().getState().get(PersistedCaller);
            if (callerInfo != null) {
                // set the ServiceUrl to the skill host's Url
                dc.getContext().getActivity().setServiceUrl(callerInfo.getCallerServiceUrl());

                Object adapter = turnContext.getAdapter();
                // recreate a ConnectorClient and set it in TurnState so replies use the correct one
                if (!(adapter instanceof ConnectorClientBuilder)) {
                    return Async.completeExceptionally(
                        new UnsupportedOperationException(
                            "OAuthPrompt: ConnectorClientProvider interface not implemented by the current adapter"
                    ));
                }

                ConnectorClientBuilder connectorClientProvider = (ConnectorClientBuilder) adapter;
                ClaimsIdentity claimsIdentity = turnContext.getTurnState().get(BotAdapter.BOT_IDENTITY_KEY);
                ConnectorClient connectorClient =  connectorClientProvider.createConnectorClient(
                                                                dc.getContext().getActivity().getServiceUrl(),
                                                                claimsIdentity,
                                                                callerInfo.getScope()).join();

                if (turnContext.getTurnState().get(ConnectorClient.class) != null) {
                    turnContext.getTurnState().replace(connectorClient);
                } else {
                    turnContext.getTurnState().add(connectorClient);
                }
            }
        } else if (isTeamsVerificationInvoke(turnContext)) {
            String magicCode = (String) turnContext.getActivity().getValue();
            //var magicCode = magicCodeObject.GetValue("state", StringComparison.Ordinal)?.toString();

            Object adapterObject = turnContext.getAdapter();
            if (!(adapterObject instanceof UserTokenProvider)) {
                return Async.completeExceptionally(
                    new UnsupportedOperationException(
                        "OAuthPrompt.Recognize(): not supported by the current adapter"
                ));
            }

            UserTokenProvider adapter = (UserTokenProvider) adapterObject;

            // Getting the token follows a different flow in Teams. At the signin completion, Teams
            // will send the bot an "invoke" activity that contains a "magic" code. This code MUST
            // then be used to try fetching the token from Botframework service within some time
            // period. We try here. If it succeeds, we return 200 with an empty body. If it fails
            // with a retriable error, we return 500. Teams will re-send another invoke in this case.
            // If it fails with a non-retriable error, we return 404. Teams will not (still work in
            // progress) retry in that case.
            try {
                TokenResponse token =  adapter.getUserToken(
                                                    turnContext,
                                                    settings.getOAuthAppCredentials(),
                                                    settings.getConnectionName(),
                                                    magicCode).join();

                if (token != null) {
                    result.setSucceeded(true);
                    result.setValue(token);

                     turnContext.sendActivity(new Activity(ActivityTypes.INVOKE_RESPONSE));
                } else {
                     sendInvokeResponse(turnContext, HttpURLConnection.HTTP_NOT_FOUND, null);
                }
            }
            catch (Exception e) {
                 sendInvokeResponse(turnContext, HttpURLConnection.HTTP_INTERNAL_ERROR, null);
            }
        } else if (isTokenExchangeRequestInvoke(turnContext)) {
            TokenExchangeInvokeRequest tokenExchangeRequest =
                                (turnContext.getActivity().getValue() instanceof TokenExchangeInvokeRequest)
                                ? (TokenExchangeInvokeRequest) turnContext.getActivity().getValue() : null;

            if (tokenExchangeRequest == null) {
                 sendInvokeResponse(
                    turnContext,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    new TokenExchangeInvokeResponse () {
                        {
                        setId(null);
                        setConnectionName(settings.getConnectionName());
                        setFailureDetail("The bot received an InvokeActivity that is missing a " +
                                         "TokenExchangeInvokeRequest value. This is required to be " +
                                         "sent with the InvokeActivity.");
                        }
                    });
            } else if (tokenExchangeRequest.getConnectionName() != settings.getConnectionName()) {
                sendInvokeResponse(
                    turnContext,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    new TokenExchangeInvokeResponse () {
                        {
                        setId(tokenExchangeRequest.getId());
                        setConnectionName(settings.getConnectionName());
                        setFailureDetail("The bot received an InvokeActivity with a TokenExchangeInvokeRequest " +
                                         "containing a ConnectionName that does not match the ConnectionName " +
                                         "expected by the bot's active OAuthPrompt. Ensure these names match " +
                                         "when sending the InvokeActivityInvalid ConnectionName in the " +
                                         "TokenExchangeInvokeRequest");
                        }
                    });
            } else if (!(turnContext.getAdapter() instanceof UserTokenProvider)) {
                sendInvokeResponse(
                    turnContext,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    new TokenExchangeInvokeResponse () {
                        {
                        setId(tokenExchangeRequest.getId());
                        setConnectionName(settings.getConnectionName());
                        setFailureDetail("The bot's BotAdapter does not support token exchange operations. Ensure " +
                                         "the bot's Adapter supports the UserTokenProvider interface.");
                        }
                    });
                    return Async.completeExceptionally(
                        new UnsupportedOperationException(
                            "OAuthPrompt.Recognize(): not supported by the current adapter"
                    ));
            } else {
                TokenResponse tokenExchangeResponse = null;
                try {
                    UserTokenProvider adapter = (UserTokenProvider) turnContext.getAdapter();
                    tokenExchangeResponse =  adapter.exchangeToken(
                        turnContext,
                        settings.getConnectionName(),
                        turnContext.getActivity().getFrom().getId(),
                        new TokenExchangeRequest () {
                            {
                                setToken(tokenExchangeRequest.getToken());
                            }
                        }).join();
                }
                catch (Exception ex) {
                    // Ignore Exceptions
                    // If token exchange failed for any reason, tokenExchangeResponse above stays null, and
                    // hence we send back a failure invoke response to the caller.
                    // This ensures that the caller shows
                }

                if (tokenExchangeResponse == null || StringUtils.isBlank(tokenExchangeResponse.getToken())) {
                     sendInvokeResponse(
                        turnContext,
                        HttpURLConnection.HTTP_PRECON_FAILED,
                        new TokenExchangeInvokeResponse(){ {
                            setId(tokenExchangeRequest.getId());
                            setConnectionName(settings.getConnectionName());
                            setFailureDetail("The bot is unable to exchange token. Proceed with regular login.");
                        }
                    });
                } else {
                     sendInvokeResponse(
                        turnContext,
                        HttpURLConnection.HTTP_OK,
                        new TokenExchangeInvokeResponse() {
                            {
                            setId(tokenExchangeRequest.getId());
                            setConnectionName(settings.getConnectionName());
                            }
                        });

                    result.setSucceeded(true);
                    TokenResponse finalResponse = tokenExchangeResponse;
                    result.setValue(new TokenResponse(){ {
                        setChannelId(finalResponse.getChannelId());
                        setConnectionName(finalResponse.getConnectionName());
                        setToken(finalResponse.getToken());
                    }});
                }
            }
        } else if (turnContext.getActivity().getType() == ActivityTypes.MESSAGE) {
            // regex to check if code supplied is a 6 digit numerical code (hence, a magic code).
            String pattern = "(\\d{6})";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(turnContext.getActivity().getText());

            if (m.find()) {
                if (!(turnContext.getAdapter() instanceof UserTokenProvider)) {
                    return Async.completeExceptionally(
                        new UnsupportedOperationException(
                            "OAuthPrompt.Recognize(): not supported by the current adapter"
                    ));
                }
                UserTokenProvider adapter = (UserTokenProvider) turnContext.getAdapter();
                TokenResponse token =  adapter.getUserToken(turnContext,
                                                            settings.getOAuthAppCredentials(),
                                                            settings.getConnectionName(),
                                                            m.group(0)).join();
                if (token != null) {
                    result.setSucceeded(true);
                    result.setValue(token);
                }
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * Shared implementation of the SetCallerInfoInDialogState function. This is
     * intended for internal use, to consolidate the implementation of the
     * OAuthPrompt and OAuthInput. Application logic should use those dialog
     * classes.
     *
     * @param state   The dialog state.
     * @param context TurnContext.
     */
    public static void setCallerInfoInDialogState(Map<String, Object> state, TurnContext context) {
        state.put(PersistedCaller, createCallerInfo(context));
    }

    /**
     * Called when a prompt dialog is pushed onto the dialog stack and is being activated.
     *
     * @param dc       The dialog context for the current turn of the conversation.
     * @param options  Optional, additional information to pass to the prompt being started.
     *
     * @return   A {@link CompletableFuture} representing the hronous operation.
     *
     * If the task is successful, the result indicates whether the prompt is still active after the
     * turn has been processed by the prompt.
     */
    @Override
    public CompletableFuture<DialogTurnResult> beginDialog(DialogContext dc, Object options) {
        if (dc == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException(
                    "dc cannot be null."
            ));
        }

        if (options != null && !(options instanceof PromptOptions)) {
            return Async.completeExceptionally(
                new IllegalArgumentException(
                    "Parameter options should be an instance of to PromptOptions if provided."
            ));
        }

        PromptOptions opt = (PromptOptions) options;
        if (opt != null) {
            // Ensure prompts have input hint set
            if (opt.getPrompt() != null && opt.getPrompt().getInputHint() == null) {
                opt.getPrompt().setInputHint(InputHints.ACCEPTING_INPUT);
            }

            if (opt.getRetryPrompt() != null && opt.getRetryPrompt() == null) {
                opt.getRetryPrompt().setInputHint(InputHints.ACCEPTING_INPUT);
            }
        }

        // Initialize state
        int timeout = _settings.getTimeout() != null ? _settings.getTimeout()
                                                     : (int) TurnStateConstants.OAUTH_LOGIN_TIMEOUT_VALUE.toMillis();
        Map<String, Object> state = dc.getActiveDialog().getState();
        state.put(PersistedOptions, opt);
        state.put(PersistedState, new HashMap<String, Object>() {
            {
                { put(Prompt.ATTEMPTCOUNTKEY, 0); }
            }
        });

        state.put(PersistedExpires, OffsetDateTime.now(ZoneId.of("UTC")).plus(timeout, ChronoUnit.MILLIS));
        setCallerInfoInDialogState(state, dc.getContext());

        // Attempt to get the users token
        if (!(dc.getContext().getAdapter() instanceof UserTokenProvider)) {
            return Async.completeExceptionally(
                new UnsupportedOperationException(
                    "OAuthPrompt.Recognize(): not supported by the current adapter"
            ));
        }

        UserTokenProvider adapter = (UserTokenProvider) dc.getContext().getAdapter();
        TokenResponse output =  adapter.getUserToken(dc.getContext(),
                                                     _settings.getOAuthAppCredentials(),
                                                     _settings.getConnectionName(),
                                                     null).join();
        if (output != null) {
            // Return token
            return  dc.endDialog(output);
        }

        // Prompt user to login
        sendOAuthCard(_settings, dc.getContext(), opt != null ? opt.getPrompt() : null);
        return CompletableFuture.completedFuture(END_OF_TURN);
    }

    /**
     * Called when a prompt dialog is the active dialog and the user replied with a new activity.
     *
     * @param dc  The dialog context for the current turn of conversation.
     *
     * @return   A {@link CompletableFuture} representing the hronous operation.
     *
     * If the task is successful, the result indicates whether the dialog is still active after the
     * turn has been processed by the dialog. The prompt generally continues to receive the user's
     * replies until it accepts the user's reply as valid input for the prompt.
     */
    @Override
    public CompletableFuture<DialogTurnResult> continueDialog(DialogContext dc) {
        if (dc == null) {
            return Async.completeExceptionally(
                new IllegalArgumentException(
                    "dc cannot be null."
            ));
        }

        // Check for timeout
        Map<String, Object> state = dc.getActiveDialog().getState();
        OffsetDateTime expires = (OffsetDateTime) state.get(PersistedExpires);
        boolean isMessage = dc.getContext().getActivity().getType() == ActivityTypes.MESSAGE;

        // If the incoming Activity is a message, or an Activity Type normally handled by OAuthPrompt,
        // check to see if this OAuthPrompt Expiration has elapsed, and end the dialog if so.
        boolean isTimeoutActivityType = isMessage
                        || isTokenResponseEvent(dc.getContext())
                        || isTeamsVerificationInvoke(dc.getContext())
                        || isTokenExchangeRequestInvoke(dc.getContext());


        boolean hasTimedOut = isTimeoutActivityType && OffsetDateTime.now(ZoneId.of("UTC")).compareTo(expires) > 0;

        if (hasTimedOut) {
            // if the token fetch request times out, complete the prompt with no result.
            return  dc.endDialog();
        }

        // Recognize token
        PromptRecognizerResult<TokenResponse> recognized =  recognizeToken(_settings, dc).join();

        Map<String, Object> promptState = (Map<String, Object>) state.get(PersistedState);
        PromptOptions promptOptions = (PromptOptions) state.get(PersistedOptions);

        // Increment attempt count
        // Convert.ToInt32 For issue https://github.com/Microsoft/botbuilder-dotnet/issues/1859
        promptState.put(Prompt.ATTEMPTCOUNTKEY, (int) promptState.get(Prompt.ATTEMPTCOUNTKEY) + 1);;

        // Validate the return value
        boolean isValid = false;
        if (_validator != null) {
            PromptValidatorContext<TokenResponse> promptContext = new PromptValidatorContext<TokenResponse>(
                                                                                dc.getContext(),
                                                                                recognized,
                                                                                promptState,
                                                                                promptOptions);
            isValid =  _validator.promptValidator(promptContext).join();
        } else if (recognized.getSucceeded()) {
            isValid = true;
        }

        // Return recognized value or re-prompt
        if (isValid) {
            return  dc.endDialog(recognized.getValue());
        } else if (isMessage && _settings.getEndOnInvalidMessage()) {
            // If EndOnInvalidMessage is set, complete the prompt with no result.
            return  dc.endDialog();
        }

        if (!dc.getContext().getResponded()
            && isMessage
            && promptOptions != null
            && promptOptions.getRetryPrompt() != null) {
             dc.getContext().sendActivity(promptOptions.getRetryPrompt());
        }

        return CompletableFuture.completedFuture(END_OF_TURN);
    }

    /**
     * Attempts to get the user's token.
     *
     * @param turnContext Context for the current turn of conversation with the
     *                    user.
     *
     * @return A task that represents the work queued to execute.
     *
     *         If the task is successful and user already has a token or the user
     *         successfully signs in, the result contains the user's token.
     */
    public CompletableFuture<TokenResponse> GetUserToken(TurnContext turnContext) {
        if (!(turnContext.getAdapter() instanceof UserTokenProvider)) {
            return Async.completeExceptionally(
                new UnsupportedOperationException(
                    "OAuthPrompt.GetUserToken(): not supported by the current adapter"
            ));
        }
        return ((UserTokenProvider) turnContext.getAdapter()).getUserToken(turnContext,
                                                                           _settings.getOAuthAppCredentials(),
                                                                           _settings.getConnectionName(), null);
    }

    /**
     * Signs out the user.
     *
     * @param turnContext  Context for the current turn of conversation with the user.
     *
     * @return   A task that represents the work queued to execute.
     */
    public CompletableFuture<Void> signOutUser(TurnContext turnContext) {
        if (!(turnContext.getAdapter() instanceof UserTokenProvider)) {
            return Async.completeExceptionally(
                new UnsupportedOperationException(
                    "OAuthPrompt.SignOutUser(): not supported by the current adapter"
            ));
        }
        String id = "";
        if (turnContext.getActivity() != null
            && turnContext.getActivity() != null
            && turnContext.getActivity().getFrom() != null) {
                id = turnContext.getActivity().getFrom().getId();
            }

        // Sign out user
        return ((UserTokenProvider) turnContext.getAdapter()).signOutUser(turnContext,
                                                                          _settings.getOAuthAppCredentials(),
                                                                          _settings.getConnectionName(),
                                                                          id);
    }

    private static CallerInfo createCallerInfo(TurnContext turnContext) {
        ClaimsIdentity botIdentity =
            turnContext.getTurnState().get(BotAdapter.BOT_IDENTITY_KEY) instanceof ClaimsIdentity ?
            (ClaimsIdentity) turnContext.getTurnState().get(BotAdapter.BOT_IDENTITY_KEY) :
            null;

        if (botIdentity != null && SkillValidation.isSkillClaim(botIdentity.claims())) {
            return new CallerInfo() {
                {
                    setCallerServiceUrl(turnContext.getActivity().getServiceUrl());
                    setScope(JwtTokenValidation.getAppIdFromClaims(botIdentity.claims()));
                }
            };
        }

        return null;
    }

    private static boolean isTokenResponseEvent(TurnContext turnContext) {
        Activity activity = turnContext.getActivity();
        return activity.getType() == ActivityTypes.EVENT
                && activity.getName() == SignInConstants.TOKEN_RESPONSE_EVENT_NAME;
    }

    private static boolean isTeamsVerificationInvoke(TurnContext turnContext) {
        Activity activity = turnContext.getActivity();
        return activity.getType() == ActivityTypes.INVOKE
                && activity.getName() == SignInConstants.VERIFY_STATE_OPERATION_NAME;
    }

    private static boolean isTokenExchangeRequestInvoke(TurnContext turnContext) {
        Activity activity = turnContext.getActivity();
        return activity.getType() == ActivityTypes.INVOKE
                && activity.getName() == SignInConstants.TOKEN_EXCHANGE_OPERATION_NAME;
    }

    private static boolean ChannelSupportsOAuthCard(String channelId) {
        switch (channelId) {
            case Channels.CORTANA:
            case Channels.SKYPE:
            case Channels.SKYPEFORBUSINESS:
                return false;
        }
        return true;
    }

    private static boolean ChannelRequiresSignInLink(String channelId) {
        switch (channelId) {
            case Channels.MSTEAMS:
                return true;
        }
        return false;
    }

    private static CompletableFuture<Void> sendInvokeResponse(TurnContext turnContext, int statusCode,
            Object body) {
        return turnContext.sendActivity(new Activity(ActivityTypes.INVOKE_RESPONSE) {
            {
                setValue(new InvokeResponse(statusCode, body));
            }
        }).thenApply(result -> null);
    }

    private static class CallerInfo {

        private String callerServiceUrl;

        private String scope;

        /**
         * @return the CallerServiceUrl value as a String.
         */
        public String getCallerServiceUrl() {
            return this.callerServiceUrl;
        }

        /**
         * @param withCallerServiceUrl The CallerServiceUrl value.
         */
        public void setCallerServiceUrl(String withCallerServiceUrl) {
            this.callerServiceUrl = withCallerServiceUrl;
        }
        /**
         * @return the Scope value as a String.
         */
        public String getScope() {
            return this.scope;
        }

        /**
         * @param withScope The Scope value.
         */
        public void setScope(String withScope) {
            this.scope = withScope;
        }

    }
}

