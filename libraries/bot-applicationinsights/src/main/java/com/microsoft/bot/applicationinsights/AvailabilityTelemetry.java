// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.applicationinsights;

import com.microsoft.applicationinsights.internal.schemav2.AvailabilityData;
import com.microsoft.applicationinsights.internal.util.LocalStringsUtils;
import com.microsoft.applicationinsights.internal.util.Sanitizer;
import com.microsoft.applicationinsights.telemetry.BaseSampleSourceTelemetry;
import com.microsoft.applicationinsights.telemetry.Duration;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

/**
 * We took this class from
 * https://github.com/microsoft/ApplicationInsights-Java/issues/1099 as this is
 * not already migrated in ApplicationInsights-Java library.
 */
public final class AvailabilityTelemetry extends BaseSampleSourceTelemetry<AvailabilityData> {
    private Double samplingPercentage;
    private final AvailabilityData data;

    public static final String ENVELOPE_NAME = "Availability";

    public static final String BASE_TYPE = "AvailabilityData";

    /**
     * Initializes a new instance of the AvailabilityTelemetry class.
     */
    public AvailabilityTelemetry() {
        this.data = new AvailabilityData();
        initialize(this.data.getProperties());
        setId(LocalStringsUtils.generateRandomIntegerId());

        // Setting mandatory fields.
        setTimestamp(new Date());
        setSuccess(true);
    }

    /**
     * Initializes a new instance of the AvailabilityTelemetry class with the given
     * name, time stamp, duration, HTTP response code and success property values.
     * 
     * @param name         A user-friendly name for the request.
     * @param duration     The time of the request.
     * @param runLocation  The duration, in milliseconds, of the request processing.
     * @param message      The HTTP response code.
     * @param success      'true' if the request was a success, 'false' otherwise.
     * @param measurements The measurements.
     * @param properties   The corresponding properties.
     */
    public AvailabilityTelemetry(
        String name,
        Duration duration,
        String runLocation,
        String message,
        boolean success,
        ConcurrentMap<String, Double> measurements,
        ConcurrentMap<String, String> properties
    ) {

        this.data = new AvailabilityData();

        this.data.setProperties(properties);
        this.data.setMeasurements(measurements);
        this.data.setMessage(message);

        initialize(this.data.getProperties());

        setId(LocalStringsUtils.generateRandomIntegerId());

        setTimestamp(new Date());

        setName(name);
        setRunLocation(runLocation);
        setDuration(duration);
        setSuccess(success);
    }

    /**
     * Gets the ver value from the data object.
     * 
     * @return The ver value.
     */
    @Override
    public int getVer() {
        return getData().getVer();
    }

    /**
     * Gets a map of application-defined request metrics.
     * 
     * @return The map of metrics
     */
    public ConcurrentMap<String, Double> getMetrics() {
        return data.getMeasurements();
    }

    /**
     * Sets the StartTime. Uses the default behavior and sets the property on the
     * 'data' start time.
     * 
     * @param timestamp The timestamp as Date.
     */
    @Override
    public void setTimestamp(Date timestamp) {
        if (timestamp == null) {
            timestamp = new Date();
        }

        super.setTimestamp(timestamp);
    }

    /**
     * Gets or human-readable name of the requested page.
     * 
     * @return A human-readable name.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Sets or human-readable name of the requested page.
     * 
     * @param name A human-readable name.
     */
    public void setName(String name) {
        data.setName(name);
    }

    /**
     * Gets or human-readable name of the run location.
     * 
     * @return A human-readable name.
     */
    public String getRunLocation() {
        return data.getRunLocation();
    }

    /**
     * Sets or human-readable name of the run location.
     * 
     * @param runLocation A human-readable name
     */
    public void setRunLocation(String runLocation) {
        data.setRunLocation(runLocation);
    }

    /**
     * Gets the unique identifier of the request.
     * 
     * @return Unique identifier.
     */
    public String getId() {
        return data.getId();
    }

    /**
     * Sets the unique identifier of the request.
     * 
     * @param id Unique identifier.
     */
    public void setId(String id) {
        data.setId(id);
    }

    /**
     * Gets a value indicating whether application handled the request successfully.
     * 
     * @return Success indication.
     */
    public boolean isSuccess() {
        return data.getSuccess();
    }

    /**
     * Sets a value indicating whether application handled the request successfully.
     * 
     * @param success Success indication.
     */
    public void setSuccess(boolean success) {
        data.setSuccess(success);
    }

    /**
     * Gets the amount of time it took the application to handle the request.
     * 
     * @return Amount of time in milliseconds.
     */
    public Duration getDuration() {
        return data.getDuration();
    }

    /**
     * Sets the amount of time it took the application to handle the request.
     * 
     * @param duration Amount of time in captured in a
     *                 {@link com.microsoft.applicationinsights.telemetry.Duration}.
     */
    public void setDuration(Duration duration) {
        data.setDuration(duration);
    }

    @Override
    public Double getSamplingPercentage() {
        return samplingPercentage;
    }

    @Override
    public void setSamplingPercentage(Double samplingPercentage) {
        this.samplingPercentage = samplingPercentage;
    }

    @Override
    @Deprecated
    protected void additionalSanitize() {
        data.setName(Sanitizer.sanitizeName(data.getName()));
        data.setId(Sanitizer.sanitizeName(data.getId()));
        Sanitizer.sanitizeMeasurements(getMetrics());
    }

    @Override
    protected AvailabilityData getData() {
        return data;
    }

    @Override
    public String getEnvelopName() {
        return ENVELOPE_NAME;
    }

    @Override
    public String getBaseTypeName() {
        return BASE_TYPE;
    }
}
