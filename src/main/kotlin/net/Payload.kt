package net

class Payload(var clientID: Byte, var msgID: Long, var msg: ByteArray) {

    constructor(clientID: Byte, msgID: Long, msg: Message) : this(
            clientID,
            msgID,
            msg.toBytes()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Payload

        if (clientID != other.clientID) return false
        if (msgID != other.msgID) return false
        if (!msg.contentEquals(other.msg)) return false

        return true
    }

    override fun toString(): String {
        return "net.Payload(clientID=$clientID, msgID=$msgID, msg=${msg.contentToString()})"
    }

    override fun hashCode(): Int {
        var result = clientID.toInt()
        result = 31 * result + msgID.hashCode()
        result = 31 * result + msg.contentHashCode()
        return result
    }


}

