package com.arogyamitra.llm

import android.content.Context
import android.util.Log
import com.arogyamitra.data.Model
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "LlmModelHelper"

// Default values for LLM inference
private const val DEFAULT_MAX_TOKENS = 1024
private const val DEFAULT_TOPK = 40
private const val DEFAULT_TOPP = 0.95f
private const val DEFAULT_TEMPERATURE = 0.7f

typealias ResultListener = (partialResult: String, done: Boolean) -> Unit
typealias CleanUpListener = () -> Unit

/**
 * Wrapper class holding the Engine and Conversation instance for a model.
 */
data class LlmModelInstance(
    val engine: Engine,
    var conversation: Conversation,
    val modelId: String
)

/**
 * Helper object for managing LiteRT-LM inference.
 */
object LlmModelHelper {
    // Store instances by Model ID to persist across UI/ViewModel recreations
    private val loadedInstances = ConcurrentHashMap<String, LlmModelInstance>()
    
    // Indexed by model name/id
    private val cleanUpListeners: MutableMap<String, CleanUpListener> = mutableMapOf()
    private val modelScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Initialize the LLM engine for a model on a background thread.
     */
    fun initialize(
        context: Context,
        model: Model,
        systemInstruction: String? = null,
        onDone: (error: String) -> Unit,
        onError: (error: String) -> Unit = {}
    ) {
        // Unload any other loaded models to enforce single instance
        if (!loadedInstances.containsKey(model.id)) {
            val keys = loadedInstances.keys.toList()
            for (key in keys) {
                val instance = loadedInstances[key]
                if (instance != null) {
                    Log.d(TAG, "Unloading existing model: $key")
                    // Cleaning up on background thread
                    try {
                         instance.conversation.close()
                         instance.engine.close()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error cleanup old model", e)
                    }
                    loadedInstances.remove(key)
                }
            }
        } else {
            Log.d(TAG, "Model ${model.name} already loaded.")
            onDone("")
            return
        }

        // Run on IO thread to prevent ANR
        modelScope.launch {
            Log.d(TAG, "Initializing model: ${model.name} at path: ${model.path}")
            
            try {
                // Verify file exists
                val modelFile = File(model.path)
                if (!modelFile.exists()) {
                    throw Exception("Model file not found at ${model.path}")
                }
                
                Log.d(TAG, "Model file size: ${modelFile.length()} bytes")

                // Create EngineConfig
                // IMPORTANT: Switched to CPU backend to avoid OpenCL errors on incompatible devices
                val cacheDir = context.cacheDir.absolutePath
                
                val engineConfig = EngineConfig(
                    modelPath = model.path,
                    backend = Backend.CPU, // Safest for broader compatibility
                    maxNumTokens = DEFAULT_MAX_TOKENS,
                    cacheDir = cacheDir
                )
                
                Log.d(TAG, "Creating engine with config: modelPath=${model.path}, backend=CPU, cacheDir=$cacheDir")
                
                // Create Engine and initialize
                val engine = Engine(engineConfig)
                
                // This call is blocking and expensive
                engine.initialize()
                
                Log.d(TAG, "Engine initialized, creating conversation...")
                
                // Create system instruction contents if provided
                val systemContents: Contents? = if (!systemInstruction.isNullOrBlank()) {
                    Contents.of(listOf(Content.Text(systemInstruction)))
                } else {
                    null
                }
                
                // Create conversation with sampler config
                val conversation = engine.createConversation(
                    ConversationConfig(
                        samplerConfig = SamplerConfig(
                            topK = DEFAULT_TOPK,
                            topP = DEFAULT_TOPP.toDouble(),
                            temperature = DEFAULT_TEMPERATURE.toDouble()
                        ),
                        systemInstruction = systemContents
                    )
                )
                
                // Store instance in map
                val instance = LlmModelInstance(engine = engine, conversation = conversation, modelId = model.id)
                loadedInstances[model.id] = instance
                
                // Also update the passed model object reference for convenience
                model.instance = instance
                model.isLoaded = true
                
                Log.d(TAG, "Model initialized successfully: ${model.name}")
                
                // Return to main thread for callback
                withContext(Dispatchers.Main) {
                    onDone("")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize model: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    val msg = e.message ?: "Unknown initialization error"
                    onDone(msg)
                    onError(msg)
                }
            }
        }
    }
    
