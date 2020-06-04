package net.impl.udp

import net.protocol.Packet
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object UtilsUDP {

    data class ClientAddress(val inetAddress: InetAddress, val port: Int) {

        companion object {
            fun from(packet: DatagramPacket): ClientAddress {
                return ClientAddress(packet.address, packet.port)
            }
        }
    }

    fun DatagramSocket.receive(): Packet {
        return validate(receiveDatagram())
    }

    /**This method here is responsible for receiving DatagramPacket from DatagramSocket. It is a blocking operation,
     * and it also forwards all the exceptions that are thrown by DatagramSocket.receive() method */
    fun DatagramSocket.receiveDatagram(): DatagramPacket {
        val bytes = ByteArray(Packet.MAX_SIZE)
        return DatagramPacket(bytes, bytes.size).also { receive(it) }
    }

    fun validate(packet: DatagramPacket): Packet {
        return Packet.fromBytes(
                packet.data.copyOf(packet.length)).also {
            println("[[RECEIVED]]   ${packet.address}:${packet.port}")
            println("$it\n")
        }
    }

    fun DatagramSocket.send(packet: Packet, address: ClientAddress) {
        println("[[SENDING FROM]]   ${this.localAddress}:${this.localPort}")
        println("$packet\n")

        val bytes = packet.toPacket()
        val dPacket = DatagramPacket(
                bytes,
                bytes.size,
                address.inetAddress,
                address.port
        )

        send(dPacket)
    }

}