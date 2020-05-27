package net

import net.packet.Message
import net.packet.Message.CommandTypes.ADD_PRODUCT
import net.packet.Packet
import java.net.InetAddress

object Processor {
    fun process(packet: Packet) {
        println(packet)


        Sender.sendMessage(
                Message(
                        ADD_PRODUCT,
                        0,
                        "OK"
                ),
                InetAddress.getLocalHost() //temporary
        )
    }
}