package org.datumpoint.app.core.gnss

import org.datumpoint.app.core.model.LocationSample
import kotlin.random.Random

object FakeGnssRepository {
    fun stableOccupation(
        latitude: Double = -25.4284,
        longitude: Double = -49.2733,
        samples: Int = 60,
        seed: Int = 42,
    ): List<LocationSample> {
        val random = Random(seed)
        val now = System.currentTimeMillis()
        return (0 until samples).map { index ->
            val outlier = index == samples / 2
            val noise = if (outlier) 0.000045 else random.nextDouble(-0.000008, 0.000008)
            LocationSample(
                latitude = latitude + noise,
                longitude = longitude + random.nextDouble(-0.000008, 0.000008),
                altitude = 935.0 + random.nextDouble(-1.5, 1.5),
                accuracyMeters = if (outlier) 18.0 else random.nextDouble(2.2, 5.4),
                timestampMillis = now + index * 1000L,
                provider = "gps",
                satellitesVisible = 28,
                satellitesUsed = 15,
                meanCn0 = random.nextDouble(31.0, 39.0),
            )
        }
    }
}
