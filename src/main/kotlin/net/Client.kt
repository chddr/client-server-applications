
import net.NetProtocol
import net.Role
import net.impl.NetworkTCP
import net.impl.NetworkUDP
import net.impl.Processor
import net.packet.Message
import net.packet.Packet
import net.type
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
            val network = when (type) {
                NetProtocol.TCP -> NetworkTCP(Role.Client)
                NetProtocol.UDP -> NetworkUDP(Role.Client)
            }
            network.send(packet)
            network.receive()
            Thread.sleep(200)
            network.send(secondPacket)
            network.receive()

            Processor.shutdown()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = run()
}