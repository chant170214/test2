package com.shadowchat

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.shadowchat.domain.ChatSessionManager
import com.shadowchat.domain.SecurityPolicy
import com.shadowchat.data.crypto.HandshakeProtocol
import com.shadowchat.data.crypto.KeyManager
import com.shadowchat.data.crypto.SecureChannel
import com.shadowchat.data.storage.ChatHistoryRepository
import com.shadowchat.data.storage.KeyStoreWrapper
import com.shadowchat.data.tor.OnionClient
import com.shadowchat.data.tor.TorController

class ShadowChatApp : Application() {
    lateinit var torController: TorController
        private set
    lateinit var onionClient: OnionClient
        private set
    lateinit var keyManager: KeyManager
        private set
    lateinit var handshakeProtocol: HandshakeProtocol
        private set
    lateinit var secureChannel: SecureChannel
        private set
    lateinit var chatSessionManager: ChatSessionManager
        private set
    lateinit var chatHistoryRepository: ChatHistoryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().permitAll().build())
        torController = TorController(this)
        onionClient = OnionClient()
        keyManager = KeyManager()
        handshakeProtocol = HandshakeProtocol(keyManager)
        secureChannel = SecureChannel(keyManager)
        chatHistoryRepository = ChatHistoryRepository(KeyStoreWrapper(this))
        chatSessionManager = ChatSessionManager(
            onionClient = onionClient,
            handshakeProtocol = handshakeProtocol,
            secureChannel = secureChannel,
            chatHistoryRepository = chatHistoryRepository,
            securityPolicy = SecurityPolicy()
        )
    }
}

val Context.shadowChatApp: ShadowChatApp
    get() = applicationContext as ShadowChatApp
