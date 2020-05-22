package net

import java.nio.ByteBuffer

class Message(var cType: Int, var userID: Int, var msg: ByteArray) {

    constructor(cType: Int, userID: Int, msg: String): this(
            cType,
            userID,
            msg.toByteArray()
    )


    fun toBytes(): ByteArray =
            ByteBuffer.allocate(4 + 4 + msg.size)
                    .putInt(cType)
                    .putInt(userID)
                    .put(msg)
                    .array()

}