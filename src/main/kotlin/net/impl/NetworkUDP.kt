package net.impl

import net.Network
import net.Role
import net.SERVER_PORT
import net.packet.Packet
import net.packet.Packet.ClientAddress
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer


class NetworkUDP(private val role: Role) : Network {

    private var socket: DatagramSocket = when (role) {
        Role.Server -> DatagramSocket(SERVER_PORT)
        Role.Client -> DatagramSocket()
    }

    override fun receive(): Packet? {
        val clientAddress: ClientAddress

        val byteBuffer: ByteBuffer = ByteArray(Packet.MAX_SIZE).let { bytes ->
            DatagramPacket(bytes, bytes.size).also {
                socket.receive(it)
                clientAddress = ClientAddress(it.address, it.port)
            }
            ByteBuffer.wrap(bytes)
        }

        val packet = byteBuffer.getInt(Packet.BEFORE_LEN).let {
            byteBuffer.array().copyOf(Packet.WITHOUT_MESSAGE_LEN + it)
        }.let { Packet.fromBytes(it) }.also { it.clientAddress = clientAddress }

        println("Received:")
        println("$packet\n")

        return when (role) {
            Role.Server -> {
                Processor.process(this, packet)
                null
            }
            Role.Client -> packet
        }
    }

    override fun send(packet: Packet) {
        println("Sending:")
        println("$packet\n")

        val inetAddress = packet.clientAddress?.inetAddress ?: InetAddress.getByName(net.HOST)
        val port = packet.clientAddress?.port ?: net.SERVER_PORT

        packet.toPacket().let { packet ->
            socket.send(DatagramPacket(packet, packet.size, inetAddress, port))
        }
    }

    override fun toString(): String {
        return "NetworkUDP(role=$role, socket=$socket)\n\n"
    }


}