package org.datumpoint.app.core.model

enum class QualityClass {
    STABLE,
    MODERATE,
    WEAK,
}

data class ProjectModel(
    val id: Long,
    val name: String,
    val description: String = "",
    val crs: String = "SIRGAS 2000 / UTM 22S (EPSG:31982)",
    val defaultObservationSeconds: Int = 60,
    val notes: String = "",
)

data class SurveyPointModel(
    val id: Long,
    val projectId: Long,
    val name: String,
    val sequence: Int,
    val latitude: Double,
    val longitude: Double,
    val easting: Double,
    val northing: Double,
    val altitude: Double?,
    val qualityClass: QualityClass,
    val acceptedFixes: Int,
    val rejectedFixes: Int,
    val radialRms: Double,
    val medianReportedAccuracy: Double,
    val durationSeconds: Int,
    val notes: String = "",
)

data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracyMeters: Double,
    val timestampMillis: Long,
    val provider: String = "gps",
    val satellitesVisible: Int? = null,
    val satellitesUsed: Int? = null,
    val meanCn0: Double? = null,
)

data class ProcessedOccupation(
    val latitude: Double,
    val longitude: Double,
    val easting: Double,
    val northing: Double,
    val altitude: Double?,
    val durationSeconds: Int,
    val totalFixes: Int,
    val acceptedFixes: Int,
    val rejectedFixes: Int,
    val outlierFixes: Int,
    val sigmaE: Double,
    val sigmaN: Double,
    val radialRms: Double,
    val maxRadialDeviation: Double,
    val minReportedAccuracy: Double,
    val meanReportedAccuracy: Double,
    val medianReportedAccuracy: Double,
    val maxReportedAccuracy: Double,
    val satellitesVisible: Int?,
    val satellitesUsed: Int?,
    val meanCn0: Double?,
    val qualityClass: QualityClass,
)
