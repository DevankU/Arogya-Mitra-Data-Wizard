package com.arogyamitra.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

@Singleton
class LocalLoraRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fileName = "lora_adapters_v4.json"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val seedModels = LoraRepository.models

    suspend fun getModels(): List<LoraModel> = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            saveModels(seedModels)
            return@withContext seedModels
        }

        try {
            val jsonString = file.readText()
            json.decodeFromString<List<LoraModel>>(jsonString)
        } catch (e: Exception) {
            Log.e("LocalLoraRepository", "Error reading models, resetting to seed", e)
            saveModels(seedModels)
            seedModels
        }
    }

    suspend fun addModel(model: LoraModel) = withContext(Dispatchers.IO) {
        val current = getModels().toMutableList()
        current.add(0, model)
        saveModels(current)
    }

    suspend fun updateModel(updated: LoraModel) = withContext(Dispatchers.IO) {
        val current = getModels().toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            saveModels(current)
        }
    }

    suspend fun deleteModel(modelId: String) = withContext(Dispatchers.IO) {
        val current = getModels().toMutableList()
        current.removeAll { it.id == modelId }
        saveModels(current)
    }

    private fun saveModels(models: List<LoraModel>) {
        try {
            val file = File(context.filesDir, fileName)
            val jsonString = json.encodeToString(models)
            file.writeText(jsonString)
        } catch (e: Exception) {
            Log.e("LocalLoraRepository", "Error saving models", e)
        }
    }
}
