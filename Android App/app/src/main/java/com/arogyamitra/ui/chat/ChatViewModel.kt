package com.arogyamitra.ui.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arogyamitra.data.Model
import com.arogyamitra.data.ModelRepository
import com.arogyamitra.data.SavedModel
import com.arogyamitra.llm.LlmModelHelper
import com.arogyamitra.ui.chat.components.ChatMessage
import com.arogyamitra.ui.chat.components.MessageSide
import com.arogyamitra.ui.chat.components.PpgData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isModelLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val currentModel: SavedModel? = null,
    val streamingMessage: String = "",
    val error: String? = null,
    val navigateToPpg: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelRepository: ModelRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var currentModelInstance: Model? = null
    
    // Store pending persona prompt to apply after model loads
    private var pendingPersonaPrompt: String? = null
    
    private val systemInstruction = """
        You are Arogya Mitra, a helpful AI health assistant. Your purpose is to:
        1. Provide general health information and wellness tips
        2. Help users understand their symptoms (but never diagnose)
        3. Encourage users to consult healthcare professionals
        4. Offer mental health support and stress management advice
        5. Analyze PPG vitals data when available
        
        Important guidelines:
        - Always remind users that you are not a replacement for professional medical advice
        - Be empathetic and supportive in your responses
        - Keep responses concise and actionable
        - If symptoms seem serious, strongly recommend seeing a doctor
        - GIVE ANSWER IN SHORT. DONT EXCEED 100 WORDS.
    """.trimIndent()
    
    init {
        viewModelScope.launch {
            modelRepository.selectedModel.collect { savedModel ->
                if (savedModel != null) {
                    // Check if we are already loading this model or if it's already loaded
                    if (_uiState.value.isLoading && _uiState.value.currentModel?.id == savedModel.id) {
                        return@collect
                    }
                    
                    if (savedModel.id != _uiState.value.currentModel?.id || !LlmModelHelper.hasInstance(savedModel.id)) {
                        loadSelectedModel(savedModel)
                    }
                }
                _uiState.update { it.copy(currentModel = savedModel) }
            }
        }
    }
    
    private suspend fun loadSelectedModel(savedModel: SavedModel) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        val model = Model(
            id = savedModel.id,
            name = savedModel.name,
            path = savedModel.path
        )
        
        currentModelInstance = model
        
        // Use pending persona prompt if available, otherwise use default
        val instructionToUse = pendingPersonaPrompt ?: systemInstruction
        val hasPendingPersona = pendingPersonaPrompt != null
        
        LlmModelHelper.initialize(
            context = context,
            model = model,
            systemInstruction = instructionToUse,
            onDone = { error ->
                if (error.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isModelLoaded = true
                        )
                    }
                    
                    // Show appropriate greeting based on whether persona was used
                    if (hasPendingPersona) {
                        addAiMessage("Specialized Adapter Loaded. I am ready to assist you with this specific domain.")
                        pendingPersonaPrompt = null // Clear after use
                    } else if (_uiState.value.messages.isEmpty()) {
                        addAiMessage("Hello! I'm Arogya Mitra, your personal AI health assistant. How can I help you today?")
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = error)
                    }
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error)
                }
            }
        )
    }
    
    /**
     * Send a message and generate a response.
     */
    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isGenerating) return
        
        val model = currentModelInstance
        
        // Check for specific commands
        if (content.trim().equals("check my vitals", ignoreCase = true)) {
            // Add user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = content,
                side = MessageSide.USER,
                timestamp = System.currentTimeMillis()
            )
            _uiState.update { 
                it.copy(
                    messages = it.messages + userMessage,
                    navigateToPpg = true 
                ) 
            }
            return
        }

        // Add user message
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            side = MessageSide.USER,
            timestamp = System.currentTimeMillis()
        )
        
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isGenerating = true,
                streamingMessage = ""
            )
        }
        
        // Check if model is loaded
        if (model == null || !LlmModelHelper.hasInstance(model.id)) {
            addAiMessage("Please import and load a model first to start chatting. Tap the model icon in the top bar to import a model.")
            _uiState.update { it.copy(isGenerating = false) }
            return
        }
        
        // Generate response
        LlmModelHelper.runInference(
            model = model,
            input = content,
            resultListener = { partialResult, done ->
                if (!done) {
                    _uiState.update {
                        it.copy(streamingMessage = it.streamingMessage + partialResult)
                    }
                } else {
                    val finalMessage = _uiState.value.streamingMessage
                    if (finalMessage.isNotBlank()) {
                        addAiMessage(finalMessage)
                    }
                    _uiState.update {
                        it.copy(streamingMessage = "", isGenerating = false)
                    }
                }
            },
            cleanUpListener = {
                _uiState.update { it.copy(isGenerating = false) }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(error = error, isGenerating = false)
                }
            }
        )
    }

    /**
     * Stop the current generation.
     */
    fun stopGeneration() {
        if (!_uiState.value.isGenerating) return
        
        val model = currentModelInstance
        if (model != null) {
            LlmModelHelper.stopGeneration(model)
            _uiState.update { it.copy(isGenerating = false) }
        }
    }
    
    /**
     * Add an AI message to the chat.
     */
    private fun addAiMessage(content: String) {
        val aiMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            side = MessageSide.AI,
            timestamp = System.currentTimeMillis()
        )
        
        _uiState.update {
            it.copy(messages = it.messages + aiMessage)
        }
    }
    
    /**
     * Clear chat history.
     */
    fun clearChat() {
        val model = currentModelInstance ?: return
        LlmModelHelper.resetConversation(model, systemInstruction)
        _uiState.update { it.copy(messages = emptyList()) }
    }
    
    /**
     * Dismiss error message.
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun onPpgNavigationConsumed() {
        _uiState.update { it.copy(navigateToPpg = false) }
    }

    fun handlePpgResult(data: PpgData) {
        val model = currentModelInstance
        
        // Create initial message with result
        val ppgMessageId = UUID.randomUUID().toString()
        val ppgMessage = ChatMessage(
            id = ppgMessageId,
            content = "Analyzing your vitals...",
            side = MessageSide.AI,
            timestamp = System.currentTimeMillis(),
            ppgData = data
        )
        
        _uiState.update { it.copy(messages = it.messages + ppgMessage) }
        
        // Send context to LLM
        if (model != null && LlmModelHelper.hasInstance(model.id)) {
            val hrStatus = when {
                data.bpm < 60 -> "Low (Bradycardia)"
                data.bpm > 100 -> "Elevated (Tachycardia)"
                else -> "Normal"
            }
            
            val contextPrompt = "System Event: User just performed a PPG scan. " +
                    "Results -> Heart Rate: ${data.bpm} BPM ($hrStatus), HRV (RMSSD): ${data.hrv ?: "N/A"} ms. " +
                    "Provide a very brief, single-sentence observation about this specific result. " +
                    "If elevated or low, mention it directly. Do not use generic 'normal' text if it is not."

            LlmModelHelper.runInference(
                model = model,
                input = contextPrompt,
                resultListener = { partialResult, done ->
                    if (done) {
                        // Update the message content with the analysis
                        _uiState.update { state ->
                            val updatedMessages = state.messages.map { msg ->
                                if (msg.id == ppgMessageId) {
                                    msg.copy(content = partialResult + state.streamingMessage) // Append just in case
                                } else msg
                            }
                            state.copy(messages = updatedMessages, streamingMessage = "")
                        }
                    } else {
                         _uiState.update { it.copy(streamingMessage = it.streamingMessage + partialResult) }
                    }
                },
                cleanUpListener = {},
                onError = {}
            )
        }
    }

    /**
     * Resets the conversation with a specific System Persona Prompt.
     * If model is loaded, applies immediately. Otherwise triggers model load with the prompt.
     */
    fun resetWithPersona(prompt: String) {
        val model = currentModelInstance
        
        // Use default instruction if prompt is empty
        val instructionToUse = if (prompt.isBlank()) systemInstruction else prompt
        
        // Store the prompt for when model loads (if not loaded yet)
        pendingPersonaPrompt = if (prompt.isBlank()) null else prompt
        
        // If model is loaded, apply immediately
        if (model != null && LlmModelHelper.hasInstance(model.id)) {
            LlmModelHelper.resetConversation(model, instructionToUse)
            pendingPersonaPrompt = null // Clear since we applied it
            
            // Clear messages and show confirmation
            _uiState.update { 
                it.copy(
                    messages = listOf(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            content = if (prompt.isBlank()) 
                                "Adapter Deactivated. Back to general Arogya Mitra assistant." 
                            else 
                                "Specialized Adapter Loaded. I am ready to assist you with this specific domain.",
                            side = MessageSide.AI,
                            timestamp = System.currentTimeMillis()
                        )
                    ) 
                ) 
            }
        } else {
            // Model not loaded yet - check if we have a saved model to load
            val savedModel = modelRepository.selectedModel.value
            if (savedModel != null) {
                // Trigger model loading - the pending prompt will be applied
                viewModelScope.launch {
                    _uiState.update { 
                        it.copy(
                            messages = listOf(
                                ChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    content = "Loading specialized adapter...",
                                    side = MessageSide.AI,
                                    timestamp = System.currentTimeMillis()
                                )
                            ) 
                        ) 
                    }
                    loadSelectedModel(savedModel)
                }
            } else {
                // No model available at all
                _uiState.update { 
                    it.copy(
                        messages = listOf(
                            ChatMessage(
                                id = UUID.randomUUID().toString(),
                                content = "Please import a model first to use this adapter. Go to Settings → Import Model.",
                                side = MessageSide.AI,
                                timestamp = System.currentTimeMillis()
                            )
                        ) 
                    ) 
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentModelInstance?.let { model ->
            LlmModelHelper.cleanUp(model)
        }
    }
}
