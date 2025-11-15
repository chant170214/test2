package com.shadowchat.data.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.util.Base64
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class KeyManager {

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun generateEphemeralKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("X25519", BouncyCastleProvider.PROVIDER_NAME)
        kpg.initialize(255, SecureRandom())
        return kpg.generateKeyPair()
    }

    fun deriveSharedSecret(privateKey: java.security.PrivateKey, publicKey: java.security.PublicKey): ByteArray {
        val agreement = KeyAgreement.getInstance("X25519", BouncyCastleProvider.PROVIDER_NAME)
        agreement.init(privateKey)
        agreement.doPhase(publicKey, true)
        return agreement.generateSecret()
    }

    fun hkdf(input: ByteArray, info: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(ByteArray(mac.macLength), "HmacSHA256")
        mac.init(key)
        mac.update(input)
        mac.update(info.toByteArray())
        return mac.doFinal()
    }

    fun encodePublicKey(publicKey: java.security.PublicKey): String =
        Base64.getEncoder().encodeToString(publicKey.encoded)
}
