package net.protocol

import java.net.InetAddress

class Packet(val clientID: Byte, val msgID: Long, val msg: Message, var clientAddress: ClientAddress? = null) {

    data class ClientAddress(val inetAddress: InetAddress, val port: Int)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Packet

        if (clientID != other.clientID) return false
        if (msgID != other.msgID) return false
        if (msg != other.msg) return false

        return true
    }



    override fun hashCode(): Int {
        var result = clientID.toInt()
        result = 31 * result + msgID.hashCode()
        result = 31 * result + msg.hashCode()
        return result
    }

    fun toPacket(): ByteArray = EncryptorDecryptor.encrypt(this)
    override fun toString(): String = """
            Packet(clientID=$clientID, msgID=$msgID, clientAddress=$clientAddress)
            Message: $msg""".trimIndent()

    companion object {
        val WITHOUT_MESSAGE_LEN: Int = 22
        val BEFORE_LEN: Int = 10
        val MAX_SIZE: Int = Message.MAX_SIZE + 1 + 1 + 8 + 4 + 2 + 2

        fun fromBytes(array: ByteArray) = EncryptorDecryptor.decrypt(array)
    }

}

