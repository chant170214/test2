package com.shadowchat.data.storage

import com.shadowchat.domain.model.ChatMessage

class ChatHistoryRepository(private val keyStoreWrapper: KeyStoreWrapper) {

    private val cache = mutableListOf<ChatMessage>()

    fun save(message: ChatMessage) {
        if (!keyStoreWrapper.isPersistenceEnabled()) {
            return
        }
        cache += message
    }

    fun all(): List<ChatMessage> = cache.toList()

    fun clear() {
        cache.clear()
    }
}
