package com.amazonaws.geo

import software.amazon.awssdk.services.dynamodb.DynamoDbClient

class GeoDataManagerConfiguration(
    val dynamoDBClient: DynamoDbClient,
    val tableName: String,
    val hashKeyAttributeName: String = DEFAULT_HASHKEY_ATTRIBUTE_NAME,
    val rangeKeyAttributeName: String = DEFAULT_RANGEKEY_ATTRIBUTE_NAME,
    val geohashAttributeName: String = DEFAULT_GEOHASH_ATTRIBUTE_NAME,
    val latitudeAttributeName: String = DEFAULT_LATITUDE_ATTRIBUTE_NAME,
    val longitudeAttributeName: String = DEFAULT_LONGITUDE_ATTRIBUTE_NAME,
    val geohashIndexName: String? = null,
    val hashKeyLength: Int = DEFAULT_HASHKEY_LENGTH,
    val maxDynamoQueriesForRequest: Int = 50
) {
    companion object {
        // Public constants
        const val MERGE_THRESHOLD: Long = 2

        // Default values
        private const val DEFAULT_HASHKEY_ATTRIBUTE_NAME = "hashKey"
        private const val DEFAULT_RANGEKEY_ATTRIBUTE_NAME = "rangeKey"
        private const val DEFAULT_GEOHASH_ATTRIBUTE_NAME = "geohash"
        private const val DEFAULT_LATITUDE_ATTRIBUTE_NAME = "lat"
        private const val DEFAULT_LONGITUDE_ATTRIBUTE_NAME = "lng"

        private const val DEFAULT_GEOHASH_INDEX_ATTRIBUTE_NAME = "geohash-index"

        private const val DEFAULT_HASHKEY_LENGTH = 6

        private const val DEFAULT_THREAD_POOL_SIZE = 10
    }
}