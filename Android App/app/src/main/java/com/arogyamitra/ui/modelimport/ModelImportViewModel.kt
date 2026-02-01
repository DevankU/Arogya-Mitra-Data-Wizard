package com.arogyamitra.ui.modelimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arogyamitra.data.Model
import com.arogyamitra.data.ModelRepository
import com.arogyamitra.data.SavedModel
import com.arogyamitra.llm.LlmModelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class ModelImportUiState(
    val models: List<SavedModel> = emptyList(),
    val selectedModelId: String? = null,
    val isImporting: Boolean = false,
    val isLoading: Boolean = false,
    val loadingModelId: String? = null,
    val error: String? = null,
    val importSuccess: Boolean = false,
    val navigateToChat: Boolean = false
)

@HiltViewModel
class ModelImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelRepository: ModelRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ModelImportUiState())
    val uiState: StateFlow<ModelImportUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            modelRepository.models.collect { models ->
                _uiState.update { state ->
                    state.copy(
                        models = models,
                        selectedModelId = models.find { it.isSelected }?.id
                    )
                }
            }
        }
    }
    
    fun importModel(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null) }
            
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open file")
                
                val fileName = getFileName(uri) ?: "model_${System.currentTimeMillis()}"
                val modelsDir = File(context.filesDir, "models")
                modelsDir.mkdirs()
                
                val modelFile = File(modelsDir, fileName)
                
                inputStream.use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val savedModel = SavedModel(
                    id = UUID.randomUUID().toString(),
                    name = fileName.substringBeforeLast("."),
                    path = modelFile.absolutePath,
                    sizeBytes = modelFile.length(),
                    version = "v1.0"
                )
                
                val addedModel = modelRepository.addModel(savedModel)
                
                _uiState.update { 
                    it.copy(
                        isImporting = false, 
                        importSuccess = true
                    ) 
                }
                
                // Auto-load if first model
                if (modelRepository.models.value.size == 1) {
                    loadModel(addedModel.id)
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isImporting = false, 
                        error = "Failed to import: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun loadModel(modelId: String) {
        val savedModel = _uiState.value.models.find { it.id == modelId } ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingModelId = modelId, error = null) }
            
            // Create Model for LlmModelHelper
            val model = Model(
                id = savedModel.id,
                name = savedModel.name,
                path = savedModel.path
            )
            
            LlmModelHelper.initialize(
                context = context,
                model = model,
                onDone = { error ->
                    if (error.isEmpty()) {
                        modelRepository.selectModel(modelId)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                loadingModelId = null,
                                selectedModelId = modelId,
                                navigateToChat = true
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                loadingModelId = null,
                                error = error
                            ) 
                        }
                    }
                },
                onError = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loadingModelId = null,
                            error = error
                        ) 
                    }
                }
            )
        }
    }

    fun unloadModel(modelId: String) {
        val savedModel = _uiState.value.models.find { it.id == modelId } ?: return
        val model = Model(savedModel.id, savedModel.name, savedModel.path)
        LlmModelHelper.cleanUp(model) {
            // UI state update handled by recomposition checking hasInstance usually, 
            // but we can force refresh if needed.
            // But since ChatViewModel listens to selectedModel, unloading here doesn't change selection persistence necessarily,
            // but LlmModelHelper instance is gone.
            // We might want to clear selectedModel in repo if user explicitly unloads?
            // "user can also unload the model by clicking on click icon"
            // Let's NOT clear repo selection, just unload instance.
            _uiState.update { it } // trigger update? Not needed if using LlmModelHelper.hasInstance directly in UI.
        }
    }
    
    fun onNavigatedToChat() {
        _uiState.update { it.copy(navigateToChat = false) }
    }
    
    fun deleteModel(modelId: String) {
        modelRepository.removeModel(modelId)
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun dismissSuccess() {
        _uiState.update { it.copy(importSuccess = false) }
    }
    
    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    }
}
