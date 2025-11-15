package com.shadowchat.domain

import com.shadowchat.data.crypto.SecureChannel
import com.shadowchat.data.tor.OnionClient

class MessageRouter(
    private val onionClient: OnionClient,
    private val secureChannel: SecureChannel
) {
    private var listener: ((ByteArray) -> Unit)? = null

    fun start() {
        // TODO start background listening to Tor sockets
    }

    fun sendEncrypted(payload: ByteArray) {
        onionClient.send(payload)
    }

    fun setOnEncryptedMessageReceived(listener: (ByteArray) -> Unit) {
        this.listener = listener
    }

    fun stop() {
        onionClient.close()
    }
}
