import net.impl.Decryption
import net.impl.Encryption
import net.impl.NetworkTCP
import net.impl.Processor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {

    val nThreads = 4;
    val service = Executors.newFixedThreadPool(nThreads)

    repeat(nThreads) {
        service.submit(
                Thread {
                    NetworkTCP.receiveMessage()
                }
        )
    }


    try {
        service.shutdown()
        while (!service.awaitTermination(10, TimeUnit.SECONDS)) {
            println("waiting for messages to be sent")
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    Decryption.shutdown()
    Processor.shutdown()
    Encryption.shutdown()

}