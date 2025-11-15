package com.shadowchat.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isLocal: Boolean,
    val timestamp: Long,
    val meta: String
)
