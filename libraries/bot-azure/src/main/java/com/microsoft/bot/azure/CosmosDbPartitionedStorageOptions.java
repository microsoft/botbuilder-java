// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.azure;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.bot.integration.Configuration;

/**
 * Cosmos DB Partitioned Storage Options.
 */
public class CosmosDbPartitionedStorageOptions {
    private static final Integer DEFAULT_THROUGHPUT = 400;
    private static final ConsistencyLevel DEFAULT_CONSISTENCY = ConsistencyLevel.Session;

    private String cosmosDbEndpoint;
    private String authKey;
    private String databaseId;
    private String containerId;
    private String keySuffix;
    private Integer containerThroughput;
    private ConnectionPolicy connectionPolicy;
    private ConsistencyLevel consistencyLevel;
    private Boolean compatibilityMode;

    /**
     * Constructs an empty options object.
     */
    public CosmosDbPartitionedStorageOptions() {
        connectionPolicy = ConnectionPolicy.GetDefault();
        consistencyLevel = DEFAULT_CONSISTENCY;
        containerThroughput = DEFAULT_THROUGHPUT;
    }

    /**
     * Construct with properties from Configuration.
     *
     * @param configuration The Configuration object to read properties from.
     */
    public CosmosDbPartitionedStorageOptions(Configuration configuration) {
        cosmosDbEndpoint = configuration.getProperty("cosmosdb.dbEndpoint");
        authKey = configuration.getProperty("cosmosdb.authKey");
        databaseId = configuration.getProperty("cosmosdb.databaseId");
        containerId = configuration.getProperty("cosmosdb.containerId");

        // will likely need to expand this to read policy settings from Configuration.
        connectionPolicy = ConnectionPolicy.GetDefault();

        // will likely need to read consistency level from config.
        consistencyLevel = DEFAULT_CONSISTENCY;

        try {
            containerThroughput = Integer.parseInt(configuration.getProperty("cosmosdb.throughput"));
        } catch (NumberFormatException e) {
            containerThroughput = DEFAULT_THROUGHPUT;
        }
    }

    /**
     * Gets the CosmosDB endpoint.
     *
     * @return The DB endpoint.
     */
    public String getCosmosDbEndpoint() {
        return cosmosDbEndpoint;
    }

    /**
     * Sets the CosmosDB endpoint.
     *
     * @param withCosmosDbEndpoint The DB endpoint to use.
     */
    public void setCosmosDbEndpoint(String withCosmosDbEndpoint) {
        cosmosDbEndpoint = withCosmosDbEndpoint;
    }

    /**
     * Gets the authentication key for Cosmos DB.
     *
     * @return The auth key for the DB.
     */
    public String getAuthKey() {
        return authKey;
    }

    /**
     * Sets the authentication key for Cosmos DB.
     *
     * @param withAuthKey The auth key to use.
     */
    public void setAuthKey(String withAuthKey) {
        authKey = withAuthKey;
    }

    /**
     * Gets the database identifier for Cosmos DB instance.
     *
     * @return The CosmosDB DB id.
     */
    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * Sets the database identifier for Cosmos DB instance.
     *
     * @param withDatabaseId The CosmosDB id.
     */
    public void setDatabaseId(String withDatabaseId) {
        databaseId = withDatabaseId;
    }

    /**
     * Gets the container identifier.
     *
     * @return The container/collection ID.
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * Sets the container identifier.
     *
     * @param withContainerId The container/collection ID.
     */
    public void setContainerId(String withContainerId) {
        containerId = withContainerId;
    }

    /**
     * Gets the ConnectionPolicy for the CosmosDB.
     *
     * @return The ConnectionPolicy settings.
     */
    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Sets the ConnectionPolicy for the CosmosDB.
     *
     * @param withConnectionPolicy The ConnectionPolicy settings.
     */
    public void setConnectionPolicy(ConnectionPolicy withConnectionPolicy) {
        connectionPolicy = withConnectionPolicy;
    }

