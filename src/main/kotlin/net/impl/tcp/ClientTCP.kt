package net.impl.tcp

import net.impl.tcp.UtilsTCP.receive
import net.impl.tcp.UtilsTCP.send
import net.protocol.Message
import net.protocol.Packet
import java.net.InetAddress
import java.net.Socket

class ClientTCP(private val clientID: Byte) {
    init {
        Socket(InetAddress.getByName(net.HOST), net.SERVER_PORT).use { socket ->
            val packet = Packet(
                    clientID,
                    0,
                    Message(Message.ClientCommandTypes.CLIENT_HELLO, 1, "hello"))
            val secondPacket = Packet(
                    clientID,
                    1,
                    Message(Message.ClientCommandTypes.CLIENT_BYE, 1, "bye"))

            socket.send(packet)
            socket.receive()

            socket.send(secondPacket)
            val pack = socket.receive()
            assert(pack.msg.msg == "BYE")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ClientTCP(0)
        }
    }
}

