/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 * http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.geo

import com.amazonaws.geo.dynamodb.internal.DynamoDBManager
import com.amazonaws.geo.model.*
import com.amazonaws.geo.s2.internal.S2Manager
import com.amazonaws.geo.s2.internal.S2Util
import com.google.common.geometry.S2CellUnion
import com.google.common.geometry.S2LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import java.util.*

/**
 *
 *
 * Manager to hangle geo spatial data in Amazon DynamoDB tables. All service calls made using this client are blocking,
 * and will not return until the service call completes.
 *
 *
 *
 * This class is designed to be thread safe; however, once constructed GeoDataManagerConfiguration should not be
 * modified. Modifying GeoDataManagerConfiguration may cause unspecified behaviors.
 *
 */
class GeoDataManager(
    /**
     *
     *
     * Return GeoDataManagerConfiguration. The returned GeoDataManagerConfiguration should not be modified.
     *
     *
     * @return
     * GeoDataManagerConfiguration that is used to configure this GeoDataManager.
     */
    val geoDataManagerConfiguration: GeoDataManagerConfiguration
) {
    private val dynamoDBManager: DynamoDBManager = DynamoDBManager(this.geoDataManagerConfiguration)

    /**
     *
     *
     * Put a point into the Amazon DynamoDB table. Once put, you cannot update attributes specified in
     * GeoDataManagerConfiguration: hash key, range key, geohash and geoJson. If you want to update these columns, you
     * need to insert a new record and delete the old record.
     *
     * **Sample usage:**
     *
     * <pre>
     * GeoPoint geoPoint = new GeoPoint(47.5, -122.3);
     * AttributeValue rangeKeyValue = AttributeValue.fromS(&quot;a6feb446-c7f2-4b48-9b3a-0f87744a5047&quot;);
     * AttributeValue titleValue = AttributeValue.fromS(&quot;Original title&quot;);
     *
     * PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyValue);
     * putPointRequest.getPutItemRequest().getItem().put(&quot;title&quot;, titleValue);
     *
     * PutPointResult putPointResult = geoDataManager.putPoint(putPointRequest);
    </pre> *
     *
     * @param putPointRequest
     * Container for the necessary parameters to execute put point request.
     *
     * @return Result of put point request.
     */
    fun putPoint(putPointRequest: PutPointRequest): PutPointResult? {
        return dynamoDBManager.putPoint(putPointRequest)
    }


    /**
     *
     *
     * Query a circular area constructed by a center point and its radius.
     *
     * **Sample usage:**
     *
     * <pre>
     * GeoPoint centerPoint = new GeoPoint(47.5, -122.3);
     *
     * QueryRadiusRequest queryRadiusRequest = new QueryRadiusRequest(centerPoint, 100);
     * QueryRadiusResult queryRadiusResult = geoIndexManager.queryRadius(queryRadiusRequest);
     *
     * for (Map&lt;String, AttributeValue&gt; item : queryRadiusResult.getItem()) {
     * System.out.println(&quot;item: &quot; + item);
     * }
    </pre> *
     *
     * @param queryRadiusRequest
     * Container for the necessary parameters to execute radius query request.
     *
     * @return Result of radius query request.
     */
    suspend fun queryRadius(queryRadiusRequest: QueryRadiusRequest): List<Map<String, AttributeValue>> {
        val latLngRect = S2Util.getBoundingLatLngRect(queryRadiusRequest)

        val ranges = mergeCells(S2Manager.findCellIds(latLngRect))

        return dispatchQueries(ranges, queryRadiusRequest)
    }

    /**
     * Merge continuous cells in cellUnion and return a list of merged GeohashRanges.
     *
     * @param cellUnion
     * Container for multiple cells.
     *
     * @return A list of merged GeohashRanges.
     */
    private fun mergeCells(cellUnion: S2CellUnion): MutableList<GeohashRange> {
        val ranges: MutableList<GeohashRange> = ArrayList()
        for (c in cellUnion.cellIds()) {
            val range = GeohashRange(c.rangeMin().id(), c.rangeMax().id())

            var wasMerged = false
            for (r in ranges) {
                if (r.tryMerge(range)) {
                    wasMerged = true
                    break
                }
            }

            if (!wasMerged) {
                ranges.add(range)
            }
        }

        return ranges
    }

    /**
     * Query Amazon DynamoDB in parallel and filter the result.
     *
     * @param ranges
     * A list of geohash ranges that will be used to query Amazon DynamoDB.
     *
     * @return Aggregated and filtered items returned from Amazon DynamoDB.
     */
    private suspend fun dispatchQueries(
        ranges: MutableList<GeohashRange>,
        geoQueryRequest: QueryRadiusRequest
    ): List<Map<String, AttributeValue>> {
        val ranges = ranges

        val hashKeyLength = geoDataManagerConfiguration.hashKeyLength
        val allRanges = ranges.flatMap {
            it.trySplit(hashKeyLength)
        }

        if (allRanges.size > geoDataManagerConfiguration.maxDynamoQueriesForRequest) {
            throw Exception("Too many range to query for ${geoQueryRequest.centerPoint.latitude} ${geoQueryRequest.centerPoint.longitude}: ${allRanges.size} ranges.")
        }
        return coroutineScope {
            val allResults = allRanges.flatMap { range ->
                executeGeoQueryForRange(request = geoQueryRequest, range = range)
            }
            ensureActive()
            val finalResult: MutableList<MutableMap<String, AttributeValue>> = LinkedList()
            allResults.forEach { queryResponse ->
                val filtered = filter(queryResponse.items(), geoQueryRequest)
                finalResult.addAll(filtered)
            }
            finalResult
        }

    }

    /**
     * Filter out any points outside of the queried area from the input list.
     *
     * @param list
     * List of items return by Amazon DynamoDB. It may contains points outside of the actual area queried.
     *
     *
     * @return List of items within the queried area.
     */
    private fun filter(
        list: MutableList<MutableMap<String, AttributeValue>>,
        geoQueryRequest: GeoQueryRequest?
    ): MutableList<MutableMap<String, AttributeValue>> {
        val result: MutableList<MutableMap<String, AttributeValue>> = LinkedList()

        var centerLatLng: S2LatLng? = null
        var radiusInMeter = 0.0
        if (geoQueryRequest is QueryRadiusRequest) {
            val centerPoint = geoQueryRequest.getCenterPoint()
            centerLatLng = S2LatLng.fromDegrees(centerPoint.getLatitude(), centerPoint.getLongitude())

            radiusInMeter = geoQueryRequest.getRadiusInMeter()
        }

        for (item in list) {
            val latitude = item[geoDataManagerConfiguration.latitudeAttributeName]!!.n().toDouble()
            val longitude = item[geoDataManagerConfiguration.longitudeAttributeName]!!.n().toDouble()

            val latLng = S2LatLng.fromDegrees(latitude, longitude)
            if (centerLatLng != null && radiusInMeter > 0 && centerLatLng.getEarthDistance(latLng) <= radiusInMeter) {
                result.add(item)
            }
        }

        return result
    }

    private fun CoroutineScope.executeGeoQueryForRange(
        request: QueryRadiusRequest,
        range: GeohashRange
    ): List<QueryResponse> {
        val hashKey = S2Manager.generateHashKey(range.rangeMin, geoDataManagerConfiguration.hashKeyLength)
        ensureActive()
        return dynamoDBManager.queryGeohash(hashKey, range, request.hashKeyPrefix)
    }
}