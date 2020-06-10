package net.udp

import net.HOST
import net.SERVER_PORT
import net.udp.UtilsUDP.ClientAddress
import net.udp.UtilsUDP.receive
import net.udp.UtilsUDP.send
import protocol.Message
import protocol.Packet
import java.net.DatagramSocket
import java.net.InetAddress

class ClientUDP(private val clientID: Byte) {

    init {
        DatagramSocket().use { socket ->

            val address = ClientAddress(InetAddress.getByName(HOST), SERVER_PORT)

            val packet = Packet(
                    clientID,
                    0,
                    Message(Message.ClientCommands.GET_TIME, 1, ""))
            val secondPacket = Packet(
                    clientID,
                    1,
                    Message(Message.ClientCommands.BYE, 1, "bye"))

            socket.send(packet, address)
            socket.receive()

            socket.send(secondPacket, address)
            val response = socket.receive()

            assert(response.msg.msg == "BYE")

        }

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ClientUDP(0)
        }
    }

}
