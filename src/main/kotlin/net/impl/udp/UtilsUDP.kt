package net.impl.udp

import net.protocol.Packet
import java.net.DatagramPacket
import java.net.DatagramSocket

object UtilsUDP {
    fun DatagramSocket.receive(): Packet {

        val bytes = ByteArray(Packet.MAX_SIZE)
        val response = DatagramPacket(bytes, bytes.size)
        receive(response)

        val len = response.length

        return Packet.fromBytes(response.data.copyOf(len)).also {
            it.clientAddress = Packet.ClientAddress(response.address, response.port)

            println("[[RECEIVED]]   ${response.address}:${response.port}")
            println("$it\n")
        }
    }

    fun DatagramSocket.send(packet: Packet) {
        println("[[SENDING FROM]]   ${this.inetAddress}:${this.localPort}")
        println("$packet\n")

        val bytes = packet.toPacket()
        val dPacket = DatagramPacket(bytes, bytes.size, packet.clientAddress!!.inetAddress, packet.clientAddress!!.port)

        send(dPacket)
    }

}