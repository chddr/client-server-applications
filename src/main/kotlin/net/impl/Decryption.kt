package net.impl

import net.packet.EncryptorDecryptor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Decryption {

    private val service = Executors.newFixedThreadPool(DECRYPTION_THREADS)

    fun decrypt(bytes: ByteArray) {
        service.submit(
                Thread {
                    val message = EncryptorDecryptor.decrypt(bytes)
                    Processor.process(message)
                }
        )
    }

    fun shutdown() {
        service.shutdown()
        while (!service.awaitTermination(10, TimeUnit.SECONDS)) {
            println("Decryption is waiting for shutdown")
        }
    }
}
