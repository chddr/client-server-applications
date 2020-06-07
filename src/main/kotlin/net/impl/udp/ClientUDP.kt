package net.impl.udp

import net.HOST
import net.SERVER_PORT
import net.impl.udp.UtilsUDP.ClientAddress
import net.impl.udp.UtilsUDP.receive
import net.impl.udp.UtilsUDP.send
import net.protocol.Message
import net.protocol.Packet
import java.net.DatagramSocket
import java.net.InetAddress

class ClientUDP(private val clientID: Byte) {

    init {
        DatagramSocket().use { socket ->

            val address = ClientAddress(InetAddress.getByName(HOST), SERVER_PORT)

            val packet = Packet(
                    clientID,
                    0,
                    Message(Message.ClientCommands.ADD_PRODUCT, 1, "test:9.42"))
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

}

fun main() {
    ClientUDP(0)
}