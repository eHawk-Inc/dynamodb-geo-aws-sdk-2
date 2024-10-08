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

public class QueryRadiusRequest extends GeoQueryRequest {
	private GeoPoint centerPoint;
	private double radiusInMeter;

	private final String hashKeyPrefix;

	public QueryRadiusRequest(GeoPoint centerPoint, double radiusInMeter, String hashKeyPrefix) {
		this.centerPoint = centerPoint;
		this.radiusInMeter = radiusInMeter;
		this.hashKeyPrefix = hashKeyPrefix;
	}

	public QueryRadiusRequest(GeoPoint centerPoint, double radiusInMeter) {
		this(centerPoint, radiusInMeter, null);
	}

	public GeoPoint getCenterPoint() {
		return centerPoint;
	}

	public double getRadiusInMeter() {
		return radiusInMeter;
	}

	public String getHashKeyPrefix() {
		return hashKeyPrefix;
	}
}
