package org.datumpoint.app.core.export

import org.datumpoint.app.core.model.ProjectModel
import org.datumpoint.app.core.model.QualityClass
import org.datumpoint.app.core.model.SurveyPointModel
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportersTest {
    private val project = ProjectModel(id = 1, name = "Teste")
    private val points = listOf(
        point("P01", 1, -25.0, -49.0, 670000.0, 7200000.0),
        point("P02", 2, -25.0, -49.0005, 670050.0, 7200000.0),
        point("P03", 3, -25.0005, -49.0005, 670050.0, 7200050.0),
    )

    @Test
    fun csvContainsMinimumHeader() {
        val csv = Exporters.csv(project, points)
        assertTrue(csv.startsWith("project,point,sequence,latitude,longitude"))
        assertTrue(csv.contains("P01"))
    }

    @Test
    fun gisAndCadFormatsContainExpectedEntities() {
        assertTrue(Exporters.geoJson(project, points).contains("FeatureCollection"))
        assertTrue(Exporters.kml(project, points).contains("<kml"))
        assertTrue(Exporters.dxf(points).contains("LWPOLYLINE"))
        assertTrue(Exporters.txt(project, points).contains("Levantamento GNSS de reconhecimento"))
    }

    private fun point(
        name: String,
        sequence: Int,
        latitude: Double,
        longitude: Double,
        easting: Double,
        northing: Double,
    ): SurveyPointModel = SurveyPointModel(
        id = sequence.toLong(),
        projectId = 1,
        name = name,
        sequence = sequence,
        latitude = latitude,
        longitude = longitude,
        easting = easting,
        northing = northing,
        altitude = null,
        qualityClass = QualityClass.MODERATE,
        acceptedFixes = 55,
        rejectedFixes = 2,
        radialRms = 1.2,
        medianReportedAccuracy = 3.5,
        durationSeconds = 60,
    )
}
