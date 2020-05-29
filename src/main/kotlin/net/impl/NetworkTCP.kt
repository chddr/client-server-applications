package net.impl

import net.Network
import net.packet.EncryptorDecryptor
import net.packet.Message
import net.packet.Message.CommandTypes
import net.packet.Packet
import java.net.InetAddress

object NetworkTCP : Network {
    override fun receiveMessage() {
        //Here we create fake messages that came from the net, let's pretend it's real
        val message = EncryptorDecryptor.encrypt(
                Packet(
                        0,
                        0,
                        Message(
                                CommandTypes.ADD_GROUP.ordinal,
                                0,
                                "hello"
                        )
                )
        )

        Decryption.decrypt(message)
    }

    override fun sendMessage(message: ByteArray, target: InetAddress) {
        println("""
                |Sending message:
                |	'$message'
                |To:
                |	$target
                |
                """
                .trimMargin()
        )

    }
}