package com.amazonaws.geo.util;


import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.amazonaws.geo.model.GeoObject;
import com.amazonaws.geo.model.GeoPoint;
import org.junit.Test;

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


import com.fasterxml.jackson.annotation.JsonIgnore;

class GeoPointOld extends GeoObject {

    protected double[] coordinates;

    public GeoPointOld() {
        type = "Point";
    }

    public GeoPointOld(double latitude, double longitude) {
        this();
        setCoordinates(new double[]{latitude, longitude});
    }

    @JsonIgnore
    public double getLatitude() {
        return coordinates[0];
    }

    @JsonIgnore
    public double getLongitude() {
        return coordinates[1];
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }
}


class GeoJsonMapperOldImplementation {
    private static ObjectMapper mapper = new ObjectMapper();

    public static GeoPointOld geoPointFromString(String jsonString) {
        try {
            return mapper.readValue(jsonString, GeoPointOld.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String stringFromGeoObject(GeoObject geoObject) {
        try {
            return mapper.writeValueAsString(geoObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public class GeoJsonMapperTest {
    private final GeoJsonMapper newMapper = new GeoJsonMapper();
    private final GeoJsonMapperOldImplementation oldMapper = new GeoJsonMapperOldImplementation();

    @Test
    public void assertSameResults() {
        assertGeoPoint(new GeoPoint(10.32333, 23.00122));
        assertGeoPoint(new GeoPoint(0, 0));
        assertGeoPoint(new GeoPoint(-90, 90));
        assertGeoPoint(new GeoPoint(-90.23134243224242, 79.2343242342));
    }

    private void assertGeoPoint(GeoPoint geoPoint) {
        final GeoPoint decodedWithNew = newMapper.geoPointFromString(newMapper.stringFromGeoObject(geoPoint));
        assert decodedWithNew.getLatitude() == geoPoint.getLatitude();
        assert decodedWithNew.getLongitude() == geoPoint.getLongitude();
        assert Objects.equals(decodedWithNew.getType(), geoPoint.getType());
        final GeoPointOld decodedWithOld = oldMapper.geoPointFromString(oldMapper.stringFromGeoObject(new GeoPointOld(geoPoint.getLatitude(), geoPoint.getLongitude())));
        assert decodedWithOld.getLatitude() == geoPoint.getLatitude();
        assert decodedWithOld.getLongitude() == geoPoint.getLongitude();
        assert decodedWithOld.getType().equals(geoPoint.getType());

        final GeoPoint variantTwo = newMapper.geoPointFromString(oldMapper.stringFromGeoObject(new GeoPointOld(geoPoint.getLatitude(), geoPoint.getLongitude())));
        assert variantTwo.getLatitude() == geoPoint.getLatitude();
        assert variantTwo.getLongitude() == geoPoint.getLongitude();
        assert Objects.equals(variantTwo.getType(), geoPoint.getType());

        final GeoPointOld variantThree = oldMapper.geoPointFromString(newMapper.stringFromGeoObject(geoPoint));
        assert variantThree.getLatitude() == geoPoint.getLatitude();
        assert variantThree.getLongitude() == geoPoint.getLongitude();
        assert variantThree.getType().equals(geoPoint.getType());

    }
}
