package net.impl

import net.Network
import net.Role
import net.SERVER_PORT
import net.packet.Packet
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*


class NetworkUDP(private val role: Role) : Network {

    private var socket: DatagramSocket = when (role) {
        Role.Server -> DatagramSocket(SERVER_PORT)
        Role.Client -> DatagramSocket()
    }


    override fun receive(): Packet? {
        TODO("Not yet implemented")
    }

    override fun close() = socket.close()

    override fun send(packet: Packet) {
        var hostProperty: String = NetworkProperties.getProperty("host")
        if (hostProperty == null) hostProperty = "localhost"

        var portProperty: String = NetworkProperties.getProperty("port")
        if (portProperty == null) portProperty = "2305"

        val inetAddress = if (packet.getClientInetAddress() != null) packet.getClientInetAddress() else InetAddress.getByName(hostProperty)
        val port = if (packet.getClientPort() != null) packet.getClientPort() else portProperty.toInt()

        val packetBytes = packet.toPacket()

        val datagramPacket = DatagramPacket(packetBytes, packetBytes.size, inetAddress, port)
        socket.send(datagramPacket)

        println("Send")
        println("""
    ${Arrays.toString(packetBytes)}
    
    """.trimIndent())    }

}