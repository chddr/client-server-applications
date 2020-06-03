package net.impl.udp

import net.impl.udp.UtilsUDP.receive
import net.impl.udp.UtilsUDP.send
import net.packet.Message
import net.packet.Packet
import java.net.DatagramSocket
import java.net.InetAddress

class ClientUDP(private val clientID: Byte) {

    init {
        DatagramSocket(net.SERVER_PORT + 1).use { socket ->

            val packet = Packet(
                    clientID,
                    0,
                    Message(Message.ClientCommandTypes.CLIENT_HELLO, 1, "hello"),
                    Packet.ClientAddress(InetAddress.getByName(net.HOST), net.SERVER_PORT))
            val secondPacket = Packet(
                    clientID,
                    1,
                    Message(Message.ClientCommandTypes.CLIENT_BYE, 1, "bye"),
                    Packet.ClientAddress(InetAddress.getByName(net.HOST), net.SERVER_PORT))

            for (i in 1..10) {
                socket.send(packet)
            }
            socket.send(packet)

            socket.send(secondPacket)
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