    /**
     * Check if a model is loaded by ID.
     */
    fun hasInstance(modelId: String): Boolean {
        return loadedInstances.containsKey(modelId)
    }
    
    /**
     * Run inference on a loaded model.
     */
    fun runInference(
        model: Model,
        input: String,
        resultListener: ResultListener,
        cleanUpListener: CleanUpListener,
        onError: (message: String) -> Unit = {}
    ) {
        // Look up instance from map using ID
        val instance = loadedInstances[model.id]
        
        if (instance == null) {
            Log.e(TAG, "Model instance not found for ID: ${model.id}")
            onError("Model not loaded")
            return
        }
        
        // Set cleanup listener
        if (!cleanUpListeners.containsKey(model.id)) {
            cleanUpListeners[model.id] = cleanUpListener
        }
        
        val conversation = instance.conversation
        
        // Create contents list with just text input
        val contents = mutableListOf<Content>()
        if (input.trim().isNotEmpty()) {
            contents.add(Content.Text(input))
        }
        
        Log.d(TAG, "Sending message: $input")
        
        // Send message asynchronously
        conversation.sendMessageAsync(
            Contents.of(contents),
            object : MessageCallback {
                override fun onMessage(message: Message) {
                    // IMPORTANT: onMessage comes from a background thread
                    Log.d(TAG, "Received partial: ${message.toString()}")
                    resultListener(message.toString(), false)
                }
                
                override fun onDone() {
                    Log.d(TAG, "Inference complete")
                    resultListener("", true)
                }
                
                override fun onError(throwable: Throwable) {
                    if (throwable is CancellationException) {
                        Log.i(TAG, "Inference cancelled")
                        resultListener("", true)
                    } else {
                        Log.e(TAG, "Inference error: ${throwable.message}", throwable)
                        onError("Error: ${throwable.message}")
                    }
                }
            }
        )
    }
    
    /**
     * Reset the conversation for a model.
     */
    fun resetConversation(model: Model, systemInstruction: String? = null) {
        modelScope.launch {
            try {
                Log.d(TAG, "Resetting conversation for model: ${model.name}")
                
                val instance = loadedInstances[model.id] ?: return@launch
                
                // Close existing conversation
                instance.conversation.close()
                
                // Create new system instruction contents if provided
                val systemContents: Contents? = if (!systemInstruction.isNullOrBlank()) {
                    Contents.of(listOf(Content.Text(systemInstruction)))
                } else {
                    null
                }
                
                // Create new conversation
                val newConversation = instance.engine.createConversation(
                    ConversationConfig(
                        samplerConfig = SamplerConfig(
                            topK = DEFAULT_TOPK,
                            topP = DEFAULT_TOPP.toDouble(),
                            temperature = DEFAULT_TEMPERATURE.toDouble()
                        ),
                        systemInstruction = systemContents
                    )
                )
                
                instance.conversation = newConversation
                // Update the instance in the map
                loadedInstances[model.id] = instance.copy(conversation = newConversation)
                
                Log.d(TAG, "Conversation reset done")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reset conversation: ${e.message}", e)
            }
        }
    }
    
    /**
     * Clean up model resources.
     */
    fun cleanUp(model: Model, onDone: () -> Unit = {}) {
        val instance = loadedInstances[model.id] ?: return
        
        modelScope.launch {
            try {
                instance.conversation.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close conversation: ${e.message}")
            }
            
            try {
                instance.engine.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close engine: ${e.message}")
            }
            
            // Remove from map
            loadedInstances.remove(model.id)
            
            // Call cleanup listener if exists
            val onCleanUp = cleanUpListeners.remove(model.id)
            withContext(Dispatchers.Main) {
                onCleanUp?.invoke()
            }
            
            model.instance = null
            model.isLoaded = false
            
            withContext(Dispatchers.Main) {
                onDone()
            }
            Log.d(TAG, "Clean up done for model: ${model.name}")
        }
    }

    /**
     * Stop the current generation.
     */
    fun stopGeneration(model: Model) {
        val instance = loadedInstances[model.id]
        if (instance != null) {
            try {
                Log.d(TAG, "Stopping generation via cancelProcess()")
                instance.conversation.cancelProcess()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop generation: ${e.message}", e)
            }
        }
    }
}
