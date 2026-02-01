package com.arogyamitra.ppg.peaks

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * PPG peak detection optimized for camera-based signals.
 * 
 * Camera PPG signals are noisier than clinical PPG, so this implementation
 * includes pre-smoothing and more robust threshold calculation.
 */
class PeakDetector(
    private val samplingRate: Int,
    private val method: FilterMethod = FilterMethod.BISHOP
) {

    /**
     * Find systolic peaks in cleaned PPG signal.
     * Returns indices of peaks.
     */
    fun findPeaks(cleanedSignal: DoubleArray): IntArray {
        if (cleanedSignal.size < 10) return intArrayOf()
        
        // Pre-smooth signal to reduce noise (especially for camera PPG)
        val smoothedSignal = smoothSignal(cleanedSignal, 3)
        
        return when (method) {
            FilterMethod.ELGENDI -> elgendiMethod(smoothedSignal)
            FilterMethod.BISHOP -> bishopMethod(smoothedSignal)
        }
    }

    /**
     * Simple smoothing filter to reduce high-frequency noise.
     */
    private fun smoothSignal(signal: DoubleArray, windowSize: Int): DoubleArray {
        if (windowSize < 2) return signal
        
        val result = DoubleArray(signal.size)
        val halfWindow = windowSize / 2
        
        for (i in signal.indices) {
            val start = maxOf(0, i - halfWindow)
            val end = minOf(signal.size, i + halfWindow + 1)
            var sum = 0.0
            for (j in start until end) {
                sum += signal[j]
            }
            result[i] = sum / (end - start)
        }
        
        return result
    }

    /**
     * Elgendi et al. (2013) method - Default NeuroKit2 approach.
     * More robust to noise and motion artifacts.
     */
    private fun elgendiMethod(signal: DoubleArray): IntArray {
        // Parameters from Elgendi et al. 2013
        val peakWindow = maxOf(3, (0.111 * samplingRate).toInt())  // 111ms
        val beatWindow = maxOf(5, (0.667 * samplingRate).toInt())  // 667ms
        val minDistance = maxOf(4, (0.3 * samplingRate).toInt())   // 300ms (200 BPM max)

        // Step 1: Square signal to emphasize peaks
        val squared = signal.map { it * it }.toDoubleArray()

        // Step 2: Moving average (peak detection characteristic)
        val maPeak = movingAverage(squared, peakWindow)

        // Step 3: Moving average (beat detection characteristic)
        val maBeat = movingAverage(squared, beatWindow)

        // Step 4: Calculate threshold (adaptive threshold based on window)
        val alphaThreshold = 0.01  // Reduced threshold multiplier for camera PPG
        val threshold = DoubleArray(maPeak.size) { i ->
            maBeat[i] * (1.0 + alphaThreshold)
        }

        // Step 5: Find blocks where signal exceeds threshold
        val peaks = mutableListOf<Int>()
        var inBlock = false
        var blockStart = 0

        for (i in maPeak.indices) {
            if (maPeak[i] > threshold[i] && !inBlock) {
                inBlock = true
                blockStart = i
            } else if ((maPeak[i] <= threshold[i] || i == maPeak.size - 1) && inBlock) {
                inBlock = false
                // Find maximum in the block in original signal
                val blockEnd = if (i == maPeak.size - 1 && maPeak[i] > threshold[i]) i + 1 else i
                val blockPeak = findMaxInRange(signal, blockStart, blockEnd)
                if (blockPeak >= 0) {
                    peaks.add(blockPeak)
                }
            }
        }

        // Step 6: Remove peaks too close together
        return filterByMinDistance(peaks.toIntArray(), signal, minDistance)
    }

    /**
     * Bishop et al. (2018) method - Optimized for camera-based PPG.
     * Uses adaptive thresholding and robust peak detection.
     */
    private fun bishopMethod(signal: DoubleArray): IntArray {
        val peaks = mutableListOf<Int>()
        val minDistance = maxOf(9, (0.35 * samplingRate).toInt())  // 350ms (170 BPM max)

        // Calculate robust adaptive threshold using percentile-based approach
        val sorted = signal.sorted()
        val percentile75 = sorted[(sorted.size * 0.75).toInt()]
        val percentile25 = sorted[(sorted.size * 0.25).toInt()]
        val iqr = percentile75 - percentile25
        
        // Threshold: require peak to be above 60th percentile
        val percentile60 = sorted[(sorted.size * 0.6).toInt()]
        val threshold = percentile60 + 0.4 * iqr  // Stricter threshold

        // Calculate minimum prominence (difference from neighbors)
        val minProminence = 0.2 * iqr

        var lastPeakIndex = -minDistance

        for (i in 3 until signal.size - 3) {
            // Check if local maximum (using 3-neighbor comparison for robustness)
            val isLocalMax = signal[i] > signal[i - 1] && 
                           signal[i] > signal[i + 1] &&
                           signal[i] >= signal[i - 2] && 
                           signal[i] >= signal[i + 2] &&
                           signal[i] >= signal[i - 3] &&
                           signal[i] >= signal[i + 3]

            // Check prominence (peak should be significantly above neighbors)
            val leftMin = minOf(signal[i-1], signal[i-2], signal[i-3])
            val rightMin = minOf(signal[i+1], signal[i+2], signal[i+3])
            val prominence = signal[i] - maxOf(leftMin, rightMin)
            val isProminent = prominence >= minProminence
            
            // Check if above threshold
            val aboveThreshold = signal[i] > threshold

            // Check minimum distance
            val farEnough = (i - lastPeakIndex) >= minDistance

            // All conditions must be met: local max, prominent, above threshold, far enough
            if (isLocalMax && isProminent && aboveThreshold && farEnough) {
                peaks.add(i)
                lastPeakIndex = i
            }
        }

        return peaks.toIntArray()
    }

    /**
     * Moving average filter with proper boundary handling.
     */
    private fun movingAverage(signal: DoubleArray, window: Int): DoubleArray {
        val result = DoubleArray(signal.size)
        val halfWindow = window / 2

        for (i in signal.indices) {
            val start = maxOf(0, i - halfWindow)
            val end = minOf(signal.size, i + halfWindow + 1)
            var sum = 0.0
            for (j in start until end) {
                sum += signal[j]
            }
            result[i] = sum / (end - start)
        }

        return result
    }

    private fun findMaxInRange(signal: DoubleArray, start: Int, end: Int): Int {
        if (start >= end || start < 0 || end > signal.size) return -1
        
        var maxIdx = start
        var maxVal = signal[start]

        for (i in start + 1 until end) {
            if (signal[i] > maxVal) {
                maxVal = signal[i]
                maxIdx = i
            }
        }

        return maxIdx
    }

    /**
     * Filter peaks by minimum distance, keeping the peak with higher amplitude.
     */
    private fun filterByMinDistance(peaks: IntArray, signal: DoubleArray, minDistance: Int): IntArray {
        if (peaks.isEmpty()) return peaks

        val filtered = mutableListOf<Int>()
        filtered.add(peaks[0])

        for (i in 1 until peaks.size) {
            if (peaks[i] - filtered.last() >= minDistance) {
                filtered.add(peaks[i])
            } else {
                // Keep the peak with higher amplitude
                if (signal[peaks[i]] > signal[filtered.last()]) {
                    filtered[filtered.size - 1] = peaks[i]
                }
            }
        }

        return filtered.toIntArray()
    }

    private fun calculateStdDev(signal: DoubleArray): Double {
        if (signal.isEmpty()) return 0.0
        val mean = signal.average()
        val variance = signal.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }
}

enum class FilterMethod {
    ELGENDI,  // Default - best for general use
    BISHOP    // Alternative - simpler and more stable
}
