
import net.Network
import net.Role
import net.impl.NetworkTCP
import net.impl.Processor
import net.packet.Message
import net.packet.Packet
import java.io.IOException


object Client {
    private fun run() {
        val packet = Packet(
                1,
                1,
                Message(Message.CommandTypes.CLIENT_HELLO, 1, "hello"))
        val secondPacket = Packet(
                1,
                1,
                Message(Message.CommandTypes.CLIENT_BYE, 1, "bye"))

        try {
            val network: Network = NetworkTCP(Role.Client)
            network.send(packet)
            network.receive()
            Thread.sleep(200)
            network.send(secondPacket)
            network.receive()

            network.close()
            Processor.shutdown()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = run()
}