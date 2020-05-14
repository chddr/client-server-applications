
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import CRC16.compute_short as crc16short


object DataEncoder {
    private const val MAGIC_BYTE: Byte = 0x13
    private val encryption: Cipher
    private val decryption: Cipher

    init {
        val key = KeyGenerator.getInstance("AES").generateKey()

        encryption = Cipher.getInstance("AES")
        encryption.init(Cipher.ENCRYPT_MODE, key)

        decryption = Cipher.getInstance("AES")
        decryption.init(Cipher.DECRYPT_MODE, key)
    }


    fun decode(data: ByteArray): Payload {
        if (data[0] != MAGIC_BYTE) {
            throw BadDataException("Wrong first 'magic byte' received, expected $MAGIC_BYTE")
        }

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


        return Payload(
                clientID,
                msgID,
                decryption.doFinal(msg)
        )
    }

    fun encode(info: Payload): ByteArray {
        val msg = encryption.doFinal(info.msg)
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