package net.impl

import net.packet.EncryptorDecryptor
import net.packet.Packet
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Encryption {

    private val service = Executors.newFixedThreadPool(ENCRYPTION_THREADS)

    fun encrypt(pkt: Packet) {
        service.submit(
                Thread {
                    val message = EncryptorDecryptor.encrypt(pkt)
                    NetworkTCP.sendMessage(message, InetAddress.getLocalHost())
                }
        )
    }

    fun shutdown() {
        service.shutdown()
        while (!service.awaitTermination(10, TimeUnit.SECONDS)) {
            println("Encryption is waiting for shutdown")
        }
    }


}

