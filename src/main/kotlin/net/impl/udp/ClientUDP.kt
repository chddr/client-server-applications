package net.impl.udp

import net.impl.udp.UtilsUDP.receive
import net.impl.udp.UtilsUDP.send
import net.protocol.Message
import net.protocol.Packet
import java.net.DatagramSocket
import java.net.InetAddress

class ClientUDP(private val clientID: Byte) {

    init {
        DatagramSocket().use { socket ->

            val packet = Packet(
                    clientID,
                    456,
                    Message(Message.ClientCommandTypes.CLIENT_HELLO, 1, "hello"),
                    Packet.ClientAddress(InetAddress.getByName(net.HOST), net.SERVER_PORT))
            val secondPacket = Packet(
                    clientID,
                    1,
                    Message(Message.ClientCommandTypes.CLIENT_BYE, 1, "bye"),
                    Packet.ClientAddress(InetAddress.getByName(net.HOST), net.SERVER_PORT))

            socket.send(packet)
            socket.receive()

            socket.send(secondPacket)
            val response = socket.receive()

            assert(response.msg.msg == "BYE")

        }

    }

}

fun main() {
    ClientUDP(0)
}