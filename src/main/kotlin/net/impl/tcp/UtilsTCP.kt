package net.impl.tcp

import net.packet.Packet
import java.net.Socket
import java.nio.ByteBuffer

object UtilsTCP {
    fun Socket.receive(): Packet {
        val byteBuffer = ByteBuffer.wrap(
                ByteArray(Packet.MAX_SIZE).also {
                    getInputStream()?.read(it)
                })

        return byteBuffer.getInt(Packet.BEFORE_LEN).let {
            byteBuffer.array().copyOf(Packet.WITHOUT_MESSAGE_LEN + it)
        }.let {
            Packet.fromBytes(it)
        }.also {
            println("[[RECEIVED]]   $this")
            println("$it\n")
        }

    }

    fun Socket.send(packet: Packet) {
        println("[[SENDING FROM]]   $this")
        println("$packet\n")

        getOutputStream()?.run {
            write(packet.toPacket())
            flush()
        }
    }
}