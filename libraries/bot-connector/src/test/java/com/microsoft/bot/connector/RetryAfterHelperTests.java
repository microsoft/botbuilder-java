package com.microsoft.bot.connector;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.bot.connector.authentication.RetryAfterHelper;
import com.microsoft.bot.connector.authentication.RetryException;
import com.microsoft.bot.connector.authentication.RetryParams;

import org.junit.Assert;
import org.junit.Test;

public class RetryAfterHelperTests {

    @Test
    public void TestRetryIncrement() {
        RetryParams result = RetryAfterHelper.processRetry(new ArrayList<String>(), 8);
        Assert.assertTrue(result.getShouldRetry());
        result = RetryAfterHelper.processRetry(new ArrayList<String>(), 9);
        Assert.assertFalse(result.getShouldRetry());
    }

    @Test
    public void TestRetryDelaySeconds() {
        List<String> headers = new ArrayList<String>();
        headers.add("10");
        RetryParams result = RetryAfterHelper.processRetry(headers, 1);
        Assert.assertEquals(result.getRetryAfter(), 10000);
    }

    @Test
    public void TestRetryDelayRFC1123Date() {
        Instant instant = Instant.now().plusSeconds(5);
        ZonedDateTime dateTime = instant.atZone(ZoneId.of("UTC"));
        String dateTimeString = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
        List<String> headers = new ArrayList<String>();
        headers.add(dateTimeString);
        RetryParams result = RetryAfterHelper.processRetry(headers, 1);
        Assert.assertTrue(result.getShouldRetry());
        Assert.assertTrue(result.getRetryAfter() > 0);
    }

    @Test
    public void TestRetryDelayRFC1123DateInPast() {
        Instant instant = Instant.now().plusSeconds(-5);
        ZonedDateTime dateTime = instant.atZone(ZoneId.of("UTC"));
        String dateTimeString = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
        List<String> headers = new ArrayList<String>();
        headers.add(dateTimeString);
        RetryParams result = RetryAfterHelper.processRetry(headers, 1);
        Assert.assertTrue(result.getShouldRetry());
        // default is 50, so since the time was in the past we should be seeing the default 50 here.
        Assert.assertTrue(result.getRetryAfter() == 50);
    }


    @Test
    public void TestRetryDelayRFC1123DateEmpty() {
        List<String> headers = new ArrayList<String>();
        headers.add("");
        RetryParams result = RetryAfterHelper.processRetry(headers, 1);
        Assert.assertTrue(result.getShouldRetry());
        // default is 50, so since the time was in the past we should be seeing the default 50 here.
        Assert.assertTrue(result.getRetryAfter() == 50);
    }

    @Test
    public void TestRetryDelayRFC1123DateNull() {
        List<String> headers = new ArrayList<String>();
        headers.add(null);
        RetryParams result = RetryAfterHelper.processRetry(headers, 1);
        Assert.assertTrue(result.getShouldRetry());
        // default is 50, so since the time was in the past we should be seeing the default 50 here.
        Assert.assertTrue(result.getRetryAfter() == 50);
    }

    @Test
    public void TestRetryDelayRFC1123NeaderNull() {
        RetryParams result = RetryAfterHelper.processRetry(null, 1);
        Assert.assertTrue(result.getShouldRetry());
        // default is 50, so since the time was in the past we should be seeing the default 50 here.
        Assert.assertTrue(result.getRetryAfter() == 50);
    }

}
