package net.tcp

import net.tcp.UtilsTCP.receive
import net.tcp.UtilsTCP.send
import protocol.Message
import protocol.Packet
import java.net.InetAddress
import java.net.Socket

class ClientTCP(private val clientID: Byte) {
    init {
        Socket(InetAddress.getByName(net.HOST), net.SERVER_PORT).use { socket ->
            val packet = Packet(
                    clientID,
                    0,
                    Message(Message.ClientCommands.GET_TIME, 1, ""))
            val secondPacket = Packet(
                    clientID,
                    1,
                    Message(Message.ClientCommands.BYE, 1, "bye"))

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

