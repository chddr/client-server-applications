package net.impl.tcp

import java.net.InetAddress
import java.net.Socket

fun main() {
    Socket(InetAddress.getByName(null), SERVER_PORT).use { socket ->
        val inputStream = socket.getInputStream()
        val outputStream = socket.getOutputStream()

        outputStream.write("HELLO FROM CLIENT".toByteArray())
        outputStream.flush()

        val inputMessage = ByteArray(100)

        inputStream.read(inputMessage)
        println("Message from server: ${String(inputMessage)}")


    }
}