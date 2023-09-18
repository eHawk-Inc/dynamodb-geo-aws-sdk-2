/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.geo.dynamodb.internal;

import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.GeohashRange;
import com.amazonaws.geo.model.PutPointRequest;
import com.amazonaws.geo.model.PutPointResult;
import com.amazonaws.geo.s2.internal.S2Manager;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBManager {
	private final GeoDataManagerConfiguration config;

	public DynamoDBManager(GeoDataManagerConfiguration config) {
		this.config = config;
	}

	public List<QueryResponse> queryGeohash(long hashKey, GeohashRange range) {
		return queryGeohash(hashKey, range, null);
	}

	/**
	 * Query Amazon DynamoDB
	 *
	 * @param hashKey
	 *            Hash key for the query request.
	 *
	 * @param range
	 *            The range of geohashs to query.
	 * @param hashKeyPrefix hash key prefix (nullable)
	 *
	 * @return The query result.
	 */
	public List<QueryResponse> queryGeohash(long hashKey, GeohashRange range, String hashKeyPrefix) {
		List<QueryResponse> queryResults = new ArrayList<>();
		Map<String, AttributeValue> lastEvaluatedKey = null;

		do {
			Map<String, Condition> keyConditions = new HashMap<>();

			Condition hashKeyCondition = Condition.builder().comparisonOperator(ComparisonOperator.EQ)
					.attributeValueList(getHashKeyAttributeValue(hashKey, hashKeyPrefix))
					.build();
			keyConditions.put(config.getHashKeyAttributeName(), hashKeyCondition);

			AttributeValue minRange = AttributeValue.fromN(Long.toString(range.getRangeMin()));
			AttributeValue maxRange = AttributeValue.fromN(Long.toString(range.getRangeMax()));

			Condition geohashCondition = Condition.builder().comparisonOperator(ComparisonOperator.BETWEEN)
					.attributeValueList(minRange, maxRange).build();
			keyConditions.put(config.getGeohashAttributeName(), geohashCondition);

			Map<String, AttributeValue> finalLastEvaluatedKey = lastEvaluatedKey;
			QueryRequest queryRequest = QueryRequest.builder().tableName(config.getTableName()).keyConditions(keyConditions)
					.indexName(config.getGeohashIndexName()).consistentRead(true)
					.returnConsumedCapacity(ReturnConsumedCapacity.TOTAL).exclusiveStartKey(finalLastEvaluatedKey).build();

			QueryResponse queryResult = config.getDynamoDBClient().query(queryRequest);
			queryResults.add(queryResult);

			lastEvaluatedKey = queryResult.hasLastEvaluatedKey() ? queryResult.lastEvaluatedKey() : null;

		} while (lastEvaluatedKey != null);

		return queryResults;
	}

	public PutPointResult putPoint(PutPointRequest putPointRequest) {
		long geohash = S2Manager.generateGeohash(putPointRequest.getGeoPoint());
		long hashKey = S2Manager.generateHashKey(geohash, config.getHashKeyLength());

		PutItemRequest request = putPointRequest.getPutItemRequestBuilder().build();
		Map <String, AttributeValue> itemAttributes = request.item();
		Map<String, AttributeValue> item = itemAttributes != null ? new HashMap<>(itemAttributes) : new HashMap<>();

		AttributeValue hashKeyValue = getHashKeyAttributeValue(hashKey, putPointRequest.getHashKeyPrefix());
		item.put(config.getHashKeyAttributeName(), hashKeyValue);
		item.put(config.getRangeKeyAttributeName(), putPointRequest.getRangeKeyValue());

		AttributeValue geohashValue = AttributeValue.fromN(Long.toString(geohash));
		item.put(config.getGeohashAttributeName(), geohashValue);
		AttributeValue latitudeValue = AttributeValue.fromN(String.valueOf(putPointRequest.getGeoPoint().getLatitude()));
		AttributeValue longitudeValue = AttributeValue.fromN(String.valueOf(putPointRequest.getGeoPoint().getLongitude()));
		item.put(config.getLatitudeAttributeName(), latitudeValue);
		item.put(config.getLongitudeAttributeName(), longitudeValue);

		PutItemRequest requestToExecute = request.copy(builder ->
				builder.tableName(config.getTableName()).item(item)
		);
		PutItemResponse putItemResult = config.getDynamoDBClient().putItem(requestToExecute);

		return new PutPointResult(putItemResult);
	}

	private static AttributeValue getHashKeyAttributeValue(long hashKey, String hashKeyPrefix) {
		return hashKeyPrefix == null ? AttributeValue.fromS(String.valueOf(hashKey)) :
				AttributeValue.fromS(hashKeyPrefix + hashKey);
	}
}
