// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MT License.

package com.microsoft.bot.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.microsoft.bot.connector.authentication.AllowedCallersClaimsValidator;
import com.microsoft.bot.connector.authentication.AuthenticationConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class AllowedCallersClaimsValidationTests {

    private final String version = "1.0";

    private final String audienceClaim = UUID.randomUUID().toString();

    public static List<Pair<String, List<String>>> getConfigureServicesSucceedsData() {
        String primaryAppId = UUID.randomUUID().toString();
        String secondaryAppId = UUID.randomUUID().toString();

        List<Pair<String, List<String>>> resultList = new ArrayList<Pair<String, List<String>>>();
        // Null allowed callers
        resultList.add(Pair.of(null, null));
        // Null configuration with attempted caller
        resultList.add(Pair.of(primaryAppId, null));
        // Empty allowed callers array
        resultList.add(Pair.of(null, new ArrayList<String>()));
        // Allow any caller
        ArrayList<String> anyCaller = new ArrayList<String>();
        anyCaller.add("*");
        resultList.add(Pair.of(primaryAppId, anyCaller));
        // Specify allowed caller
        ArrayList<String> allowedCaller = new ArrayList<String>();
        allowedCaller.add(primaryAppId);
        resultList.add((Pair.of(primaryAppId, allowedCaller)));
        // Specify multiple callers
        ArrayList<String> multipleCallers = new ArrayList<String>();
        multipleCallers.add(primaryAppId);
        multipleCallers.add(secondaryAppId);
        resultList.add((Pair.of(primaryAppId, multipleCallers)));
        // Blocked caller throws exception
        ArrayList<String> blockedCallers = new ArrayList<String>();
        blockedCallers.add(secondaryAppId);
        resultList.add((Pair.of(primaryAppId, blockedCallers)));
        return resultList;
    }

    @Test
    public void TestAcceptAllowedCallersArray() {
        List<Pair<String, List<String>>> configuredServices = getConfigureServicesSucceedsData();
        for (Pair<String, List<String>> item : configuredServices) {
            acceptAllowedCallersArray(item.getLeft(), item.getRight());
        }
    }


    public void acceptAllowedCallersArray(String allowedCallerClaimId,  List<String> allowList) {
        AllowedCallersClaimsValidator validator = new AllowedCallersClaimsValidator(allowList);

        if (allowedCallerClaimId != null) {
            Map<String, String> claims = createCallerClaims(allowedCallerClaimId);

            if (allowList != null) {
                if (allowList.contains(allowedCallerClaimId) || allowList.contains("*")) {
                     validator.validateClaims(claims);
                } else {
                     validateUnauthorizedAccessException(allowedCallerClaimId, validator, claims);
                }
            } else {
                 validateUnauthorizedAccessException(allowedCallerClaimId, validator, claims);
            }
        }
    }

    private static void validateUnauthorizedAccessException(
        String allowedCallerClaimId,
        AllowedCallersClaimsValidator validator,
        Map<String, String> claims) {
        try {
            validator.validateClaims(claims);
        } catch (RuntimeException exception) {
            Assert.assertTrue(exception.getMessage().contains(allowedCallerClaimId));
        }
    }

    private Map<String, String> createCallerClaims(String appId) {
        Map<String, String> callerClaimMap = new HashMap<String, String>();

            callerClaimMap.put(AuthenticationConstants.APPID_CLAIM, appId);
            callerClaimMap.put(AuthenticationConstants.VERSION_CLAIM, version);
            callerClaimMap.put(AuthenticationConstants.AUDIENCE_CLAIM, audienceClaim);
        return callerClaimMap;
    }
}

