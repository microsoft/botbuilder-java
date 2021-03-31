package com.microsoft.bot.connector.authentication;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Class that contains a helper function to process HTTP 429 Retry-After headers
 * for the CredentialsAuthenticator. The reason to extract this was
 * CredentialsAuthenticator is an internal class that isn't exposed except
 * through other Authentication classes and we wanted a way to test the
 * processing of 429 headers without building complicated test harnesses.
 */
public final class RetryAfterHelper {

    private RetryAfterHelper() {

    }

    /**
     * Process a RetryException and see if we should wait for a requested amount of
     * time before retrying to call the authentication service again.
     *
     * @param header The header values to process.
     * @param count  The count of how many times we have retried.
     * @return A RetryParams with instructions of when or how many more times to
     *         retry.
     */
    public static RetryParams processRetry(List<String> header, Integer count) {
        if (header == null || header.size() == 0) {
            return RetryParams.defaultBackOff(++count);
        } else {
            String headerString = header.get(0);
            if (StringUtils.isNotBlank(headerString)) {
                // see if it matches a numeric value
                if (headerString.matches("^[0-9]+\\.?0*$")) {
                    headerString = headerString.replaceAll("\\.0*$", "");
                    Duration delay = Duration.ofSeconds(Long.parseLong(headerString));
                    return new RetryParams(delay.toMillis());
                } else {
                    // check to see if it's a RFC_1123 format Date/Time
                    DateTimeFormatter gmtFormat = DateTimeFormatter.RFC_1123_DATE_TIME;
                    try {
                        ZonedDateTime zoned = ZonedDateTime.parse(headerString, gmtFormat);
                        if (zoned != null) {
                            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
                            long waitMillis = zoned.toInstant().toEpochMilli() - now.toInstant().toEpochMilli();
                            if (waitMillis > 0) {
                                return new RetryParams(waitMillis);
                            } else {
                                return RetryParams.defaultBackOff(++count);
                            }
                        }
                    } catch (DateTimeParseException ex) {
                        return RetryParams.defaultBackOff(++count);
                    }
                }
            }
        }
        return RetryParams.defaultBackOff(++count);
    }

}
