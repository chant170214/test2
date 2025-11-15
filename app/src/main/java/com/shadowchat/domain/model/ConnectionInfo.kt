package com.shadowchat.domain.model

data class ConnectionInfo(
    val onion: String,
    val hostPublicKey: ByteArray,
    val sessionId: ByteArray
)
