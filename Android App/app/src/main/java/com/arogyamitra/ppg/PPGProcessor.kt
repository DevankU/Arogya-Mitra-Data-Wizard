package com.arogyamitra.ppg

import com.arogyamitra.ppg.analysis.HeartRateCalculator
import com.arogyamitra.ppg.analysis.HRVAnalyzer
import com.arogyamitra.ppg.analysis.HRVMetrics
import com.arogyamitra.ppg.filters.ButterworthFilter
import com.arogyamitra.ppg.filters.NoiseReductionPipeline
import com.arogyamitra.ppg.filters.SignalDetrend
import com.arogyamitra.ppg.filters.TemporalAverageFilter
import com.arogyamitra.ppg.models.PPGResult
import com.arogyamitra.ppg.peaks.FilterMethod
import com.arogyamitra.ppg.peaks.PeakDetector
import com.arogyamitra.ppg.peaks.PeakValidation
import com.arogyamitra.ppg.peaks.PeakValidator
import com.arogyamitra.ppg.utils.SignalBuffer
import kotlin.math.abs

/**
 * Production-ready PPG processor optimized for camera-based signals.
 *
 * Enhanced Pipeline:
 * 1. Temporal smoothing (real-time input noise reduction)
 * 2. Noise reduction (median + Savitzky-Golay filters)
 * 3. Signal cleaning (detrending + bandpass filtering)
 * 4. Peak detection (systolic peaks)
 * 5. Peak validation (quality check)
 * 6. Heart rate calculation
 * 7. HRV analysis
 */
