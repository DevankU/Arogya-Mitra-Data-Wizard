package com.arogyamitra.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arogyamitra.data.LocalLoraRepository
import com.arogyamitra.data.LoraModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceUiState(
    val models: List<LoraModel> = emptyList(),
    val filteredModels: List<LoraModel> = emptyList(),
    val isLoading: Boolean = false,
    val downloadProgress: Float? = null,
    val activeModelId: String? = null,
    val loadingMessage: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: LocalLoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val allModels = repository.getModels()
            _uiState.value = _uiState.value.copy(
                models = allModels,
                filteredModels = allModels,
                activeModelId = allModels.find { it.isActive }?.id,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterModels()
    }

    private fun filterModels() {
        val query = _uiState.value.searchQuery.lowercase()
        val filtered = if (query.isEmpty()) {
            _uiState.value.models
        } else {
            _uiState.value.models.filter { model ->
                model.title.lowercase().contains(query) ||
                model.author.lowercase().contains(query) ||
                model.description.lowercase().contains(query) ||
                model.tags.any { it.lowercase().contains(query) }
            }
        }
        _uiState.value = _uiState.value.copy(filteredModels = filtered)
    }

    fun activateAndNavigate(modelId: String, onActivated: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Loading LoRA Adapter..."
            )
            delay(1500) // Simulate loading
            
            _uiState.value = _uiState.value.copy(
                activeModelId = modelId,
                isLoading = false,
                loadingMessage = null
            )
            onActivated()
        }
    }
    
    fun getSystemPromptById(modelId: String): String {
        return _uiState.value.models.find { it.id == modelId }?.systemPrompt ?: ""
    }
    
    fun getActiveSystemPrompt(): String {
        val id = _uiState.value.activeModelId
        return _uiState.value.models.find { it.id == id }?.systemPrompt ?: ""
    }
}
