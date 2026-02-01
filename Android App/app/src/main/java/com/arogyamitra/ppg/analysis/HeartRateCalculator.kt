package com.arogyamitra.ppg.analysis

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Calculate heart rate metrics from peaks with outlier rejection.
 * Uses robust statistics (median, IQR-based filtering) for camera-based PPG.
 */
class HeartRateCalculator(private val samplingRate: Int) {

    companion object {
        // Physiological limits for RR intervals
        private const val MIN_RR_INTERVAL = 0.3   // 200 BPM max
        private const val MAX_RR_INTERVAL = 1.5   // 40 BPM min
        
        // IQR multiplier for outlier detection
        private const val IQR_OUTLIER_FACTOR = 1.5
    }

    /**
     * Calculate heart rate metrics from detected peaks.
     * Uses median-based calculation and outlier rejection for robustness.
     */
    fun calculate(peaks: IntArray): HeartRateMetrics {
        if (peaks.size < 3) {  // Need at least 3 peaks for 2 intervals
            return HeartRateMetrics.invalid()
        }

        // Calculate RR intervals in seconds
        val rrIntervals = DoubleArray(peaks.size - 1) { i ->
            (peaks[i + 1] - peaks[i]) / samplingRate.toDouble()
        }

        // Step 1: Filter by physiological limits
        val physiologicalIntervals = rrIntervals.filter { it in MIN_RR_INTERVAL..MAX_RR_INTERVAL }
        if (physiologicalIntervals.size < 2) {
            return HeartRateMetrics.invalid()
        }

        // Step 2: Apply IQR-based outlier rejection
        val cleanedIntervals = removeOutliersIQR(physiologicalIntervals)
        if (cleanedIntervals.size < 2) {
            return HeartRateMetrics.invalid()
        }

        // Convert to instantaneous BPM
        val instantaneousBPM = cleanedIntervals.map { 60.0 / it }

        // Use MEDIAN for robustness against remaining outliers
        val sortedBPM = instantaneousBPM.sorted()
        val medianBPM = if (sortedBPM.size % 2 == 0) {
            (sortedBPM[sortedBPM.size / 2 - 1] + sortedBPM[sortedBPM.size / 2]) / 2
        } else {
            sortedBPM[sortedBPM.size / 2]
        }

        // Also calculate mean for comparison
        val meanBPM = instantaneousBPM.average()
        
        // Use median if mean is too different (indicates remaining outliers)
        val finalBPM = if (abs(meanBPM - medianBPM) > 10) {
            medianBPM  // Mean is unreliable, use median
        } else {
            (meanBPM + medianBPM) / 2  // Average of both for stability
        }

        val minBPM = instantaneousBPM.minOrNull() ?: 0.0
        val maxBPM = instantaneousBPM.maxOrNull() ?: 0.0

        val variance = instantaneousBPM.map { (it - finalBPM).pow(2) }.average()
        val stdDev = sqrt(variance)

        return HeartRateMetrics(
            meanBPM = finalBPM,
            minBPM = minBPM,
            maxBPM = maxBPM,
            standardDeviation = stdDev,
            instantaneousBPM = instantaneousBPM
        )
    }

    /**
     * Remove outliers using IQR method.
     * Values outside Q1 - 1.5*IQR to Q3 + 1.5*IQR are considered outliers.
     */
    private fun removeOutliersIQR(values: List<Double>): List<Double> {
        if (values.size < 4) return values
        
        val sorted = values.sorted()
        val q1Index = sorted.size / 4
        val q3Index = (sorted.size * 3) / 4
        
        val q1 = sorted[q1Index]
        val q3 = sorted[q3Index]
        val iqr = q3 - q1
        
        val lowerBound = q1 - IQR_OUTLIER_FACTOR * iqr
        val upperBound = q3 + IQR_OUTLIER_FACTOR * iqr
        
        return values.filter { it in lowerBound..upperBound }
    }
}

data class HeartRateMetrics(
    val meanBPM: Double,
    val minBPM: Double,
    val maxBPM: Double,
    val standardDeviation: Double,
    val instantaneousBPM: List<Double>
) {
    companion object {
        fun invalid() = HeartRateMetrics(0.0, 0.0, 0.0, 0.0, emptyList())
    }
    
    val isValid: Boolean get() = meanBPM > 0
}
