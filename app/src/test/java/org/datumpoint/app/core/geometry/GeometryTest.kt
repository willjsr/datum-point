package org.datumpoint.app.core.geometry

import org.junit.Assert.assertEquals
import org.junit.Test

class GeometryTest {
    @Test
    fun distanceAndAzimuthUseGridCoordinates() {
        val a = Point2d("P01", 500000.0, 7000000.0)
        val b = Point2d("P02", 500100.0, 7000000.0)
        assertEquals(100.0, Geometry.distance(a, b), 0.001)
        assertEquals(90.0, Geometry.azimuthDegrees(a, b), 0.001)
    }

    @Test
    fun areaAndPerimeterWorkForReversedPolygon() {
        val square = listOf(
            Point2d("A", 0.0, 0.0),
            Point2d("B", 10.0, 0.0),
            Point2d("C", 10.0, 10.0),
            Point2d("D", 0.0, 10.0),
        )
        assertEquals(100.0, Geometry.area(square), 0.001)
        assertEquals(100.0, Geometry.area(square.reversed()), 0.001)
        assertEquals(40.0, Geometry.perimeter(square), 0.001)
    }

    @Test
    fun smallInputsReturnZeroArea() {
        assertEquals(0.0, Geometry.area(emptyList()), 0.0)
        assertEquals(0.0, Geometry.area(listOf(Point2d("A", 0.0, 0.0))), 0.0)
    }
}
