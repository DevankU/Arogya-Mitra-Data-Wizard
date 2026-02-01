package com.arogyamitra.ppg.face

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

/**
 * MediaPipe Face Landmarker wrapper for forehead ROI extraction.
 * 
 * Uses face mesh landmarks to identify the forehead region for PPG signal extraction.
 */
class FaceMeshDetector(private val context: Context) {

    private var faceLandmarker: FaceLandmarker? = null
    private var lastResult: FaceLandmarkerResult? = null

    // Cheek landmark indices (MediaPipe Face Mesh 468 landmarks)
    // Cheeks provide better PPG signal than forehead due to thinner skin
    // and higher blood flow. Using the central cheek areas.
    
    // Right cheek landmarks - between nose and ear
    private val rightCheekLandmarks = intArrayOf(
        50, 205, 206, 207,   // Upper right cheek
        123, 117, 118, 119,  // Mid right cheek  
        100, 126, 209, 49    // Lower right cheek
    )
    
    // Left cheek landmarks - between nose and ear
    private val leftCheekLandmarks = intArrayOf(
        280, 425, 426, 427,  // Upper left cheek
        352, 346, 347, 348,  // Mid left cheek
        329, 355, 429, 279   // Lower left cheek
    )
    
    // Combined for ROI extraction
    private val faceROILandmarkIndices = rightCheekLandmarks + leftCheekLandmarks

    // Callback for asynchronous detection results
    var onFaceDetected: ((ForeheadRegion?) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    /**
     * Initialize the Face Landmarker with the model from assets.
     */
    fun initialize() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("face_landmarker.task")
                .setDelegate(Delegate.CPU) // Use CPU for broader compatibility
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(0.5f)
                .setMinFacePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setOutputFaceBlendshapes(false)
                .setOutputFacialTransformationMatrixes(false)
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            onError?.invoke("Failed to initialize face detector: ${e.message}")
        }
    }

    /**
     * Detect face and extract forehead region from bitmap.
     * Synchronous version for IMAGE mode.
     */
    fun detectForeheadRegion(bitmap: Bitmap): ForeheadRegion? {
        val landmarker = faceLandmarker ?: return null

        try {
            // Convert bitmap to MPImage
            val mpImage = BitmapImageBuilder(bitmap).build()
            
            // Detect face landmarks
            val result = landmarker.detect(mpImage)
            lastResult = result

            // Check if any face was detected
            if (result.faceLandmarks().isEmpty()) {
                return null
            }

            // Get landmarks for the first face
            val landmarks = result.faceLandmarks()[0]
            
            return extractForeheadRegion(landmarks, bitmap.width, bitmap.height)
        } catch (e: Exception) {
            onError?.invoke("Face detection error: ${e.message}")
            return null
        }
    }

    /**
     * Extract face ROI (cheeks) from face landmarks.
     * Cheeks provide better PPG signal due to thinner skin and higher blood flow.
     */
    private fun extractForeheadRegion(
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        imageWidth: Int,
        imageHeight: Int
    ): ForeheadRegion? {
        if (landmarks.size < 468) {
            return null // Not enough landmarks
        }

        // Get cheek landmark coordinates
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        for (idx in faceROILandmarkIndices) {
            if (idx < landmarks.size) {
                val landmark = landmarks[idx]
                val x = landmark.x() * imageWidth
                val y = landmark.y() * imageHeight

                minX = minOf(minX, x)
                maxX = maxOf(maxX, x)
                minY = minOf(minY, y)
                maxY = maxOf(maxY, y)
            }
        }

        // Validate region
        if (minX >= maxX || minY >= maxY) {
            return null
        }

        // Create bounding rectangle with some padding
        val padding = 0.1f // 10% padding
        val width = maxX - minX
        val height = maxY - minY
        
        val paddedMinX = (minX - width * padding).coerceAtLeast(0f)
        val paddedMinY = (minY - height * padding).coerceAtLeast(0f)
        val paddedMaxX = (maxX + width * padding).coerceAtMost(imageWidth.toFloat())
        val paddedMaxY = (maxY + height * padding).coerceAtMost(imageHeight.toFloat())

        return ForeheadRegion(
            x = paddedMinX.toInt(),
            y = paddedMinY.toInt(),
            width = (paddedMaxX - paddedMinX).toInt(),
            height = (paddedMaxY - paddedMinY).toInt(),
            bounds = RectF(paddedMinX, paddedMinY, paddedMaxX, paddedMaxY),
            confidence = 1.0f // MediaPipe doesn't provide per-region confidence
        )
    }

    /**
     * Get the last detection result.
     */
    fun getLastResult(): FaceLandmarkerResult? = lastResult

    /**
     * Check if detector is initialized.
     */
    fun isInitialized(): Boolean = faceLandmarker != null

    /**
     * Release resources.
     */
    fun close() {
        faceLandmarker?.close()
        faceLandmarker = null
    }
}

/**
 * Represents the forehead region extracted from face landmarks.
 */
data class ForeheadRegion(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val bounds: RectF,
    val confidence: Float
) {
    val isValid: Boolean get() = width > 10 && height > 10
}
