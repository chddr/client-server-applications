package net.impl.udp

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

const val SERVER_PORT = 1234

fun main() {
    val isRun = AtomicBoolean(true)

    thread(start = true) {
        try {
            Thread.sleep(10_000L)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        isRun.set(false)
    }


    DatagramSocket(net.impl.tcp.SERVER_PORT).use { serverSocket ->
        serverSocket.soTimeout = 2_000
        while (isRun.get()) {
            try {
                val inputMessage = ByteArray(1000)
                val packet = DatagramPacket(inputMessage, inputMessage.size)
                serverSocket.receive(packet)

                thread(start = true) {
                    val realMessageSize = packet.length
                    println("Message from client ${String(inputMessage, 0, realMessageSize, StandardCharsets.UTF_8)}")

                    val bytes = "Server is ok".toByteArray()

                    val response = DatagramPacket(bytes, bytes.size, packet.address, packet.port)
                    serverSocket.send(response)

                }
            } catch (e: SocketTimeoutException) {
                println("Socket timeout")
            }
        }


    }

}
