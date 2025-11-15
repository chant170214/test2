package com.shadowchat.data.crypto

import com.shadowchat.data.tor.OnionClient
import com.shadowchat.domain.model.ConnectionInfo
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HandshakeProtocol(private val keyManager: KeyManager) {

    private var ephemeralKeyPair = keyManager.generateEphemeralKeyPair()

    fun prepareHostingInfo(): ConnectionInfo {
        val onion = "shadow.onion" // placeholder
        val sessionId = ByteArray(16).apply { java.security.SecureRandom().nextBytes(this) }
        return ConnectionInfo(onion, ephemeralKeyPair.public.encoded, sessionId)
    }

    fun performHandshakeAsClient(
        onionClient: OnionClient,
        onion: String,
        hostPublicKey: ByteArray,
        sessionId: ByteArray
    ) {
        val clientKeyPair = keyManager.generateEphemeralKeyPair()
        val sharedSecret = keyManager.deriveSharedSecret(clientKeyPair.private, decodePublicKey(hostPublicKey))
        val payload = JSONObject().apply {
            put("type", "handshake_init")
            put("client_pubkey", Base64.getEncoder().encodeToString(clientKeyPair.public.encoded))
            put("session_id", Base64.getEncoder().encodeToString(sessionId))
        }.toString().toByteArray()
        onionClient.connect(onion, 12345)
        onionClient.send(payload)
        onionClient.receive()
        deriveSessionKeys(sharedSecret)
    }

    fun performHandshakeAsHost(
        onionClient: OnionClient,
        onion: String,
        sessionId: ByteArray
    ) {
        onionClient.connect(onion, 12345)
        val data = onionClient.receive() ?: return
        val json = JSONObject(String(data))
        val clientPub = Base64.getDecoder().decode(json.getString("client_pubkey"))
        val sharedSecret = keyManager.deriveSharedSecret(ephemeralKeyPair.private, decodePublicKey(clientPub))
        val ack = JSONObject().apply {
            put("type", "handshake_ack")
            put("ok", true)
            put("session_id", json.getString("session_id"))
            put("protocol", JSONObject().apply {
                put("cipher", "AES-256-GCM")
                put("kdf", "HKDF-SHA256")
            })
        }.toString().toByteArray()
        onionClient.send(ack)
        deriveSessionKeys(sharedSecret)
    }

    private fun deriveSessionKeys(sharedSecret: ByteArray) {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(ByteArray(mac.macLength), "HmacSHA256"))
        mac.update(sharedSecret)
        mac.update("shadowchat root".toByteArray())
        mac.doFinal()
    }

    fun clear() {
        ephemeralKeyPair = keyManager.generateEphemeralKeyPair()
    }

    private fun decodePublicKey(bytes: ByteArray): PublicKey {
        val spec = X509EncodedKeySpec(bytes)
        return KeyFactory.getInstance("X25519", "BC").generatePublic(spec)
    }
}
