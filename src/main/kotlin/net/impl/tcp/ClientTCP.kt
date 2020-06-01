package net.impl.tcp

import net.impl.tcp.UtilsTCP.receive
import net.impl.tcp.UtilsTCP.send
import net.packet.Message
import net.packet.Packet
import java.net.InetAddress
import java.net.Socket

class ClientTCP {
    init {
        Socket(InetAddress.getByName(net.HOST), net.SERVER_PORT).use { socket ->
            val packet = Packet(
                    1,
                    1,
                    Message(Message.ClientCommandTypes.CLIENT_HELLO, 1, "hello"))
            val secondPacket = Packet(
                    1,
                    1,
                    Message(Message.ClientCommandTypes.CLIENT_BYE, 1, "bye"))

            socket.send(packet)
            socket.receive()

            socket.send(secondPacket)
            socket.receive()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ClientTCP()
        }
    }
}

