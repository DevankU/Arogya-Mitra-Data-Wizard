package com.arogyamitra.ppg.analysis

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Heart Rate Variability analysis.
 * Equivalent to: nk.hrv_time()
 * 
 * Calculates time-domain HRV metrics from RR intervals.
 */
class HRVAnalyzer(private val samplingRate: Int) {

    /**
     * Analyze HRV from detected peaks.
     */
    fun analyze(peaks: IntArray): HRVMetrics {
        if (peaks.size < 3) {
            return HRVMetrics.invalid()
        }

        // RR intervals in milliseconds
        val rrIntervals = DoubleArray(peaks.size - 1) { i ->
            ((peaks[i + 1] - peaks[i]) * 1000.0) / samplingRate
        }

        // Filter physiologically impossible intervals (<300ms or >2000ms)
        val validIntervals = rrIntervals.filter { it in 300.0..2000.0 }.toDoubleArray()
        if (validIntervals.size < 2) {
            return HRVMetrics.invalid()
        }

        // Time domain metrics
        val meanNN = validIntervals.average()
        val sdnn = calculateSDNN(validIntervals, meanNN)
        val rmssd = calculateRMSSD(validIntervals)
        val sdsd = calculateSDSD(validIntervals)
        val nn50 = calculateNN50(validIntervals)
        val pnn50 = if (validIntervals.size > 1) {
            (nn50.toDouble() / (validIntervals.size - 1)) * 100
        } else 0.0

        // Geometric measure
        val triangularIndex = calculateTriangularIndex(validIntervals)

        return HRVMetrics(
            meanNN = meanNN,
            sdnn = sdnn,
            rmssd = rmssd,
            sdsd = sdsd,
            nn50 = nn50,
            pnn50 = pnn50,
            triangularIndex = triangularIndex
        )
    }

    /**
     * SDNN: Standard deviation of NN intervals.
     * Indicator of overall HRV.
     */
    private fun calculateSDNN(intervals: DoubleArray, mean: Double): Double {
        if (intervals.isEmpty()) return 0.0
        val variance = intervals.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }

    /**
     * RMSSD: Root mean square of successive differences.
     * Indicator of parasympathetic (vagal) activity.
     */
    private fun calculateRMSSD(intervals: DoubleArray): Double {
        if (intervals.size < 2) return 0.0
        val successiveDiffs = DoubleArray(intervals.size - 1) { i ->
            intervals[i + 1] - intervals[i]
        }
        val squaredDiffs = successiveDiffs.map { it * it }
        return sqrt(squaredDiffs.average())
    }

    /**
     * SDSD: Standard deviation of successive differences.
     */
    private fun calculateSDSD(intervals: DoubleArray): Double {
        if (intervals.size < 2) return 0.0
        val successiveDiffs = DoubleArray(intervals.size - 1) { i ->
            intervals[i + 1] - intervals[i]
        }
        val mean = successiveDiffs.average()
        val variance = successiveDiffs.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }

    /**
     * NN50: Number of pairs of successive NNs that differ by more than 50 ms.
     */
    private fun calculateNN50(intervals: DoubleArray): Int {
        if (intervals.size < 2) return 0
        var count = 0
        for (i in 0 until intervals.size - 1) {
            if (abs(intervals[i + 1] - intervals[i]) > 50.0) {
                count++
            }
        }
        return count
    }

    /**
     * HRV Triangular Index: Total number of NN intervals /
     * Number of NN intervals in modal bin.
     */
    private fun calculateTriangularIndex(intervals: DoubleArray): Double {
        if (intervals.isEmpty()) return 0.0
        
        // Create histogram with 7.8125 ms bins (standard)
        val binWidth = 7.8125
        val min = intervals.minOrNull() ?: return 0.0
        val max = intervals.maxOrNull() ?: return 0.0
        
        if (max <= min) return 0.0
        
        val numBins = ((max - min) / binWidth).toInt() + 1
        val histogram = IntArray(numBins)
        
        for (interval in intervals) {
            val bin = ((interval - min) / binWidth).toInt().coerceIn(0, numBins - 1)
            histogram[bin]++
        }

        val maxBinCount = histogram.maxOrNull() ?: return 0.0
        return if (maxBinCount > 0) {
            intervals.size.toDouble() / maxBinCount
        } else 0.0
    }
}

data class HRVMetrics(
    val meanNN: Double,    // Mean NN interval (ms)
    val sdnn: Double,      // Standard deviation of NN intervals (ms)
    val rmssd: Double,     // Root mean square of successive differences (ms)
    val sdsd: Double,      // Standard deviation of successive differences (ms)
    val nn50: Int,         // Number of NN pairs differing by >50ms
    val pnn50: Double,     // Percentage of NN50
    val triangularIndex: Double  // HRV triangular index
) {
    companion object {
        fun invalid() = HRVMetrics(0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0)
    }
    
    val isValid: Boolean get() = meanNN > 0
}
