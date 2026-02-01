package com.arogyamitra.ppg.utils

/**
 * Circular buffer for efficient signal storage with O(1) add operations.
 * Used to store PPG samples for processing.
 */
class SignalBuffer(private val capacity: Int) {
    private val buffer = DoubleArray(capacity)
    private var head = 0
    private var count = 0

    /**
     * Add a new sample to the buffer.
     * Overwrites oldest sample when buffer is full.
     */
    fun add(value: Double) {
        buffer[head] = value
        head = (head + 1) % capacity
        if (count < capacity) count++
    }

    /**
     * Convert buffer to array in chronological order (oldest to newest).
     */
    fun toArray(): DoubleArray {
        if (count == 0) return doubleArrayOf()

        val result = DoubleArray(count)
        val start = if (count < capacity) 0 else head

        for (i in 0 until count) {
            result[i] = buffer[(start + i) % capacity]
        }

        return result
    }

    /**
     * Clear all samples from the buffer.
     */
    fun clear() {
        head = 0
        count = 0
    }

    /**
     * Current number of samples in the buffer.
     */
    val size: Int get() = count

    /**
     * Check if buffer is full.
     */
    val isFull: Boolean get() = count == capacity
}
