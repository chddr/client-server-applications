package net.protocol

import java.nio.ByteBuffer
import java.util.*
import net.protocol.CRC16.computeShort as crc16short


object EncryptorDecryptor {

    fun decrypt(data: ByteArray): Packet {
        if (data[0] != MAGIC_BYTE)
            throw BadDataException("Wrong first 'magic byte' received, expected $MAGIC_BYTE")

        val clientID = data[1]
        val msgID = ByteBuffer.wrap(data, 2, 8).long
        val msgLen = ByteBuffer.wrap(data, 10, 4).int
        var crc = ByteBuffer.wrap(data, 14, 2).short
        var validateCRC = crc16short(data, 0, 14)

        if (crc != validateCRC)
            throw BadDataException(String.format("Incorrect checksum, data probably corrupted. Expected: %s, got: %s", validateCRC, crc))

        val msg = data.copyOfRange(16, 16 + msgLen)

        crc = ByteBuffer.wrap(data, 16 + msgLen, 2).short
        validateCRC = crc16short(data, 16, msgLen)

        if (crc != validateCRC)
            throw BadDataException(String.format("Incorrect checksum, data probably corrupted. Expected: %s, got: %s", validateCRC, crc))


        return Packet(
                clientID,
                msgID,
                Message(Base64.getDecoder().decode(msg))
        )
    }

    fun encrypt(info: Packet): ByteArray {
        val msg = Base64.getEncoder().encode(info.msg.toBytes())
        val buffer = ByteBuffer.allocate(18 + msg.size)

        return buffer
                .put(MAGIC_BYTE)
                .put(info.clientID)
                .putLong(info.msgID)
                .putInt(msg.size)
                .putShort(crc16short(buffer.array(), 0, 14)) //CRC of previous four fields
                .put(msg)
                .putShort(crc16short(buffer.array(), 16, msg.size))
                .array()
    }
}

