package org.datumpoint.app.core.processing

import org.datumpoint.app.core.geodesy.Sirgas2000Utm
import org.datumpoint.app.core.model.LocationSample
import org.datumpoint.app.core.model.ProcessedOccupation
import org.datumpoint.app.core.model.QualityClass
import kotlin.math.hypot

class ObservationProcessor(
    private val maxAccuracyMeters: Double = 10.0,
    private val minOutlierSamples: Int = 8,
    private val outlierMadThreshold: Double = 3.5,
) {
    fun process(samples: List<LocationSample>, durationSeconds: Int, zone: Int = 22): ProcessedOccupation {
        require(samples.isNotEmpty()) { "No GNSS samples were collected." }
        val plausible = samples.filter {
            it.provider.equals("gps", ignoreCase = true) &&
                it.timestampMillis > 0 &&
                it.accuracyMeters.isFinite() &&
                it.accuracyMeters > 0.0 &&
                it.accuracyMeters <= maxAccuracyMeters
        }
        require(plausible.isNotEmpty()) { "No samples passed the GNSS accuracy filter." }

        val projected = plausible.map { it to Sirgas2000Utm.fromLatLon(it.latitude, it.longitude, zone) }
        val medianE = Statistics.median(projected.map { it.second.easting })
        val medianN = Statistics.median(projected.map { it.second.northing })
        val distances = projected.map { (_, utm) -> hypot(utm.easting - medianE, utm.northing - medianN) }
        val mad = Statistics.mad(distances)
        val outlierLimit = if (plausible.size >= minOutlierSamples && mad > 0.0) {
            Statistics.median(distances) + outlierMadThreshold * 1.4826 * mad
        } else {
            Double.POSITIVE_INFINITY
        }
        val acceptedProjected = projected.zip(distances)
            .filter { (_, distance) -> distance <= outlierLimit }
            .map { (sampleWithUtm, _) -> sampleWithUtm }
        require(acceptedProjected.isNotEmpty()) { "All samples were classified as outliers." }

        val acceptedSamples = acceptedProjected.map { it.first }
        val eastings = acceptedProjected.map { it.second.easting }
        val northings = acceptedProjected.map { it.second.northing }
        val meanE = Statistics.mean(eastings)
        val meanN = Statistics.mean(northings)
        val (lat, lon) = Sirgas2000Utm.toLatLon(org.datumpoint.app.core.geodesy.UtmCoordinate(meanE, meanN, zone, org.datumpoint.app.core.geodesy.Hemisphere.SOUTH))
        val radial = acceptedProjected.map { hypot(it.second.easting - meanE, it.second.northing - meanN) }
        val accuracies = acceptedSamples.map { it.accuracyMeters }

        val medianAccuracy = Statistics.median(accuracies)
        val radialRms = Statistics.rms(radial)
        val quality = classifyQuality(
            acceptedFixes = acceptedSamples.size,
            medianAccuracy = medianAccuracy,
            radialRms = radialRms,
            durationSeconds = durationSeconds,
            outliers = plausible.size - acceptedSamples.size,
            satellitesUsed = acceptedSamples.mapNotNull { it.satellitesUsed }.maxOrNull(),
        )

        return ProcessedOccupation(
            latitude = lat,
            longitude = lon,
            easting = meanE,
            northing = meanN,
            altitude = acceptedSamples.mapNotNull { it.altitude }.takeIf { it.isNotEmpty() }?.average(),
            durationSeconds = durationSeconds,
            totalFixes = samples.size,
            acceptedFixes = acceptedSamples.size,
            rejectedFixes = samples.size - plausible.size,
            outlierFixes = plausible.size - acceptedSamples.size,
            sigmaE = Statistics.standardDeviation(eastings),
            sigmaN = Statistics.standardDeviation(northings),
            radialRms = radialRms,
            maxRadialDeviation = radial.maxOrNull() ?: 0.0,
            minReportedAccuracy = accuracies.min(),
            meanReportedAccuracy = accuracies.average(),
            medianReportedAccuracy = medianAccuracy,
            maxReportedAccuracy = accuracies.max(),
            satellitesVisible = acceptedSamples.mapNotNull { it.satellitesVisible }.maxOrNull(),
            satellitesUsed = acceptedSamples.mapNotNull { it.satellitesUsed }.maxOrNull(),
            meanCn0 = acceptedSamples.mapNotNull { it.meanCn0 }.takeIf { it.isNotEmpty() }?.average(),
            qualityClass = quality,
        )
    }

    private fun classifyQuality(
        acceptedFixes: Int,
        medianAccuracy: Double,
        radialRms: Double,
        durationSeconds: Int,
        outliers: Int,
        satellitesUsed: Int?,
    ): QualityClass {
        val satelliteScore = satellitesUsed ?: 0
        return when {
            acceptedFixes >= 45 && medianAccuracy <= 4.0 && radialRms <= 2.0 &&
                durationSeconds >= 60 && outliers <= 2 && satelliteScore >= 8 -> QualityClass.STABLE
            acceptedFixes >= 20 && medianAccuracy <= 8.0 && radialRms <= 5.0 &&
                durationSeconds >= 30 && satelliteScore >= 4 -> QualityClass.MODERATE
            else -> QualityClass.WEAK
        }
    }
}
