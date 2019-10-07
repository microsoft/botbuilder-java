/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.protocol.SerializerAdapter;
import org.junit.Assert;
import org.junit.Test;

public class CloudErrorDeserializerTests {
    @Test
    public void cloudErrorDeserialization() throws Exception {
        SerializerAdapter<ObjectMapper> serializerAdapter = new AzureJacksonAdapter();
        String bodyString =
            "{" +
            "    \"error\": {" +
            "        \"code\": \"BadArgument\"," +
            "        \"message\": \"The provided database ‘foo’ has an invalid username.\"," +
            "        \"target\": \"query\"," +
            "        \"details\": [" +
            "            {" +
            "                \"code\": \"301\"," +
            "                \"target\": \"$search\"," +
            "                \"message\": \"$search query option not supported\"" +
            "            }" +
            "        ]," +
            "        \"additionalInfo\": [" +
            "            {" +
            "                \"type\": \"SomeErrorType\"," +
            "                \"info\": {" +
            "                    \"someProperty\": \"SomeValue\"" +
            "                }" +
            "            }" +
            "        ]" +
            "    }" +
            "}";
        
        CloudError cloudError = serializerAdapter.deserialize(bodyString, CloudError.class);

        Assert.assertEquals("BadArgument", cloudError.code());
        Assert.assertEquals("The provided database ‘foo’ has an invalid username.", cloudError.message());
        Assert.assertEquals("query", cloudError.target());
        Assert.assertEquals(1, cloudError.details().size());
        Assert.assertEquals("301", cloudError.details().get(0).code());
        Assert.assertEquals(1, cloudError.additionalInfo().size());
        Assert.assertEquals("SomeErrorType", cloudError.additionalInfo().get(0).type());
        Assert.assertEquals("SomeValue", cloudError.additionalInfo().get(0).info().get("someProperty").asText());
    }
    
    @Test
    public void cloudErrorWithPolicyViolationDeserialization() throws Exception {
        SerializerAdapter<ObjectMapper> serializerAdapter = new AzureJacksonAdapter();
        String bodyString =
            "{" +
            "    \"error\": {" +
            "        \"code\": \"BadArgument\"," +
            "        \"message\": \"The provided database ‘foo’ has an invalid username.\"," +
            "        \"target\": \"query\"," +
            "        \"details\": [" +
            "        {" +
            "            \"code\": \"301\"," +
            "            \"target\": \"$search\"," +
            "            \"message\": \"$search query option not supported\"," +
            "            \"additionalInfo\": [" +
            "            {" +
            "                \"type\": \"PolicyViolation\"," +
            "                \"info\": {" +
            "                    \"policyDefinitionDisplayName\": \"Allowed locations\"," +
            "                    \"policyDefinitionId\": \"testDefinitionId\"," +
            "                    \"policyDefinitionName\": \"testDefinitionName\"," +
            "                    \"policyDefinitionEffect\": \"deny\"," +
            "                    \"policyAssignmentId\": \"testAssignmentId\"," +
            "                    \"policyAssignmentName\": \"testAssignmentName\"," +
            "                    \"policyAssignmentDisplayName\": \"test assignment\"," +
            "                    \"policyAssignmentScope\": \"/subscriptions/testSubId/resourceGroups/jilimpolicytest2\"," +
            "                    \"policyAssignmentParameters\": {" +
            "                        \"listOfAllowedLocations\": {" +
            "                            \"value\": [" +
            "                                \"westus\"" +
            "                	         ]" +
            "                    	 }" +
            "                    }" +            
            "                }" +
            "            }" +
            "            ]" +
            "        }" +
            "        ]," +
            "        \"additionalInfo\": [" +
            "            {" +
            "                \"type\": \"SomeErrorType\"," +
            "                \"info\": {" +
            "                    \"someProperty\": \"SomeValue\"" +
            "                }" +
            "            }" +
            "        ]" +
            "    }" +
            "}";
        
        CloudError cloudError = serializerAdapter.deserialize(bodyString, CloudError.class);

        Assert.assertEquals("BadArgument", cloudError.code());
        Assert.assertEquals("The provided database ‘foo’ has an invalid username.", cloudError.message());
        Assert.assertEquals("query", cloudError.target());
        Assert.assertEquals(1, cloudError.details().size());
        Assert.assertEquals("301", cloudError.details().get(0).code());
        Assert.assertEquals(1, cloudError.additionalInfo().size());
        Assert.assertEquals("SomeErrorType", cloudError.additionalInfo().get(0).type());
        Assert.assertEquals("SomeValue", cloudError.additionalInfo().get(0).info().get("someProperty").asText());
        Assert.assertEquals(1, cloudError.details().get(0).additionalInfo().size());
        Assert.assertTrue(cloudError.details().get(0).additionalInfo().get(0) instanceof PolicyViolation);
        
        PolicyViolation policyViolation = (PolicyViolation)cloudError.details().get(0).additionalInfo().get(0);
        
        Assert.assertEquals("PolicyViolation", policyViolation.type());
        Assert.assertEquals("Allowed locations", policyViolation.policyErrorInfo().getPolicyDefinitionDisplayName());
        Assert.assertEquals("westus", policyViolation.policyErrorInfo().getPolicyAssignmentParameters().get("listOfAllowedLocations").getValue().elements().next().asText());
    }
    
    @Test
    public void cloudErrorWitDifferentCasing() throws Exception {
        SerializerAdapter<ObjectMapper> serializerAdapter = new AzureJacksonAdapter();
        String bodyString =
            "{" +
            "    \"error\": {" +
            "        \"Code\": \"BadArgument\"," +
            "        \"Message\": \"The provided database ‘foo’ has an invalid username.\"," +
            "        \"Target\": \"query\"," +
            "        \"Details\": [" +
            "        {" +
            "            \"Code\": \"301\"," +
            "            \"Target\": \"$search\"," +
            "            \"Message\": \"$search query option not supported\"," +
            "            \"AdditionalInfo\": [" +
            "            {" +
            "                \"Type\": \"PolicyViolation\"," +
            "                \"Info\": {" +
            "                    \"PolicyDefinitionDisplayName\": \"Allowed locations\"," +
            "                    \"PolicyAssignmentParameters\": {" +
            "                        \"listOfAllowedLocations\": {" +
            "                            \"Value\": [" +
            "                                \"westus\"" +
            "                	         ]" +
            "                    	 }" +
            "                    }" +            
            "                }" +
            "            }" +
            "            ]" +
            "        }" +
            "        ]" +
            "    }" +
            "}";
        
        CloudError cloudError = serializerAdapter.deserialize(bodyString, CloudError.class);

        Assert.assertEquals("BadArgument", cloudError.code());
        Assert.assertEquals("The provided database ‘foo’ has an invalid username.", cloudError.message());
        Assert.assertEquals("query", cloudError.target());
        Assert.assertEquals(1, cloudError.details().size());
        Assert.assertEquals("301", cloudError.details().get(0).code());
        Assert.assertEquals(1, cloudError.details().get(0).additionalInfo().size());
        Assert.assertTrue(cloudError.details().get(0).additionalInfo().get(0) instanceof PolicyViolation);
        
        PolicyViolation policyViolation = (PolicyViolation)cloudError.details().get(0).additionalInfo().get(0);
        
        Assert.assertEquals("PolicyViolation", policyViolation.type());
        Assert.assertEquals("Allowed locations", policyViolation.policyErrorInfo().getPolicyDefinitionDisplayName());
        Assert.assertEquals("westus", policyViolation.policyErrorInfo().getPolicyAssignmentParameters().get("listOfAllowedLocations").getValue().elements().next().asText());
    }
}