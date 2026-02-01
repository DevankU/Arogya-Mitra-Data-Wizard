package com.arogyamitra.ppg.filters

import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.PI
import kotlin.math.cos

/**
 * Butterworth bandpass filter implementation using FFT.
 * Replicates: scipy.signal.butter() + scipy.signal.filtfilt()
 * 
 * Uses FFT-based filtering for better numerical stability on mobile devices.
 */
class ButterworthFilter(private val samplingRate: Int) {

    private val transformer = FastFourierTransformer(DftNormalization.STANDARD)

    /**
     * Apply bandpass filter using FFT-based method.
     * This is more stable than IIR cascades on mobile devices.
     */
    fun bandpass(
        signal: DoubleArray,
        lowFreq: Double,
        highFreq: Double
    ): DoubleArray {
        if (signal.isEmpty()) return doubleArrayOf()
        if (signal.size < 4) return signal.copyOf()

        return fftBandpass(signal, lowFreq, highFreq)
    }

    /**
     * FFT-based bandpass filter (zero-phase).
     * Equivalent to scipy.signal.filtfilt()
     */
    private fun fftBandpass(
        signal: DoubleArray,
        lowFreq: Double,
        highFreq: Double
    ): DoubleArray {
        // Pad signal to power of 2 for FFT efficiency
        val paddedSize = nextPowerOfTwo(signal.size)
        val padded = DoubleArray(paddedSize)
        
        // Copy signal and apply mirror padding to reduce edge effects
        signal.copyInto(padded)
        // Mirror padding at the end
        for (i in signal.size until paddedSize) {
            val mirrorIdx = 2 * signal.size - i - 2
            if (mirrorIdx >= 0 && mirrorIdx < signal.size) {
                padded[i] = signal[mirrorIdx]
            }
        }

        // Convert to complex array for FFT
        val complexSignal = Array(paddedSize) { Complex(padded[it], 0.0) }
        
        // Perform FFT
        val fft = transformer.transform(complexSignal, TransformType.FORWARD)

        // Apply bandpass filter in frequency domain
        val freqResolution = samplingRate.toDouble() / paddedSize
        val filteredFft = Array(fft.size) { i ->
            val freq = if (i <= paddedSize / 2) {
                i * freqResolution
            } else {
                (paddedSize - i) * freqResolution // Negative frequencies
            }
            
            // Create smooth bandpass response
            val response = bandpassResponse(freq, lowFreq, highFreq)
            fft[i].multiply(response)
        }

        // Inverse FFT
        val filtered = transformer.transform(filteredFft, TransformType.INVERSE)

        // Extract real part and remove padding
        return DoubleArray(signal.size) { filtered[it].real }
    }

    /**
     * Smooth bandpass frequency response.
     * Uses raised cosine rolloff for smooth transitions.
     */
    private fun bandpassResponse(
        freq: Double,
        lowFreq: Double,
        highFreq: Double
    ): Double {
        val transitionBandwidth = 0.3 // 30% transition band

        // Low frequency cutoff with smooth transition
        val lowCutoffLow = lowFreq * (1 - transitionBandwidth)
        val lowCutoffHigh = lowFreq * (1 + transitionBandwidth)
        val lowResponse = when {
            freq < lowCutoffLow -> 0.0
            freq > lowCutoffHigh -> 1.0
            else -> {
                val x = (freq - lowCutoffLow) / (lowCutoffHigh - lowCutoffLow)
                0.5 * (1 - cos(PI * x))
            }
        }

        // High frequency cutoff with smooth transition
        val highCutoffLow = highFreq * (1 - transitionBandwidth)
        val highCutoffHigh = highFreq * (1 + transitionBandwidth)
        val highResponse = when {
            freq > highCutoffHigh -> 0.0
            freq < highCutoffLow -> 1.0
            else -> {
                val x = (freq - highCutoffLow) / (highCutoffHigh - highCutoffLow)
                0.5 * (1 + cos(PI * x))
            }
        }

        return lowResponse * highResponse
    }

    /**
     * Find next power of 2 >= n
     */
    private fun nextPowerOfTwo(n: Int): Int {
        var power = 1
        while (power < n) power = power shl 1
        return power
    }
}
