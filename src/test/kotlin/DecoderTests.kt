import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DecoderTests {
    @Test
    fun wrongMagicByte() {
        val badData = byteArrayOf(
                0x12, 0xA, 0xA
        )
        Assertions.assertThrows(BadDataException::class.java,
                { DataEncoder.decode(badData) },
                "magic byte is wrong, should fail"
        )
    }

    @Test
    fun shortMessageException() {
        val badData = byteArrayOf(
                0x13
        )
        Assertions.assertThrows(IndexOutOfBoundsException::class.java,
                { DataEncoder.decode(badData) },
                "decode() should fail, message is too short"
        )
    }

    @Test
    @Throws(DecoderException::class)
    fun wrongMsgLength() {
        val inputMessage = "13 00 0000000000000001 000000CC 462F 48656C6C6F20776F726C6421 37B9".replace(" ", "")
        val byteMessage = Hex.decodeHex(inputMessage)
        Assertions.assertThrows(IndexOutOfBoundsException::class.java,
                { DataEncoder.decode(byteMessage) },
                "decode() can't read so many bytes and should fail"
        )
    }

    @Test
    @Throws(DecoderException::class)
    fun wrongFirstCRC() {
        val inputMessage = "13 00 0000000000000001 0000000C FAFA 48656C6C6F20776F726C6421 37B9".replace(" ", "")
        val byteMessage = Hex.decodeHex(inputMessage)
        Assertions.assertThrows(BadDataException::class.java,
                { DataEncoder.decode(byteMessage) },
                "provided wrong CRC"
        )
    }

    @Test
    @Throws(DecoderException::class)
    fun wrongSecondCRC() {
        val inputMessage = "13 00 0000000000000001 0000000C 162F 48656C6C6F20776F726C6421 FAFA".replace(" ", "")
        val byteMessage = Hex.decodeHex(inputMessage)
        Assertions.assertThrows(BadDataException::class.java,
                { DataEncoder.decode(byteMessage) },
                "provided wrong CRC"
        )
    }

    @Test
    fun encodeDecodeTest() {
        val payload = Payload(
                2,
                4,
                1,
                0,
                "Hello world!".toByteArray()
        )

        val decoded = DataEncoder.decode(
                DataEncoder.encode(payload)
        )

        Assertions.assertEquals(payload,
                decoded
        )
    }

}