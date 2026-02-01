package com.arogyamitra.ppg.filters

/**
 * Savitzky-Golay filter for smoothing while preserving peaks.
 * 
 * This filter is preferred over simple moving average for PPG signals
 * because it preserves peak shapes while reducing noise.
 */
class SavitzkyGolayFilter {
    
    companion object {
        // Pre-computed coefficients for window size 5, polynomial order 2
        // These smooth the signal while preserving peak locations
        private val COEFFICIENTS_5 = doubleArrayOf(-3.0, 12.0, 17.0, 12.0, -3.0).map { it / 35.0 }.toDoubleArray()
        
        // Pre-computed coefficients for window size 7, polynomial order 2
        private val COEFFICIENTS_7 = doubleArrayOf(-2.0, 3.0, 6.0, 7.0, 6.0, 3.0, -2.0).map { it / 21.0 }.toDoubleArray()
        
        // Pre-computed coefficients for window size 9, polynomial order 2
        private val COEFFICIENTS_9 = doubleArrayOf(-21.0, 14.0, 39.0, 54.0, 59.0, 54.0, 39.0, 14.0, -21.0).map { it / 231.0 }.toDoubleArray()
    }
    
    /**
     * Apply Savitzky-Golay filter to signal.
     * @param signal Input signal
     * @param windowSize Window size (5, 7, or 9)
     * @return Smoothed signal with preserved peak shapes
     */
    fun filter(signal: DoubleArray, windowSize: Int = 7): DoubleArray {
        if (signal.size < windowSize) return signal.copyOf()
        
        val coefficients = when (windowSize) {
            5 -> COEFFICIENTS_5
            7 -> COEFFICIENTS_7
            9 -> COEFFICIENTS_9
            else -> COEFFICIENTS_7
        }
        
        val halfWindow = coefficients.size / 2
        val result = DoubleArray(signal.size)
        
        // Handle edges by copying original values
        for (i in 0 until halfWindow) {
            result[i] = signal[i]
        }
        for (i in (signal.size - halfWindow) until signal.size) {
            result[i] = signal[i]
        }
        
        // Apply filter to central region
        for (i in halfWindow until signal.size - halfWindow) {
            var sum = 0.0
            for (j in coefficients.indices) {
                sum += coefficients[j] * signal[i - halfWindow + j]
            }
            result[i] = sum
        }
        
        return result
    }
}

/**
 * Temporal averaging filter for real-time signal smoothing.
 * Maintains a history buffer and returns averaged values.
 */
class TemporalAverageFilter(private val windowSize: Int = 5) {
    private val buffer = ArrayDeque<Double>(windowSize)
    
    /**
     * Add a new sample and get the smoothed value.
     */
    fun addAndGet(value: Double): Double {
        buffer.addLast(value)
        if (buffer.size > windowSize) {
            buffer.removeFirst()
        }
        return buffer.average()
    }
    
    /**
     * Reset the filter state.
     */
    fun reset() {
        buffer.clear()
    }
}

/**
 * Median filter for removing outliers/spikes.
 */
class MedianFilter {
    
    /**
     * Apply median filter to signal.
     * Good for removing spike artifacts from motion.
     */
    fun filter(signal: DoubleArray, windowSize: Int = 3): DoubleArray {
        if (signal.size < windowSize) return signal.copyOf()
        
        val halfWindow = windowSize / 2
        val result = DoubleArray(signal.size)
        
        for (i in signal.indices) {
            val start = maxOf(0, i - halfWindow)
            val end = minOf(signal.size, i + halfWindow + 1)
            val window = signal.sliceArray(start until end).sorted()
            result[i] = window[window.size / 2]
        }
        
        return result
    }
}

/**
 * Combined noise reduction pipeline.
 */
class NoiseReductionPipeline {
    private val medianFilter = MedianFilter()
    private val sgFilter = SavitzkyGolayFilter()
    
    /**
     * Apply full noise reduction pipeline:
     * 1. Median filter (remove spikes)
     * 2. Savitzky-Golay filter (smooth while preserving peaks)
     */
    fun process(signal: DoubleArray): DoubleArray {
        // Step 1: Remove spike artifacts with median filter
        val deSpyked = medianFilter.filter(signal, 3)
        
        // Step 2: Smooth while preserving peak shapes
        val smoothed = sgFilter.filter(deSpyked, 7)
        
        return smoothed
    }
}
