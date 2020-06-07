
import net.protocol.BadDataException
import net.protocol.EncryptorDecryptor
import net.protocol.Message
import net.protocol.Message.ClientCommands.ADD_PRODUCT_TO_GROUP
import net.protocol.Packet
import org.apache.commons.codec.binary.Hex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EncodingTests {
    @Test
    fun wrongMagicByte() {
        val badData = byteArrayOf(
                0x12, 0xA, 0xA
        )
        Assertions.assertThrows(BadDataException::class.java,
                { EncryptorDecryptor.decrypt(badData) },
                "magic byte is wrong, should fail"
        )
    }

    @Test
    fun shortMessageException() {
        val badData = byteArrayOf(
                0x13
        )
        Assertions.assertThrows(IndexOutOfBoundsException::class.java,
                { EncryptorDecryptor.decrypt(badData) },
                "decode() should fail, message is too short"
        )
    }

    @Test
    fun wrongMsgLength() {
        val inputMessage = "13 00 0000000000000001 000000CC 462F 48656C6C6F20776F726C6421 37B9".replace(" ", "")
        val byteMessage = Hex.decodeHex(inputMessage)
        Assertions.assertThrows(IndexOutOfBoundsException::class.java,
                { EncryptorDecryptor.decrypt(byteMessage) },
                "decode() can't read so many bytes and should fail"
        )
    }

    @Test
    fun wrongFirstCRC() {
        val inputMessage = "13 00 0000000000000001 0000000C FAFA 48656C6C6F20776F726C6421 37B9".replace(" ", "")
        val byteMessage = Hex.decodeHex(inputMessage)
        Assertions.assertThrows(BadDataException::class.java,
                { EncryptorDecryptor.decrypt(byteMessage) },
                "provided wrong CRC"
        )
    }

    @Test
    fun wrongSecondCRC() {
        val inputMessage = "13 00 0000000000000001 0000000C 162F 48656C6C6F20776F726C6421 FAFA".replace(" ", "")
        val byteMessage = Hex.decodeHex(inputMessage)
        Assertions.assertThrows(BadDataException::class.java,
                { EncryptorDecryptor.decrypt(byteMessage) },
                "provided wrong CRC"
        )
    }

    @Test
    fun encodeDecodeTest() {
        val payload = Packet(
                2,
                4,
                Message(
                        ADD_PRODUCT_TO_GROUP.ordinal,
                        0,
                        "Hello world!"
                )
        )

        val decoded = EncryptorDecryptor.decrypt(
                EncryptorDecryptor.encrypt(payload)
        )

        Assertions.assertEquals(payload, decoded)
    }


}