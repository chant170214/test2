package com.shadowchat.presentation

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.shadowchat.databinding.ActivityQrShareBinding
import com.shadowchat.domain.model.ConnectionInfo
import com.shadowchat.shadowChatApp
import org.json.JSONObject
import java.util.Base64

class QRShareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrShareBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val info = shadowChatApp.chatSessionManager.createHostingInfo()
        binding.textSessionInfo.text = info.onion
        val qrData = JSONObject().apply {
            put("version", 1)
            put("mode", "host")
            put("onion", info.onion)
            put("host_pubkey", Base64.getEncoder().encodeToString(info.hostPublicKey))
            put("session_id", Base64.getEncoder().encodeToString(info.sessionId))
        }.toString()

        binding.imageQr.setImageBitmap(generateQr(qrData))
    }

    private fun generateQr(data: String): Bitmap {
        val writer = QRCodeWriter()
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
