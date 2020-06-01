package net.packet

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

    fun toPacket(): ByteArray = EncryptorDecryptor.encrypt(this)

    companion object {
        fun fromBytes(array: ByteArray) = EncryptorDecryptor.decrypt(array)
    }

}

