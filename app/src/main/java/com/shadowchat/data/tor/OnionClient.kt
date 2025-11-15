package com.shadowchat.data.tor

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class OnionClient {
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    fun connect(onionAddress: String, port: Int) {
        socket = Socket().apply {
            connect(InetSocketAddress("127.0.0.1", 9050))
        }
        outputStream = socket?.getOutputStream()
        inputStream = socket?.getInputStream()
    }

    fun send(rawBytes: ByteArray) {
        outputStream?.write(rawBytes)
        outputStream?.flush()
    }

    fun receive(): ByteArray? {
        val available = inputStream?.available() ?: 0
        if (available <= 0) return null
        val buffer = ByteArray(available)
        inputStream?.read(buffer)
        return buffer
    }

    fun close() {
        inputStream?.close()
        outputStream?.close()
        socket?.close()
    }
}
