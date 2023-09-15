package com.amazonaws.geo.util;


import com.amazonaws.geo.model.GeoPoint;
import org.junit.Test;

import java.util.Objects;

public class GeoJsonMapperTest {
    private final GeoJsonMapper mapper = new GeoJsonMapper();

    @Test
    public void assertSameResults() {
        assertGeoPoint(new GeoPoint(10.32333, 23.00122));
        assertGeoPoint(new GeoPoint(0, 0));
        assertGeoPoint(new GeoPoint(-90, 90));
        assertGeoPoint(new GeoPoint(-90.23134243224242, 79.2343242342));
    }

    private void assertGeoPoint(GeoPoint geoPoint) {
        final GeoPoint decoded = mapper.geoPointFromString(mapper.stringFromGeoObject(geoPoint));
        assert decoded.getLatitude() == geoPoint.getLatitude();
        assert decoded.getLongitude() == geoPoint.getLongitude();
        assert Objects.equals(decoded.getType(), geoPoint.getType());
    }
}
