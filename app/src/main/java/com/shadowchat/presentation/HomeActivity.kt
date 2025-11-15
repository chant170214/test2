package com.shadowchat.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shadowchat.databinding.ActivityHomeBinding
import com.shadowchat.shadowChatApp

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTorStatus()

        binding.buttonShowQr.setOnClickListener {
            startActivity(Intent(this, QRShareActivity::class.java))
        }

        binding.buttonScanQr.setOnClickListener {
            startActivity(Intent(this, QRScanActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateTorStatus()
    }

    private fun updateTorStatus() {
        val isReady = shadowChatApp.torController.isTorReady()
        val text = if (isReady) getString(com.shadowchat.R.string.tor_ready) else getString(com.shadowchat.R.string.tor_not_ready)
        binding.textTorStatus.text = getString(com.shadowchat.R.string.tor_status_format, text)
    }
}
