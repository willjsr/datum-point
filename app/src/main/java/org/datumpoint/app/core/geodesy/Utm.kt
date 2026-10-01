package org.datumpoint.app.core.geodesy

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class UtmCoordinate(
    val easting: Double,
    val northing: Double,
    val zone: Int,
    val hemisphere: Hemisphere,
)

enum class Hemisphere { NORTH, SOUTH }

object Sirgas2000Utm {
    private const val A = 6378137.0
    private const val F = 1.0 / 298.257222101
    private const val K0 = 0.9996
    private const val FALSE_EASTING = 500000.0
    private const val FALSE_NORTHING = 10000000.0
    private val e2 = F * (2.0 - F)
    private val ep2 = e2 / (1.0 - e2)

    fun zoneForLongitude(longitude: Double): Int =
        floor((longitude + 180.0) / 6.0).toInt() + 1

    fun fromLatLon(latitude: Double, longitude: Double, zone: Int = 22): UtmCoordinate {
        require(latitude in -80.0..84.0) { "Latitude outside UTM limits." }
        require(zone in 1..60) { "UTM zone must be between 1 and 60." }

        val lat = latitude.toRadians()
        val lon = longitude.toRadians()
        val lon0 = ((zone - 1) * 6 - 180 + 3).toDouble().toRadians()
        val n = A / sqrt(1.0 - e2 * sin(lat).pow(2))
        val t = tan(lat).pow(2)
        val c = ep2 * cos(lat).pow(2)
        val a = cos(lat) * (lon - lon0)
        val m = meridianArc(lat)

        val easting = FALSE_EASTING + K0 * n * (
            a + (1 - t + c) * a.pow(3) / 6.0 +
                (5 - 18 * t + t.pow(2) + 72 * c - 58 * ep2) * a.pow(5) / 120.0
            )
        var northing = K0 * (
            m + n * tan(lat) * (
                a.pow(2) / 2.0 +
                    (5 - t + 9 * c + 4 * c.pow(2)) * a.pow(4) / 24.0 +
                    (61 - 58 * t + t.pow(2) + 600 * c - 330 * ep2) * a.pow(6) / 720.0
                )
            )
        val hemisphere = if (latitude < 0) Hemisphere.SOUTH else Hemisphere.NORTH
        if (hemisphere == Hemisphere.SOUTH) northing += FALSE_NORTHING
        return UtmCoordinate(easting, northing, zone, hemisphere)
    }

    fun toLatLon(coordinate: UtmCoordinate): Pair<Double, Double> {
        val x = coordinate.easting - FALSE_EASTING
        val y = if (coordinate.hemisphere == Hemisphere.SOUTH) {
            coordinate.northing - FALSE_NORTHING
        } else {
            coordinate.northing
        }
        val lon0 = ((coordinate.zone - 1) * 6 - 180 + 3).toDouble().toRadians()
        val m = y / K0
        val mu = m / (A * (1 - e2 / 4.0 - 3 * e2.pow(2) / 64.0 - 5 * e2.pow(3) / 256.0))
        val e1 = (1 - sqrt(1 - e2)) / (1 + sqrt(1 - e2))
        val fp = mu +
            (3 * e1 / 2 - 27 * e1.pow(3) / 32) * sin(2 * mu) +
            (21 * e1.pow(2) / 16 - 55 * e1.pow(4) / 32) * sin(4 * mu) +
            (151 * e1.pow(3) / 96) * sin(6 * mu) +
            (1097 * e1.pow(4) / 512) * sin(8 * mu)

        val c1 = ep2 * cos(fp).pow(2)
        val t1 = tan(fp).pow(2)
        val n1 = A / sqrt(1 - e2 * sin(fp).pow(2))
        val r1 = A * (1 - e2) / (1 - e2 * sin(fp).pow(2)).pow(1.5)
        val d = x / (n1 * K0)

        val lat = fp - (n1 * tan(fp) / r1) * (
            d.pow(2) / 2 -
                (5 + 3 * t1 + 10 * c1 - 4 * c1.pow(2) - 9 * ep2) * d.pow(4) / 24 +
                (61 + 90 * t1 + 298 * c1 + 45 * t1.pow(2) - 252 * ep2 - 3 * c1.pow(2)) * d.pow(6) / 720
            )
        val lon = lon0 + (
            d - (1 + 2 * t1 + c1) * d.pow(3) / 6 +
                (5 - 2 * c1 + 28 * t1 - 3 * c1.pow(2) + 8 * ep2 + 24 * t1.pow(2)) * d.pow(5) / 120
            ) / cos(fp)
        return lat.toDegrees() to lon.toDegrees()
    }

    fun planarDistanceMeters(aLat: Double, aLon: Double, bLat: Double, bLon: Double): Double {
        val zone = zoneForLongitude((aLon + bLon) / 2.0).coerceIn(1, 60)
        val a = fromLatLon(aLat, aLon, zone)
        val b = fromLatLon(bLat, bLon, zone)
        return kotlin.math.hypot(a.easting - b.easting, a.northing - b.northing)
    }

    private fun meridianArc(lat: Double): Double =
        A * (
            (1 - e2 / 4 - 3 * e2.pow(2) / 64 - 5 * e2.pow(3) / 256) * lat -
                (3 * e2 / 8 + 3 * e2.pow(2) / 32 + 45 * e2.pow(3) / 1024) * sin(2 * lat) +
                (15 * e2.pow(2) / 256 + 45 * e2.pow(3) / 1024) * sin(4 * lat) -
                (35 * e2.pow(3) / 3072) * sin(6 * lat)
            )

    private fun Double.toRadians(): Double = this * PI / 180.0
    private fun Double.toDegrees(): Double = this * 180.0 / PI
}
