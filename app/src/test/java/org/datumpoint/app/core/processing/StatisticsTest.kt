package org.datumpoint.app.core.processing

import org.datumpoint.app.core.gnss.FakeGnssRepository
import org.datumpoint.app.core.model.QualityClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsTest {
    @Test
    fun basicStatisticsAreStable() {
        val values = listOf(1.0, 2.0, 3.0, 100.0)
        assertEquals(26.5, Statistics.mean(values), 0.001)
        assertEquals(2.5, Statistics.median(values), 0.001)
        assertEquals(1.0, Statistics.mad(values), 0.001)
        assertEquals(50.035, Statistics.rms(values), 0.001)
    }

    @Test
    fun occupationProcessorFiltersAccuracyAndOutliers() {
        val result = ObservationProcessor(maxAccuracyMeters = 10.0)
            .process(FakeGnssRepository.stableOccupation(samples = 60), durationSeconds = 60)
        assertEquals(60, result.totalFixes)
        assertTrue(result.acceptedFixes >= 50)
        assertTrue(result.rejectedFixes >= 1)
        assertTrue(result.radialRms < 3.0)
        assertTrue(result.qualityClass == QualityClass.STABLE || result.qualityClass == QualityClass.MODERATE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyOccupationFailsClearly() {
        ObservationProcessor().process(emptyList(), durationSeconds = 60)
    }
}
