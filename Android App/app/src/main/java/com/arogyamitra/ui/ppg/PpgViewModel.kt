package com.arogyamitra.ui.ppg

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.arogyamitra.ppg.analysis.HRVMetrics
import com.arogyamitra.ppg.camera.PPGAnalyzer
import com.arogyamitra.ppg.face.ForeheadRegion
import com.arogyamitra.ppg.models.PPGResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject

/**
 * ViewModel for PPG vitals screen.
 * Manages camera lifecycle, PPG processing, and UI state.
 */
@HiltViewModel
class PpgViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PpgViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow(PpgUiState())
    val uiState: StateFlow<PpgUiState> = _uiState.asStateFlow()

    // Camera components
    private var cameraProvider: ProcessCameraProvider? = null
    private var ppgAnalyzer: PPGAnalyzer? = null
    private var cameraExecutor: ExecutorService? = null
    private var previewView: PreviewView? = null
    
    // User constraints state
    private var lastDisplayedBpm: Int? = null

    /**
     * Check if camera permission is granted.
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Initialize camera and PPG analyzer.
     */
    fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        this.previewView = previewView
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize PPG analyzer
        ppgAnalyzer = PPGAnalyzer(getApplication()).apply {
            initialize()
            
            onPPGResult = { result ->
                handlePPGResult(result)
            }
            
            onFaceDetectionStatus = { detected, region ->
                _uiState.value = _uiState.value.copy(
                    faceDetected = detected,
                    faceRegion = region,
                    statusMessage = if (detected) "Face detected - hold still" else "Position your face in frame"
                )
            }
            
            onProgress = { progress ->
                _uiState.value = _uiState.value.copy(progress = progress / 100f)
            }
            
            onError = { error ->
                Log.e(TAG, "PPG Error: $error")
                _uiState.value = _uiState.value.copy(
                    statusMessage = error,
                    error = error
                )
            }
        }

        // Start camera
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Camera initialization failed: ${e.message}"
                )
            }
        }, ContextCompat.getMainExecutor(getApplication()))
    }

    /**
     * Bind camera use cases.
     */
    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner) {
        val provider = cameraProvider ?: return
        val preview = previewView ?: return
        val analyzer = ppgAnalyzer ?: return
        val executor = cameraExecutor ?: return

        // Unbind previous use cases
        provider.unbindAll()

        // Camera selector - use front camera for face detection
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        // Preview use case
        val previewUseCase = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = preview.surfaceProvider
            }

        // Image analysis use case
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
            .also {
                it.setAnalyzer(executor, analyzer)
            }

        try {
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewUseCase,
                imageAnalysis
            )
            
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                statusMessage = "Position your face in frame"
            )
            
            Log.d(TAG, "Camera bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Camera binding failed: ${e.message}")
            _uiState.value = _uiState.value.copy(
                error = "Camera binding failed: ${e.message}"
            )
        }
    }

    /**
     * Handle PPG processing results.
     */
    private fun handlePPGResult(result: PPGResult) {
        viewModelScope.launch {
            when (result) {
                is PPGResult.Success -> {
                    // --- User Logic Implementation ---
                    var finalBpm = result.bpm.toInt()
                    
                    // 1. If < 70, give random(70-75)
                    if (finalBpm < 70) {
                        finalBpm = Random.nextInt(70, 76) // 70 to 75
                    }
                    
                    // 2. Dont fluctuate > 15bpm
                    lastDisplayedBpm?.let { last ->
                        val diff = finalBpm - last
                        // Clamp diff to [-15, 15]
                        val clampedDiff = diff.coerceIn(-15, 15)
                        finalBpm = last + clampedDiff
                    }
                    
                    lastDisplayedBpm = finalBpm
                    // ---------------------------------

                    _uiState.value = _uiState.value.copy(
                        heartRate = finalBpm,
                        hrv = result.hrv,
                        confidence = result.confidence.toInt(),
                        signalQuality = result.signalQuality,
                        statusMessage = "Measurement ready",
                        hasResult = true,
                        instantaneousBPM = result.instantaneousBPM
                    )
                }
                
                is PPGResult.Insufficient -> {
                    _uiState.value = _uiState.value.copy(
                        progress = result.progressPercent / 100f,
                        statusMessage = "Collecting data... ${result.progressPercent}%"
                    )
                }
                
                is PPGResult.Invalid -> {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = result.reason,
                        confidence = result.confidence.toInt()
                    )
                }
                
                is PPGResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        statusMessage = result.message,
                        error = result.message
                    )
                }
            }
        }
    }

    /**
     * Start/restart scanning.
     */
    fun startScanning() {
        ppgAnalyzer?.reset()
        lastDisplayedBpm = null
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            progress = 0f,
            heartRate = 0,
            hrv = null,
            hasResult = false,
            statusMessage = "Position your face in frame"
        )
    }

    /**
     * Stop scanning.
     */
    fun stopScanning() {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            statusMessage = "Scanning paused"
        )
    }

    /**
     * Toggle scanning state.
     */
    fun toggleScanning() {
        if (_uiState.value.isScanning) {
            stopScanning()
        } else {
            startScanning()
        }
    }

    /**
     * Release resources.
     */
    override fun onCleared() {
        super.onCleared()
        ppgAnalyzer?.close()
        cameraExecutor?.shutdown()
        cameraProvider?.unbindAll()
    }
}

/**
 * UI State for PPG screen.
 */
data class PpgUiState(
    val isScanning: Boolean = false,
    val faceDetected: Boolean = false,
    val faceRegion: ForeheadRegion? = null,
    val progress: Float = 0f,
    val heartRate: Int = 0,
    val hrv: HRVMetrics? = null,
    val confidence: Int = 0,
    val signalQuality: Double = 0.0,
    val statusMessage: String = "Ready to scan",
    val hasResult: Boolean = false,
    val error: String? = null,
    val instantaneousBPM: List<Double> = emptyList()
)
