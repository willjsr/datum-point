package org.datumpoint.app.core.processing

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object Statistics {
    fun mean(values: List<Double>): Double =
        requireNotEmpty(values).average()

    fun median(values: List<Double>): Double {
        val sorted = requireNotEmpty(values).sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
    }

    fun standardDeviation(values: List<Double>): Double {
        if (values.size <= 1) return 0.0
        val mean = mean(values)
        return sqrt(values.sumOf { (it - mean).pow(2) } / (values.size - 1))
    }

    fun rms(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        return sqrt(values.sumOf { it.pow(2) } / values.size)
    }

    fun mad(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val median = median(values)
        return median(values.map { abs(it - median) })
    }

    private fun requireNotEmpty(values: List<Double>): List<Double> {
        require(values.isNotEmpty()) { "At least one value is required." }
        return values
    }
}
