package com.arogyamitra.ppg.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.arogyamitra.ppg.PPGProcessor
import com.arogyamitra.ppg.face.FaceMeshDetector
import com.arogyamitra.ppg.face.ForeheadRegion
import com.arogyamitra.ppg.models.PPGResult

/**
 * CameraX ImageAnalysis.Analyzer for PPG signal extraction.
 * 
 * Pipeline:
 * 1. Convert camera frame to bitmap
 * 2. Detect face and extract cheek/forehead ROI
 * 3. Calculate green channel average
 * 4. Feed to PPG processor
 * 5. Process periodically and invoke callbacks
 */
class PPGAnalyzer(
    private val context: Context,
    private val samplingRate: Int = 30,
    private val windowSeconds: Int = 10
) : ImageAnalysis.Analyzer {
    
    companion object {
        private const val TAG = "PPGAnalyzer"
        private const val ANALYZE_EVERY_N_FRAMES = 15 // Analyze twice per second
        private const val FACE_LOST_THRESHOLD = 15    // Reset after 15 frames (~0.5s) without face
    }

    // Core components
    private val ppgProcessor = PPGProcessor(samplingRate, windowSeconds)
    private val faceMeshDetector = FaceMeshDetector(context)
    
    // State tracking
    private var frameCount = 0
    private var consecutiveNoFaceFrames = 0
    private var lastBitmap: Bitmap? = null
    
    // Callbacks
    var onPPGResult: ((PPGResult) -> Unit)? = null
    var onFaceDetectionStatus: ((Boolean, ForeheadRegion?) -> Unit)? = null
    var onProgress: ((Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    
    /**
     * Initialize the face detector.
     * Must be called before analyzing frames.
     */
    fun initialize() {
        try {
            faceMeshDetector.initialize()
            Log.d(TAG, "PPGAnalyzer initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize: ${e.message}")
            onError?.invoke("Failed to initialize face detector: ${e.message}")
        }
    }
    
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        try {
            // Convert ImageProxy to Bitmap
            val bitmap = imageToBitmap(image)
            if (bitmap == null) {
                image.close()
                return
            }
            
            // Detect face and get ROI
            val foreheadRegion = faceMeshDetector.detectForeheadRegion(bitmap)
            
            // Track face detection status
            handleFaceTracking(foreheadRegion != null, foreheadRegion)
            
            if (foreheadRegion != null && foreheadRegion.isValid) {
                // Extract green channel average from ROI
                val greenAvg = extractGreenChannel(bitmap, foreheadRegion)
                
                // Add sample to processor
                ppgProcessor.addSample(greenAvg)
                
                // Report progress
                val progress = ppgProcessor.getBufferProgress()
                onProgress?.invoke(progress)
                
                // Process every N frames
                frameCount++
                if (frameCount % 30 == 0) {
                     Log.d(TAG, "Analysis running. Frame: $frameCount, Face: ${foreheadRegion != null}")
                }
                
                if (frameCount % ANALYZE_EVERY_N_FRAMES == 0 && ppgProcessor.isReady()) {
                    Log.d(TAG, "Invoking PPGProcessor...")
                    val result = ppgProcessor.process()
                    Log.d(TAG, "PPG Result: $result")
                    onPPGResult?.invoke(result)
                }
            } else {
                 if (frameCount % 30 == 0) {
                     Log.d(TAG, "No face detected in analysis")
                 }
            }
            
            // Clean up
            lastBitmap?.recycle()
            lastBitmap = bitmap
            
        } catch (e: Exception) {
            Log.e(TAG, "Analysis error: ${e.message}", e)
        } finally {
            image.close()
        }
    }
    
    /**
     * Handle face tracking state and reset if face lost for too long.
     */
    private fun handleFaceTracking(faceDetected: Boolean, region: ForeheadRegion?) {
        if (faceDetected) {
            consecutiveNoFaceFrames = 0
            onFaceDetectionStatus?.invoke(true, region)
        } else {
            consecutiveNoFaceFrames++
            
            if (consecutiveNoFaceFrames >= FACE_LOST_THRESHOLD) {
                // Face lost for too long - reset processor
                ppgProcessor.reset()
                frameCount = 0
                onFaceDetectionStatus?.invoke(false, null)
                onProgress?.invoke(0)
            }
        }
    }
    
    /**
     * Extract average green channel value from ROI.
     * Green channel contains the strongest PPG signal.
     */
    private fun extractGreenChannel(bitmap: Bitmap, region: ForeheadRegion): Double {
        var greenSum = 0.0
        var pixelCount = 0
        
        // Ensure region is within bitmap bounds
        val startX = region.x.coerceIn(0, bitmap.width - 1)
        val startY = region.y.coerceIn(0, bitmap.height - 1)
        val endX = (region.x + region.width).coerceIn(0, bitmap.width)
        val endY = (region.y + region.height).coerceIn(0, bitmap.height)
        
        // Sample every other pixel for performance
        for (y in startY until endY step 2) {
            for (x in startX until endX step 2) {
                val pixel = bitmap.getPixel(x, y)
                greenSum += Color.green(pixel)
                pixelCount++
            }
        }
        
        return if (pixelCount > 0) greenSum / pixelCount else 0.0
    }
    
    /**
     * Convert ImageProxy to Bitmap.
     */
    @OptIn(ExperimentalGetImage::class)
    private fun imageToBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(
            nv21,
            android.graphics.ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, image.width, image.height),
            80,
            out
        )

        val imageBytes = out.toByteArray()
        var bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        
        // Rotate if needed (front camera is often mirrored)
        val rotation = imageProxy.imageInfo.rotationDegrees
        if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        
        return bitmap
    }
    
    /**
     * Get current processing progress (0-100).
     */
    fun getProgress(): Int = ppgProcessor.getBufferProgress()
    
    /**
     * Check if ready for processing.
     */
    fun isReady(): Boolean = ppgProcessor.isReady()
    
    /**
     * Reset all state.
     */
    fun reset() {
        ppgProcessor.reset()
        frameCount = 0
        consecutiveNoFaceFrames = 0
        lastBitmap?.recycle()
        lastBitmap = null
    }
    
    /**
     * Release resources.
     */
    fun close() {
        reset()
        faceMeshDetector.close()
    }
}
