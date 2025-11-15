package com.shadowchat.data.crypto

import com.shadowchat.data.tor.OnionClient
import com.shadowchat.domain.model.ChatMessage
import org.json.JSONObject
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureChannel(private val keyManager: KeyManager) {

    private var txKey: ByteArray? = null
    private var rxKey: ByteArray? = null

    fun initializeForClient(hostKey: ByteArray, sessionId: ByteArray) {
        txKey = keyManager.hkdf(hostKey, "enc tx")
        rxKey = keyManager.hkdf(hostKey, "enc rx")
    }

    fun initializeForHost(sessionId: ByteArray) {
        txKey = keyManager.hkdf(sessionId, "enc rx")
        rxKey = keyManager.hkdf(sessionId, "enc tx")
    }

    fun sendMessage(onionClient: OnionClient, message: ChatMessage, sessionId: String?) {
        val payload = JSONObject().apply {
            put("type", "message")
            put("session_id", sessionId ?: "")
            put("msg_id", message.id)
            put("timestamp", message.timestamp)
            put("payload", JSONObject().apply { put("text", message.text) })
        }.toString()
        val packet = encrypt(payload.toByteArray())
        onionClient.send(packet)
    }

    fun encrypt(plain: ByteArray): ByteArray {
        val key = txKey ?: return plain
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val nonce = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.copyOf(32), "AES"), GCMParameterSpec(128, nonce))
        val encrypted = cipher.doFinal(plain)
        val json = JSONObject().apply {
            put("nonce", Base64.getEncoder().encodeToString(nonce))
            put("ciphertext", Base64.getEncoder().encodeToString(encrypted))
        }
        return json.toString().toByteArray()
    }

    fun decrypt(packet: ByteArray): String {
        val key = rxKey ?: return String(packet)
        val json = JSONObject(String(packet))
        val nonce = Base64.getDecoder().decode(json.getString("nonce"))
        val ciphertext = Base64.getDecoder().decode(json.getString("ciphertext"))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.copyOf(32), "AES"), GCMParameterSpec(128, nonce))
        return String(cipher.doFinal(ciphertext))
    }

    fun clear() {
        txKey = null
        rxKey = null
    }
}
