package protocol

import java.nio.ByteBuffer
import java.nio.charset.Charset

class Message(val cType: Int, val userID: Int = 0, val msg: String) {

    enum class ClientCommands {

        GET_PRODUCT, GET_TIME, GET_GROUP, GET_PRODUCT_LIST, GET_GROUP_LIST,
        ADD_GROUP, ADD_PRODUCT,
        DELETE_PRODUCT, DELETE_GROUP,
        CHANGE_PRODUCT_NAME, CHANGE_GROUP_NAME, SET_PRODUCT_PRICE,
        INCREASE_PRODUCT_COUNT, DECREASE_PRODUCT_COUNT,
        BYE;

        companion object {
            operator fun contains(int: Int) = int in values().indices
            operator fun get(cType: Int) = values()[cType]
        }
    }

    enum class ServerCommands {
        SERVER_BYE, RESEND,
        INTERNAL_ERROR, WRONG_COMMAND_ERROR, NO_SUCH_ID_ERROR, NAME_TAKEN_ERROR, WRONG_MESSAGE_FORMAT_ERROR,
        NOT_ENOUGH_ITEMS_ERROR, WRONG_NAME_ERROR, NON_EMPTY_PRODUCT_ERROR,
        DELETED_PRODUCT, DELETED_GROUP,
        GROUP_LIST, PRODUCT_LIST,
        PRODUCT, SERVER_TIME, GROUP;

        companion object {
            operator fun contains(int: Int) = int in ClientCommands.values().indices
            operator fun get(cType: Int) = ClientCommands.values()[cType]
        }
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
        private const val MSG_MAX_SIZE = 1024 //TODO think about how to limit it so it will never overflow
        const val MAX_SIZE: Int = 8 + MSG_MAX_SIZE
    }

}