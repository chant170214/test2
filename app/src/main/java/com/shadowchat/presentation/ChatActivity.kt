package com.shadowchat.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shadowchat.R
import com.shadowchat.databinding.ActivityChatBinding
import com.shadowchat.domain.ChatSessionManager
import com.shadowchat.shadowChatApp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var chatSessionManager: ChatSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatSessionManager = shadowChatApp.chatSessionManager
        adapter = MessageAdapter()
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerMessages.adapter = adapter

        val peerInfoJson = intent.getStringExtra(EXTRA_PEER_INFO)
        peerInfoJson?.let {
            chatSessionManager.connectWithPeer(JSONObject(it))
        } ?: chatSessionManager.ensureHosting()

        lifecycleScope.launch {
            chatSessionManager.observeMessages().collectLatest { messages ->
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerMessages.scrollToPosition(messages.lastIndex)
                }
            }
        }

        lifecycleScope.launch {
            chatSessionManager.observeSessionStatus().collectLatest { status ->
                binding.textSessionStatus.text = status
            }
        }

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text.toString()
            if (text.isNotBlank()) {
                chatSessionManager.sendMessage(text)
                binding.editMessage.setText("")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_end_session -> {
                chatSessionManager.closeSession()
                finish()
                true
            }

            R.id.action_clear_history -> {
                chatSessionManager.clearHistory()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            chatSessionManager.closeSession()
        }
    }

    companion object {
        const val EXTRA_PEER_INFO = "extra_peer_info"
    }
}
