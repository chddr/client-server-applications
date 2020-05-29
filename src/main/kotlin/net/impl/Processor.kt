package net.impl

import net.packet.Message
import net.packet.Packet
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Processor(private val packet: Packet) : Runnable {

    companion object {
        private val service = Executors.newFixedThreadPool(PROCESSOR_THREADS)

        fun process(packet: Packet) {
            service.submit(Processor(packet))
        }

        fun shutdown() {
            service.shutdown()
            while (!service.awaitTermination(10, TimeUnit.SECONDS)){
                println("Processor is waiting for shutdown")
            }
        }

    }


    override fun run() {
        println("""
                |Thread ${Thread.currentThread().id} Received message:
                |	'$packet'
                |
                """
                .trimMargin()
        )

        //simulating real work done
        Thread.sleep(2000)

        val msg = when (packet.msg.msg) {
            "hello" -> "Hello from server, it's ${LocalDateTime.now().toLocalTime()}"
            else -> "BYE"
        }

        Encryption.encrypt(
                Packet(
                        0,
                        0,
                        Message(
                                Message.CommandTypes.SERVER_RESPONSE.ordinal,
                                0,
                                msg
                        )
                )
        )
    }
}