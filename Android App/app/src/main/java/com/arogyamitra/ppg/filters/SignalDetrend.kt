package com.arogyamitra.ppg.filters

/**
 * Remove baseline drift and trends from signal.
 * Equivalent to: scipy.signal.detrend() or nk.signal_detrend()
 */
class SignalDetrend {

    /**
     * Remove trend from signal using specified method.
     */
    fun remove(signal: DoubleArray, method: DetrendMethod = DetrendMethod.LINEAR): DoubleArray {
        return when (method) {
            DetrendMethod.LINEAR -> removeLinearTrend(signal)
            DetrendMethod.CONSTANT -> removeConstant(signal)
        }
    }

    /**
     * Fit and remove linear trend (y = mx + b) using least squares regression.
     */
    private fun removeLinearTrend(signal: DoubleArray): DoubleArray {
        val n = signal.size
        if (n < 2) return signal.copyOf()

        // Calculate linear regression coefficients
        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumX2 = 0.0

        for (i in signal.indices) {
            val x = i.toDouble()
            val y = signal[i]
            sumX += x
            sumY += y
            sumXY += x * y
            sumX2 += x * x
        }

        val denominator = n * sumX2 - sumX * sumX
        if (denominator == 0.0) return signal.copyOf()

        val slope = (n * sumXY - sumX * sumY) / denominator
        val intercept = (sumY - slope * sumX) / n

        // Remove trend
        return DoubleArray(n) { i ->
            signal[i] - (slope * i + intercept)
        }
    }

    /**
     * Remove DC offset (mean) from signal.
     */
    private fun removeConstant(signal: DoubleArray): DoubleArray {
        if (signal.isEmpty()) return signal.copyOf()
        val mean = signal.average()
        return signal.map { it - mean }.toDoubleArray()
    }
}

enum class DetrendMethod {
    LINEAR,
    CONSTANT
}
