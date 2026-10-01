package org.datumpoint.app.core.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val crs: String = "EPSG:31982",
    val defaultObservationSeconds: Int = 60,
    val notes: String = "",
)

@Entity(
    tableName = "survey_points",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId"), Index(value = ["projectId", "name"], unique = true)],
)
data class SurveyPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val description: String = "",
    val sequence: Int,
    val latitude: Double,
    val longitude: Double,
    val easting: Double,
    val northing: Double,
    val altitude: Double?,
    val createdAt: Long,
    val qualityClass: String,
    val notes: String = "",
)

@Entity(
    tableName = "observation_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SurveyPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["pointId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("pointId")],
)
data class ObservationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pointId: Long,
    val startedAt: Long,
    val endedAt: Long,
    val durationSeconds: Int,
    val totalFixes: Int,
    val acceptedFixes: Int,
    val rejectedFixes: Int,
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
    val medianCn0: Double?,
    val rawGnssSupported: Boolean,
    val deviceModel: String,
    val androidVersion: String,
    val processingVersion: String = "0.1.0",
)

@Entity(
    tableName = "reference_segments",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId")],
)
data class ReferenceSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val fromPointName: String,
    val toPointName: String,
    val expectedDistanceMeters: Double,
    val description: String = "",
)

@Entity(
    tableName = "reference_values",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId")],
)
data class ReferenceValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val type: String,
    val value: Double,
    val unit: String,
    val description: String = "",
)
