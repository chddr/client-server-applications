package net

import java.net.InetAddress

interface Network {
    fun receiveMessage()
    fun sendMessage(message: ByteArray, target: InetAddress)
}