package com.arogyamitra.ui.chat.components

data class ChatMessage(
    val id: String,
    val content: String,
    val side: MessageSide,
    val timestamp: Long,
    val isRead: Boolean = true,
    val ppgData: PpgData? = null
)

data class PpgData(
    val bpm: Int,
    val history: List<Double>,
    val hrv: Double? = null
) : java.io.Serializable

enum class MessageSide {
    USER,
    AI
}
