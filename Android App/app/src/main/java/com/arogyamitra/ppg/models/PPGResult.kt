package com.arogyamitra.ppg.models

import com.arogyamitra.ppg.analysis.HRVMetrics

/**
 * Result of PPG processing.
 * Sealed class hierarchy for type-safe result handling.
 */
sealed class PPGResult {

    /**
     * Successful processing with valid heart rate data.
     */
    data class Success(
        val bpm: Double,
        val instantaneousBPM: List<Double>,
        val hrv: HRVMetrics,
        val confidence: Double,
        val peakCount: Int,
        val signalQuality: Double
    ) : PPGResult()

    /**
     * Insufficient data collected (buffer not full yet).
     */
    data class Insufficient(
        val progress: Int,
        val required: Int
    ) : PPGResult() {
        val progressPercent: Int get() = (progress * 100 / required).coerceIn(0, 100)
    }

    /**
     * Data collected but processing failed validation.
     */
    data class Invalid(
        val reason: String,
        val confidence: Double
    ) : PPGResult()

    /**
     * Unexpected error during processing.
     */
    data class Error(
        val message: String
    ) : PPGResult()

    companion object {
        fun success(
            bpm: Double,
            instantaneousBPM: List<Double>,
            hrv: HRVMetrics,
            confidence: Double,
            peakCount: Int,
            signalQuality: Double
        ) = Success(bpm, instantaneousBPM, hrv, confidence, peakCount, signalQuality)

        fun insufficient(progress: Int, required: Int) =
            Insufficient(progress, required)

        fun invalid(reason: String, confidence: Double) =
            Invalid(reason, confidence)

        fun error(message: String) = Error(message)
    }
}
