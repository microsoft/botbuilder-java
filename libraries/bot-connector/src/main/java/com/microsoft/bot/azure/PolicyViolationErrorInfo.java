/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.azure;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * An instance of this class provides Azure policy violation information.
 */
public class PolicyViolationErrorInfo {
    /**
     * The policy definition id.
     */
    private String policyDefinitionId;

    /**
     * The policy set definition id.
     */
    private String policySetDefinitionId;

    /**
     * The policy definition instance id inside a policy set.
     */
    private String policyDefinitionReferenceId;

    /**
     * The policy set definition name.
     */
    private String policySetDefinitionName;

    /**
     * The policy definition name.
     */
    private String policyDefinitionName;

    /**
     * The policy definition action.
     */
    private String policyDefinitionEffect;

    /**
     * The policy assignment id.
     */
    private String policyAssignmentId;

    /**
     * The policy assignment name.
     */
    private String policyAssignmentName;

    /**
     * The policy assignment display name.
     */
    private String policyAssignmentDisplayName;

    /**
     * The policy assignment scope.
     */
    private String policyAssignmentScope;

    /**
     * The policy assignment parameters.
     */
    private HashMap<String, PolicyParameter> policyAssignmentParameters;

    /**
     * The policy definition display name.
     */
    private String policyDefinitionDisplayName;

    /**
     * The policy set definition display name.
     */
    private String policySetDefinitionDisplayName;

    /**
     * @return the policy definition id.
     */
    public String getPolicyDefinitionId() {
        return policyDefinitionId;
    }

    /**
     * @return the policy set definition id.
     */
    public String getPolicySetDefinitionId() {
        return policySetDefinitionId;
    }

    /**
     * @return the policy definition instance id inside a policy set.
     */
    public String getPolicyDefinitionReferenceId() {
        return policyDefinitionReferenceId;
    }

    /**
     * @return the policy set definition name.
     */
    public String getPolicySetDefinitionName() {
        return policySetDefinitionName;
    }

    /**
     * @return the policy definition name.
     */
    public String getPolicyDefinitionName() {
        return policyDefinitionName;
    }

    /**
     * @return the policy definition action.
     */
    public String getPolicyDefinitionEffect() {
        return policyDefinitionEffect;
    }

    /**
     * @return the policy assignment id.
     */
    public String getPolicyAssignmentId() {
        return policyAssignmentId;
    }

    /**
     * @return the policy assignment name.
     */
    public String getPolicyAssignmentName() {
        return policyAssignmentName;
    }

    /**
     * @return the policy assignment display name.
     */
    public String getPolicyAssignmentDisplayName() {
        return policyAssignmentDisplayName;
    }

    /**
     * @return the policy assignment scope.
     */
    public String getPolicyAssignmentScope() {
        return policyAssignmentScope;
    }

    /**
     * @return the policy assignment parameters.
     */
    public HashMap<String, PolicyParameter> getPolicyAssignmentParameters() {
        return policyAssignmentParameters;
    }

    /**
     * @return the policy definition display name.
     */
    public String getPolicyDefinitionDisplayName() {
        return policyDefinitionDisplayName;
    }

    /**
     * @return the policy set definition display name.
     */
    public String getPolicySetDefinitionDisplayName() {
        return policySetDefinitionDisplayName;
    }

    /**
     * An instance of this class provides policy parameter value.
     */
    public static class PolicyParameter {
        private JsonNode value;

        /**
         * @return the parameter value.
         */
        public JsonNode getValue() {
            return value;
        }
    }
}
