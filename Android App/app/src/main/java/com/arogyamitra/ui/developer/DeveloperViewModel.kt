package com.arogyamitra.ui.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arogyamitra.data.LocalLoraRepository
import com.arogyamitra.data.LoraModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DeveloperUiState(
    val earnings: Double = 72855.50,
    val downloads: Int = 12500,
    val activeUsers: Int = 842,
    val rating: Double = 4.8,
    val salesTrend: List<Float> = listOf(0.2f, 0.4f, 0.6f, 0.5f, 0.7f, 0.8f, 0.75f),
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val developerName: String = "Devank Upadhyaya",
    val developerEmail: String = "devank@gmail.com"
)

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val repository: LocalLoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState = _uiState.asStateFlow()

    private val _models = MutableStateFlow<List<LoraModel>>(emptyList())
    
    val filteredModels: StateFlow<List<LoraModel>> = combine(_models, _uiState) { models, state ->
        models.filter { model ->
            val matchesSearch = model.title.contains(state.searchQuery, ignoreCase = true) ||
                                model.description.contains(state.searchQuery, ignoreCase = true) ||
                                model.author.contains(state.searchQuery, ignoreCase = true)
            val matchesCategory = state.selectedCategory == "All" || model.tags.contains(state.selectedCategory)
            
            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Only user-uploaded models (for My Adapters)
    val myModels: StateFlow<List<LoraModel>> = combine(_models, _uiState) { models, _ ->
        models.filter { it.isUserUploaded }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All models for marketplace display
    val allModels: StateFlow<List<LoraModel>> = _models.asStateFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            _models.value = repository.getModels()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun uploadModel(
        name: String,
        description: String,
        price: String,
        baseModel: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true)
            
            // Simulate upload delay
            delay(1500)
            
            val newModel = LoraModel(
                id = UUID.randomUUID().toString(),
                title = name,
                author = _uiState.value.developerName,
                description = description,
                price = if (price.isEmpty() || price == "0") "Free" else "₹$price",
                rating = 0.0,
                iconName = "extension",
                color = 0xFF9C27B0u, // Purple for user uploads
                systemPrompt = "You are $name, a specialized assistant based on $baseModel. $description",
                size = "25MB",
                tags = tags.ifEmpty { listOf("Custom") },
                isInstalled = false,
                isUserUploaded = true
            )
            
            repository.addModel(newModel)
            loadModels()
            
            _uiState.value = _uiState.value.copy(isUploading = false, uploadSuccess = true)
        }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            repository.deleteModel(modelId)
            loadModels()
        }
    }
    
    fun resetUploadState() {
        _uiState.value = _uiState.value.copy(uploadSuccess = false)
    }
}
