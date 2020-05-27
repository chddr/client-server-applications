package net

import net.packet.Message
import net.packet.Message.CommandTypes.ADD_PRODUCT
import net.packet.Packet

object Receiver {
    fun receiveMessage() {
        //Here we create fake messages that came from the net, let's pretend it's real
        val message = Packet(
                0,
                0,
                Message(
                        ADD_PRODUCT,
                        0,
                        "fake and gay"
                )
        )

        Decryptor.decrypt(
                message.toBytes()
        )
    }
}