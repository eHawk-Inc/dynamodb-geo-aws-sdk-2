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

package com.amazonaws.geo.util;

import com.amazonaws.geo.model.GeoPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoJsonMapper {

	public static GeoPoint geoPointFromString(String jsonString) {
		try {
			final JSONObject object = new JSONObject(jsonString);
			final JSONArray coordinates = object.getJSONArray("coordinates");
			return new GeoPoint(coordinates.getDouble(0), coordinates.getDouble(1));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static String stringFromGeoObject(GeoPoint geoObject) {
		try {
			JSONObject jsonObject = new JSONObject();
			final JSONArray coordinates = new JSONArray();
			coordinates.put(0, geoObject.getLatitude());
			coordinates.put(1, geoObject.getLongitude());
			jsonObject.put("coordinates", coordinates);
			return jsonObject.toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
