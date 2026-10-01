package org.datumpoint.app.core.geometry

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

data class Point2d(val name: String, val easting: Double, val northing: Double)

object Geometry {
    fun distance(a: Point2d, b: Point2d): Double =
        hypot(b.easting - a.easting, b.northing - a.northing)

    fun azimuthDegrees(a: Point2d, b: Point2d): Double {
        val radians = atan2(b.easting - a.easting, b.northing - a.northing)
        return ((radians * 180.0 / PI) + 360.0) % 360.0
    }

    fun perimeter(points: List<Point2d>, closed: Boolean = true): Double {
        if (points.size < 2) return 0.0
        val segments = points.zipWithNext().sumOf { (a, b) -> distance(a, b) }
        return if (closed && points.size > 2) segments + distance(points.last(), points.first()) else segments
    }

    fun signedArea(points: List<Point2d>): Double {
        if (points.size < 3) return 0.0
        val sum = points.indices.sumOf { index ->
            val a = points[index]
            val b = points[(index + 1) % points.size]
            a.easting * b.northing - b.easting * a.northing
        }
        return sum / 2.0
    }

    fun area(points: List<Point2d>): Double = kotlin.math.abs(signedArea(points))
}