class PPGProcessor(
    private val samplingRate: Int = 30,
    private val windowSeconds: Int = 10,
    private val method: FilterMethod = FilterMethod.BISHOP
) {

    companion object {
        // Minimum signal variance required to distinguish from still image
        private const val MIN_SIGNAL_VARIANCE = 0.5
    }

    private val windowSize = samplingRate * windowSeconds
    private val signalBuffer = SignalBuffer(windowSize)

    // Signal processing components
    private val temporalFilter = TemporalAverageFilter(3)  // Smooth input in real-time
    private val noiseReduction = NoiseReductionPipeline()   // Median + Savitzky-Golay
    private val detrend = SignalDetrend()
    private val filter = ButterworthFilter(samplingRate)
    private val peakDetector = PeakDetector(samplingRate, method)
    private val peakValidator = PeakValidator(samplingRate)
    private val hrCalculator = HeartRateCalculator(samplingRate)
    private val hrvAnalyzer = HRVAnalyzer(samplingRate)

    // State tracking
    private var frameCount = 0
    private var lastValidBPM = 0.0
    private var consecutiveFailures = 0

    /**
     * Add a new PPG sample (green channel average from face ROI).
     * Applies temporal smoothing for immediate noise reduction.
     */
    fun addSample(greenValue: Double) {
        // Apply temporal smoothing to reduce frame-to-frame noise
        val smoothedValue = temporalFilter.addAndGet(greenValue)
        signalBuffer.add(smoothedValue)
        frameCount++
    }

    /**
     * Process the PPG signal and return results.
     * Equivalent to: nk.ppg_process(ppg, sampling_rate=30)
     */
    fun process(): PPGResult {
        // Check if we have enough data
        if (signalBuffer.size < windowSize) {
            return PPGResult.insufficient(
                progress = signalBuffer.size,
                required = windowSize
            )
        }

        return try {
            val rawSignal = signalBuffer.toArray()

            // Step 0: Check signal has sufficient variance (rejects still images)
            val signalVariance = calculateVariance(rawSignal)
            if (signalVariance < MIN_SIGNAL_VARIANCE) {
                consecutiveFailures++
                return PPGResult.invalid(
                    reason = "Static signal detected - no blood flow variation",
                    confidence = 0.0
                )
            }

            // Step 1: Clean signal (equivalent to nk.ppg_clean)
            val cleanedSignal = cleanSignal(rawSignal)

            // Step 2: Find peaks (equivalent to nk.ppg_findpeaks)
            val peaks = peakDetector.findPeaks(cleanedSignal)

            // Step 3: Validate peak quality
            val validation = peakValidator.validate(peaks, cleanedSignal)

            if (!validation.isValid) {
                consecutiveFailures++
                return PPGResult.invalid(
                    reason = validation.reason,
                    confidence = validation.confidence * 100
                )
            }

            // Step 4: Calculate heart rate
            val heartRate = hrCalculator.calculate(peaks)

            if (!heartRate.isValid) {
                consecutiveFailures++
                return PPGResult.invalid(
                    reason = "Could not calculate heart rate",
                    confidence = 30.0
                )
            }

            // Step 5: Calculate HRV metrics
            val hrv = hrvAnalyzer.analyze(peaks)

            // Step 6: Calculate confidence score
            val confidence = calculateConfidence(heartRate.meanBPM, heartRate.standardDeviation, hrv, validation)

            // Validate physiological range
            if (!isPhysiologicallyValid(heartRate.meanBPM)) {
                return PPGResult.invalid(
                    reason = "BPM out of range: ${heartRate.meanBPM.toInt()}",
                    confidence = confidence
                )
            }

            // Success - update state
            consecutiveFailures = 0
            lastValidBPM = heartRate.meanBPM

            PPGResult.success(
                bpm = heartRate.meanBPM,
                instantaneousBPM = heartRate.instantaneousBPM,
                hrv = hrv,
                confidence = confidence,
                peakCount = peaks.size,
                signalQuality = validation.quality
            )

        } catch (e: Exception) {
            consecutiveFailures++
            PPGResult.error(e.message ?: "Unknown error")
        }
    }

    /**
     * Clean PPG signal using enhanced pipeline for camera-based signals.
     * 
     * Steps:
     * 1. Noise reduction (median filter + Savitzky-Golay)
     * 2. Detrending (baseline drift removal)
     * 3. Bandpass filtering (0.5-8 Hz or 0.7-4 Hz)
     */
    private fun cleanSignal(rawSignal: DoubleArray): DoubleArray {
        // Step 1: Apply noise reduction pipeline
        // - Median filter removes spike artifacts from motion
        // - Savitzky-Golay smooths while preserving peak shapes
        val denoised = noiseReduction.process(rawSignal)
        
        // Step 2: Remove baseline drift/trend
        val detrended = detrend.remove(denoised)

        // Step 3: Bandpass filter
        // This removes DC offset and high-frequency noise
        val (lowFreq, highFreq) = when (method) {
            FilterMethod.ELGENDI -> 0.5 to 8.0  // 30-480 BPM range
            FilterMethod.BISHOP -> 0.5 to 3.5   // 30-210 BPM range (optimized for camera)
        }

        return filter.bandpass(
            signal = detrended,
            lowFreq = lowFreq,
            highFreq = highFreq
        )
    }

    /**
     * Calculate overall confidence score (0-100).
     */
    private fun calculateConfidence(
        meanBPM: Double,
        stdDev: Double,
        hrv: HRVMetrics,
        validation: PeakValidation
    ): Double {
        var confidence = 100.0

        // Factor 1: Signal quality from peak validation (40% weight)
        confidence *= (validation.quality * 0.4 + 0.6)

        // Factor 2: BPM stability (30% weight)
        // Lower coefficient of variation = higher confidence
        val cv = if (meanBPM > 0) stdDev / meanBPM else 1.0
        val stabilityScore = maxOf(0.0, 1.0 - cv * 2.0)
        confidence *= (stabilityScore * 0.3 + 0.7)

        // Factor 3: HRV reasonableness (20% weight)
        // RMSSD should be between 5-150 ms for healthy adults
        val hrvScore = if (hrv.rmssd in 5.0..150.0) 1.0 else 0.5
        confidence *= (hrvScore * 0.2 + 0.8)

        // Factor 4: Consistency with previous readings (10% weight)
        if (lastValidBPM > 0) {
            val difference = abs(meanBPM - lastValidBPM)
            val consistencyScore = maxOf(0.0, 1.0 - difference / 30.0)
            confidence *= (consistencyScore * 0.1 + 0.9)
        }

        // Penalize consecutive failures
        if (consecutiveFailures > 0) {
            confidence *= (1.0 - consecutiveFailures * 0.1).coerceAtLeast(0.5)
        }

        return confidence.coerceIn(0.0, 100.0)
    }

    /**
     * Check if BPM is within physiological limits.
     */
    private fun isPhysiologicallyValid(bpm: Double): Boolean {
        return bpm in 40.0..200.0 // Relaxed range for various conditions
    }

    /**
     * Reset processor state.
     */
    fun reset() {
        signalBuffer.clear()
        temporalFilter.reset()
        frameCount = 0
        consecutiveFailures = 0
        lastValidBPM = 0.0
    }

    /**
     * Get current buffer fill percentage.
     */
    fun getBufferProgress(): Int {
        return (signalBuffer.size * 100 / windowSize).coerceIn(0, 100)
    }

    /**
     * Check if buffer is ready for processing.
     */
    fun isReady(): Boolean {
        return signalBuffer.isFull
    }

    /**
     * Calculate signal variance to detect static images.
     * A still image will have near-zero variance since the green values don't change.
     */
    private fun calculateVariance(signal: DoubleArray): Double {
        if (signal.isEmpty()) return 0.0
        
        val mean = signal.average()
        var sumSquaredDiff = 0.0
        
        for (value in signal) {
            val diff = value - mean
            sumSquaredDiff += diff * diff
        }
        
        return sumSquaredDiff / signal.size
    }
}