    /**
     * Represents the consistency levels supported for Azure Cosmos DB client
     * operations in the Azure Cosmos DB database service.
     *
     * The requested ConsistencyLevel must match or be weaker than that provisioned
     * for the database account. Consistency levels by order of strength are Strong,
     * BoundedStaleness, Session and Eventual.
     *
     * @return The ConsistencyLevel
     */
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Represents the consistency levels supported for Azure Cosmos DB client
     * operations in the Azure Cosmos DB database service.
     *
     * The requested ConsistencyLevel must match or be weaker than that provisioned
     * for the database account. Consistency levels by order of strength are Strong,
     * BoundedStaleness, Session and Eventual.
     *
     * @param withConsistencyLevel The ConsistencyLevel to use.
     */
    public void setConsistencyLevel(ConsistencyLevel withConsistencyLevel) {
        consistencyLevel = withConsistencyLevel;
    }

    /**
     * Gets the throughput set when creating the Container. Defaults to 400.
     *
     * @return The container throughput.
     */
    public Integer getContainerThroughput() {
        return containerThroughput;
    }

    /**
     * Sets the throughput set when creating the Container. Defaults to 400.
     *
     * @param withContainerThroughput The desired thoughput.
     */
    public void setContainerThroughput(Integer withContainerThroughput) {
        containerThroughput = withContainerThroughput;
    }

    /**
     * Gets a value indicating whether or not to run in Compatibility Mode. Early
     * versions of CosmosDb had a key length limit of 255. Keys longer than this
     * were truncated in CosmosDbKeyEscape. This remains the default behavior, but
     * can be overridden by setting CompatibilityMode to false. This setting will
     * also allow for using older collections where no PartitionKey was specified.
     *
     * Note: CompatibilityMode cannot be 'true' if KeySuffix is used.
     * 
     * @return The compatibilityMode
     */
    public Boolean getCompatibilityMode() {
        return compatibilityMode;
    }

    /**
     * Sets a value indicating whether or not to run in Compatibility Mode. Early
     * versions of CosmosDb had a key length limit of 255. Keys longer than this
     * were truncated in CosmosDbKeyEscape. This remains the default behavior, but
     * can be overridden by setting CompatibilityMode to false. This setting will
     * also allow for using older collections where no PartitionKey was specified.
     *
     * Note: CompatibilityMode cannot be 'true' if KeySuffix is used.
     *
     * @param withCompatibilityMode Currently, max key length for cosmosdb is 1023:
     *                              https://docs.microsoft.com/en-us/azure/cosmos-db/concepts-limits#per-item-limits
     *                              The default for backwards compatibility is 255,
     *                              CosmosDbKeyEscape.MaxKeyLength.
     */
    public void setCompatibilityMode(Boolean withCompatibilityMode) {
        this.compatibilityMode = withCompatibilityMode;
    }

    /**
     * Gets the suffix to be added to every key. See
     * CosmosDbKeyEscape.EscapeKey(string). Note:CompatibilityMode must be set to
     * 'false' to use a KeySuffix. When KeySuffix is used, keys will NOT be
     * truncated but an exception will be thrown if the key length is longer than
     * allowed by CosmosDb.
     *
     * @return String containing only valid CosmosDb key characters. (e.g. not:
     *         '\\', '?', '/', '#', '*').
     */
    public String getKeySuffix() {
        return keySuffix;
    }

    /**
     * Sets the suffix to be added to every key. See
     * CosmosDbKeyEscape.EscapeKey(string). Note:CompatibilityMode must be set to
     * 'false' to use a KeySuffix. When KeySuffix is used, keys will NOT be
     * truncated but an exception will be thrown if the key length is longer than
     * allowed by CosmosDb.
     *
     * @param withKeySuffix String containing only valid CosmosDb key characters.
     *                      (e.g. not: '\\', '?', '/', '#', '*').
     */
    public void setKeySuffix(String withKeySuffix) {
        this.keySuffix = withKeySuffix;
    }
}
