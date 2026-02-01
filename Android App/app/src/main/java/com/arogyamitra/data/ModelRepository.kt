package com.arogyamitra.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SavedModel(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val version: String = "v1.0",
    val isSelected: Boolean = false
)

@Singleton
class ModelRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _models = MutableStateFlow<List<SavedModel>>(emptyList())
    val models: StateFlow<List<SavedModel>> = _models.asStateFlow()
    
    private val _selectedModel = MutableStateFlow<SavedModel?>(null)
    val selectedModel: StateFlow<SavedModel?> = _selectedModel.asStateFlow()
    
    init {
        loadModels()
    }
    
    private fun loadModels() {
        val modelsJson = prefs.getString(KEY_MODELS, null)
        if (modelsJson != null) {
            try {
                val savedModels = json.decodeFromString<List<SavedModel>>(modelsJson)
                // Verify files still exist
                val validModels = savedModels.filter { File(it.path).exists() }
                _models.value = validModels
                _selectedModel.value = validModels.find { it.isSelected }
            } catch (e: Exception) {
                _models.value = emptyList()
            }
        }
    }
    
    private fun saveModels() {
        val modelsJson = json.encodeToString(_models.value)
        prefs.edit().putString(KEY_MODELS, modelsJson).apply()
    }
    
    fun addModel(model: SavedModel): SavedModel {
        val isFirst = _models.value.isEmpty()
        val newModel = model.copy(isSelected = isFirst)
        
        _models.value = _models.value + newModel
        if (isFirst) {
            _selectedModel.value = newModel
        }
        saveModels()
        return newModel
    }
    
    fun selectModel(modelId: String) {
        _models.value = _models.value.map { 
            it.copy(isSelected = it.id == modelId) 
        }
        _selectedModel.value = _models.value.find { it.id == modelId }
        saveModels()
    }
    
    fun removeModel(modelId: String) {
        val modelToRemove = _models.value.find { it.id == modelId }
        if (modelToRemove != null) {
            // Delete file
            File(modelToRemove.path).delete()
            
            _models.value = _models.value.filter { it.id != modelId }
            
            // If removed was selected, select first available
            if (modelToRemove.isSelected && _models.value.isNotEmpty()) {
                selectModel(_models.value.first().id)
            } else if (_models.value.isEmpty()) {
                _selectedModel.value = null
            }
            saveModels()
        }
    }
    
    fun hasModels(): Boolean = _models.value.isNotEmpty()
    
    fun isFirstRun(): Boolean = prefs.getBoolean(KEY_FIRST_RUN, true)
    
    fun setFirstRunComplete() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "arogya_mitra_prefs"
        private const val KEY_MODELS = "saved_models"
        private const val KEY_FIRST_RUN = "first_run"
    }
}
