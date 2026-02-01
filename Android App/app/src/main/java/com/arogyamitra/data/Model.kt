package com.arogyamitra.data

data class Model(
    val id: String,
    val name: String,
    val path: String,
    var isLoaded: Boolean = false,
    var instance: Any? = null
) {
    companion object {
        const val MODEL_EXTENSION_LITERTLM = ".litertlm"
        const val MODEL_EXTENSION_TASK = ".task"
        
        fun isValidModelFile(fileName: String): Boolean {
            return fileName.endsWith(MODEL_EXTENSION_LITERTLM) || 
                   fileName.endsWith(MODEL_EXTENSION_TASK)
        }
    }
}
