package net.packet

import java.nio.ByteBuffer

class Packet(var clientID: Byte, var msgID: Long, var msg: Message) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Packet

        if (clientID != other.clientID) return false
        if (msgID != other.msgID) return false
        if (msg != other.msg) return false

        return true
    }

    override fun toString(): String {
        return "net.Payload(clientID=$clientID, msgID=$msgID, msg=$msg)"
    }

    override fun hashCode(): Int {
        var result = clientID.toInt()
        result = 31 * result + msgID.hashCode()
        result = 31 * result + msg.hashCode()
        return result
    }

    fun toBytes(): ByteArray =
            ByteBuffer.allocate(17 + msg.msg.size)
                    .put(clientID)
                    .putLong(msgID)
                    .put(msg.toBytes())
                    .array()



}

