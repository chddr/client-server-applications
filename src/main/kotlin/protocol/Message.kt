package protocol

import java.nio.ByteBuffer
import java.nio.charset.Charset

class Message(val cType: Int, val userID: Int = 0, val msg: String) {

    enum class ClientCommands {
        BYE ,GET_PRODUCT_COUNT, ADD_GROUP, ADD_PRODUCT, INCREASE_PRODUCT_COUNT, DECREASE_PRODUCT_COUNT, SET_PRODUCT_PRICE;

        companion object {
            operator fun contains(int: Int) = int in values().indices
            operator fun get(cType: Int) = values()[cType]
        }
    }

    enum class ServerCommands {
        OK, BYE, RESEND, INTERNAL_ERROR, WRONG_COMMAND, ERROR, NO_SUCH_PRODUCT, ID_PRODUCT_COUNT, ID
    }

    constructor(data: ByteArray) : this(
            cType = ByteBuffer.wrap(data, 0, 4).int,
            userID = ByteBuffer.wrap(data, 4, 4).int,
            msg = data.copyOfRange(8, data.size).toString(Charset.defaultCharset())
    )

    constructor(cType: ClientCommands, userID: Int = 0, msg: String) : this(cType.ordinal, userID, msg)
    constructor(cType: ServerCommands, userID: Int = 0, msg: String) : this(cType.ordinal, userID, msg)

    fun toBytes(): ByteArray =
            ByteBuffer.allocate(4 + 4 + msg.length)
                    .putInt(cType)
                    .putInt(userID)
                    .put(msg.toByteArray())
                    .array()

    override fun toString(): String {
        return "Message(cType=$cType, userID=$userID, msg=${msg})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (cType != other.cType) return false
        if (userID != other.userID) return false
        if (!msg.contentEquals(other.msg)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cType
        result = 31 * result + userID
        result = 31 * result + msg.hashCode()
        return result
    }


    companion object {
        private val MSG_MAX_SIZE = 256
        val MAX_SIZE: Int = 8 + MSG_MAX_SIZE
    }

}