package net.impl.udp

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.StandardCharsets

fun main() {
    for (i in 1..1000) {
        DatagramSocket(net.impl.tcp.SERVER_PORT +1).use(block = { serverSocket ->
            serverSocket.soTimeout = 10_000
            println(serverSocket.localPort)

            val bytes = "Hello from client".toByteArray()
            val packet = DatagramPacket(bytes, bytes.size, InetAddress.getByName(null), net.impl.tcp.SERVER_PORT +1)
            serverSocket.send(packet)


            val inputMessage = ByteArray(100)
            val response = DatagramPacket(inputMessage, inputMessage.size)
            serverSocket.receive(response)

            val realMessageSize = packet.length
            println("Message from server: \"${String(inputMessage, 0, realMessageSize, StandardCharsets.UTF_8)}\"")


        })
    }
}