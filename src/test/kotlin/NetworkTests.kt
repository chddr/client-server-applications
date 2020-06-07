
import net.NetProtocol
import net.impl.ServerRunner
import net.impl.tcp.ClientTCP
import net.impl.udp.ClientUDP
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class NetworkTests {

    @Test
    fun manyTCPConnections() {

        val t = thread(start = true) {
            ServerRunner(NetProtocol.TCP, 1000)
        }

        repeat(100) {
            thread {
                ClientTCP(0)//should be successful if no exceptions are thrown
            }
        }


        t.join()
    }

    @Test
    fun manyUDPConnections() {

        val t = thread(start = true) {
            ServerRunner(NetProtocol.UDP, 1000)
        }

        repeat(100) {
            thread {
                ClientUDP(0)//should be successful if no exceptions are thrown
            }
        }


        t.join()
    }
}