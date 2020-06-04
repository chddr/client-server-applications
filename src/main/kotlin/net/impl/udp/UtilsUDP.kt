package net.impl.udp

import net.protocol.Packet
import java.net.DatagramPacket
import java.net.DatagramSocket

object UtilsUDP {
    fun DatagramSocket.receive(): Packet = validate(receiveDatagram())

    fun addressFromDatagram(packet: DatagramPacket): Packet.ClientAddress = Packet.ClientAddress(packet.address, packet.port)

    /**This method here is responsible for receiving DatagramPacket from DatagramSocket. It is a blocking operation,
     * and it also forwards all the exceptions that are thrown by DatagramSocket.receive() method */
    fun DatagramSocket.receiveDatagram(): DatagramPacket {
        val bytes = ByteArray(Packet.MAX_SIZE)
        return DatagramPacket(bytes, bytes.size).also { receive(it) }
    }

    fun validate(packet: DatagramPacket): Packet {
        return Packet.fromBytes(packet.data.copyOf(packet.length)).also {
            it.clientAddress = Packet.ClientAddress(packet.address, packet.port)

            println("[[RECEIVED]]   ${packet.address}:${packet.port}")
            println("$it\n")
        }
    }

    fun DatagramSocket.send(packet: Packet) {
        println("[[SENDING FROM]]   ${this.inetAddress}:${this.localPort}")
        println("$packet\n")

        val bytes = packet.toPacket()
        val dPacket = DatagramPacket(
                bytes,
                bytes.size,
                packet.clientAddress!!.inetAddress,
                packet.clientAddress!!.port
        )

        send(dPacket)
    }

}