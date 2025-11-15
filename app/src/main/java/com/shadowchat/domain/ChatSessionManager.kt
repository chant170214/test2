package com.shadowchat.domain

import com.shadowchat.data.crypto.HandshakeProtocol
import com.shadowchat.data.crypto.SecureChannel
import com.shadowchat.data.storage.ChatHistoryRepository
import com.shadowchat.data.tor.OnionClient
import com.shadowchat.domain.model.ChatMessage
import com.shadowchat.domain.model.ConnectionInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.util.Base64
import java.util.UUID

class ChatSessionManager(
    private val onionClient: OnionClient,
    private val handshakeProtocol: HandshakeProtocol,
    private val secureChannel: SecureChannel,
    private val chatHistoryRepository: ChatHistoryRepository,
    private val securityPolicy: SecurityPolicy
) {

    private val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val status = MutableStateFlow("未接続")
    private var currentSessionId: String? = null
    private var connectionInfo: ConnectionInfo? = null

    fun observeMessages(): StateFlow<List<ChatMessage>> = messages.asStateFlow()
    fun observeSessionStatus(): StateFlow<String> = status.asStateFlow()

    fun createHostingInfo(): ConnectionInfo {
        securityPolicy.ensureSecureWindow()
        val info = handshakeProtocol.prepareHostingInfo()
        connectionInfo = info
        status.value = "待機中"
        return info
    }

    fun ensureHosting() {
        if (connectionInfo == null) {
            createHostingInfo()
        }
    }

    fun connectWithPeer(peerInfoJson: JSONObject) {
        val onion = peerInfoJson.getString("onion")
        val hostKey = Base64.getDecoder().decode(peerInfoJson.getString("host_pubkey"))
        val sessionId = Base64.getDecoder().decode(peerInfoJson.getString("session_id"))
        status.value = "接続試行中"
        currentSessionId = String(sessionId)
        handshakeProtocol.performHandshakeAsClient(onionClient, onion, hostKey, sessionId)
        secureChannel.initializeForClient(hostKey, sessionId)
        status.value = "接続済み"
    }

    fun startListening(onion: String, sessionId: ByteArray) {
        status.value = "接続待ち"
        currentSessionId = String(sessionId)
        handshakeProtocol.performHandshakeAsHost(onionClient, onion, sessionId)
        secureChannel.initializeForHost(sessionId)
        status.value = "接続済み"
    }

    fun sendMessage(text: String) {
        val id = UUID.randomUUID().toString()
        val message = ChatMessage(
            id = id,
            text = text,
            isLocal = true,
            timestamp = System.currentTimeMillis(),
            meta = "送信"
        )
        messages.update { it + message }
        chatHistoryRepository.save(message)
        secureChannel.sendMessage(onionClient, message, currentSessionId)
    }

    fun receiveMessage(message: ChatMessage) {
        messages.update { it + message }
        chatHistoryRepository.save(message)
    }

    fun closeSession() {
        onionClient.close()
        secureChannel.clear()
        handshakeProtocol.clear()
        status.value = "切断"
        currentSessionId = null
        securityPolicy.clear()
    }

    fun clearHistory() {
        chatHistoryRepository.clear()
        messages.value = emptyList()
    }
}
