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

package com.amazonaws.geo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class GeoQueryResult extends GeoDataResult {
	private List<Map<String, AttributeValue>> item;
	private List<QueryResponse> queryResults;

	public GeoQueryResult() {
		item = Collections.synchronizedList(new ArrayList<Map<String, AttributeValue>>());
		queryResults = Collections.synchronizedList(new ArrayList<QueryResponse>());
	}

	public GeoQueryResult(GeoQueryResult geoQueryResult) {
		this();

		item = geoQueryResult.getItem();
		queryResults = geoQueryResult.getQueryResults();
	}

	public List<Map<String, AttributeValue>> getItem() {
		return item;
	}

	public List<QueryResponse> getQueryResults() {
		return queryResults;
	}
}
