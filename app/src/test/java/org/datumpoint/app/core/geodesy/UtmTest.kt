package org.datumpoint.app.core.geodesy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UtmTest {
    @Test
    fun curitibaPointProjectsInsideUtm22SouthRange() {
        val utm = Sirgas2000Utm.fromLatLon(-25.4284, -49.2733, zone = 22)
        assertEquals(22, utm.zone)
        assertEquals(Hemisphere.SOUTH, utm.hemisphere)
        assertTrue(utm.easting in 650000.0..710000.0)
        assertTrue(utm.northing in 7150000.0..7220000.0)
    }

    @Test
    fun projectionRoundTripKeepsCentimeterScaleForRecognitionUse() {
        val latitude = -25.4284
        val longitude = -49.2733
        val utm = Sirgas2000Utm.fromLatLon(latitude, longitude, zone = 22)
        val (roundLat, roundLon) = Sirgas2000Utm.toLatLon(utm)
        assertEquals(latitude, roundLat, 0.0000001)
        assertEquals(longitude, roundLon, 0.0000001)
    }

    @Test
    fun zoneCalculationHandlesWesternHemisphere() {
        assertEquals(22, Sirgas2000Utm.zoneForLongitude(-49.27))
        assertEquals(23, Sirgas2000Utm.zoneForLongitude(-45.0))
    }
}
