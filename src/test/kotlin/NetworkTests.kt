
import net.impl.tcp.ClientTCP
import net.impl.tcp.ServerTCP
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class NetworkTests {

    @Test
    fun manyTCPConnections() {

        val t = thread {
            ServerTCP()
        }

        for (i in 0 until 10){
            thread {
                ClientTCP(i.toByte())//should be successful if no exceptions are thrown
            }
        }

        t.join()
    }
}