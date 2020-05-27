package net.packet

import java.nio.ByteBuffer

class Message(var cType: CommandTypes, var userID: Int, var msg: ByteArray) {

    enum class CommandTypes {
        GET_PRODUCT_COUNT, GET_PRODUCT, ADD_PRODUCT, ADD_PRODUCT_GROUP, ADD_PRODUCT_TO_GROUP, SET_PRODUCT_PRICE
    }

    constructor(cType: CommandTypes, userID: Int, msg: String) : this(
            cType,
            userID,
            msg.toByteArray()
    )

    constructor(data: ByteArray) : this(
            cType = CommandTypes.values()[
                    ByteBuffer.wrap(data, 0, 4).int
            ],
            userID = ByteBuffer.wrap(data, 4, 4).int,
            msg = data.copyOfRange(8, data.size)
    )


    fun toBytes(): ByteArray =
            ByteBuffer.allocate(4 + 4 + msg.size)
                    .putInt(cType.ordinal)
                    .putInt(userID)
                    .put(msg)
                    .array()

    override fun toString(): String {
        return "Message(cType=$cType, userID=$userID, msg=${msg.contentToString()})"
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

}