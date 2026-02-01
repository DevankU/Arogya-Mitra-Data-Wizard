package com.arogyamitra.ppg.peaks

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Validate detected peaks for quality and physiological plausibility.
 * 
 * Camera-based PPG signals are inherently noisier than clinical devices,
 * so thresholds are relaxed accordingly.
 */
class PeakValidator(private val samplingRate: Int) {

    companion object {
        // Relaxed thresholds for camera-based PPG
        // Clinical PPG might use 0.3-0.5, but camera PPG needs more tolerance
        private const val MAX_INTERVAL_CV = 0.8  // Increased from 0.5
        private const val MAX_AMPLITUDE_CV = 1.2 // Increased from 0.8
        private const val MIN_PEAKS_PER_MIN = 20.0  // Lowered to capture low artifacts for handling
        private const val MAX_PEAKS_PER_MIN = 220.0 // ~220 BPM maximum
    }

    /**
     * Validate peaks and return quality assessment.
     */
    fun validate(peaks: IntArray, signal: DoubleArray): PeakValidation {
        if (peaks.size < 3) {  // Need at least 3 peaks for reliable intervals
            return PeakValidation(
                isValid = false,
                reason = "Insufficient peaks (found ${peaks.size}, need ≥3)",
                confidence = 0.0,
                quality = 0.0
            )
        }

        // Check 1: Peak count reasonableness
        val duration = signal.size / samplingRate.toDouble()
        val peaksPerMinute = (peaks.size / duration) * 60
        if (peaksPerMinute < MIN_PEAKS_PER_MIN || peaksPerMinute > MAX_PEAKS_PER_MIN) {
            return PeakValidation(
                isValid = false,
                reason = "Unrealistic peak rate: ${peaksPerMinute.toInt()} peaks/min",
                confidence = 0.3,
                quality = 0.2
            )
        }

        // Check 2: Inter-peak interval regularity
        val intervals = calculateIntervals(peaks)
        val cv = coefficientOfVariation(intervals)

        // For camera PPG, allow higher CV but reduce quality score
        if (cv > MAX_INTERVAL_CV) {
            return PeakValidation(
                isValid = false,
                reason = "Irregular peak intervals (CV=${String.format("%.2f", cv)}), hold still for reading",
                confidence = max(0.3, 0.6 - cv * 0.3),
                quality = 0.3
            )
        }

        // Check 3: Peak amplitude consistency (less strict for camera PPG)
        val amplitudes = peaks.map { 
            if (it >= 0 && it < signal.size) signal[it] else 0.0 
        }.toDoubleArray()
        val amplitudeCV = coefficientOfVariation(amplitudes)

        // Only fail on extreme amplitude inconsistency
        if (amplitudeCV > MAX_AMPLITUDE_CV) {
            return PeakValidation(
                isValid = false,
                reason = "Inconsistent peak amplitudes - improve lighting",
                confidence = 0.5,
                quality = 0.4
            )
        }

        // Calculate quality score (0-1)
        val quality = calculateQuality(cv, amplitudeCV)

        return PeakValidation(
            isValid = true,
            reason = "Valid",
            confidence = quality,
            quality = quality
        )
    }

    private fun calculateIntervals(peaks: IntArray): DoubleArray {
        if (peaks.size < 2) return doubleArrayOf()
        return DoubleArray(peaks.size - 1) { i ->
            (peaks[i + 1] - peaks[i]) / samplingRate.toDouble()
        }
    }

    private fun coefficientOfVariation(values: DoubleArray): Double {
        if (values.isEmpty()) return Double.MAX_VALUE
        val mean = values.average()
        if (abs(mean) < 1e-10) return Double.MAX_VALUE
        val stdDev = sqrt(values.map { (it - mean).pow(2) }.average())
        return stdDev / abs(mean)
    }

    private fun calculateQuality(intervalCV: Double, amplitudeCV: Double): Double {
        // Lower CV = higher quality
        // Adjusted scaling for camera-based PPG
        val intervalScore = max(0.0, 1.0 - intervalCV * 1.2)  // Gentler penalty
        val amplitudeScore = max(0.0, 1.0 - amplitudeCV * 0.5)

        // Weighted average (interval regularity is more important)
        return intervalScore * 0.7 + amplitudeScore * 0.3
    }
}

data class PeakValidation(
    val isValid: Boolean,
    val reason: String,
    val confidence: Double,
    val quality: Double
)
