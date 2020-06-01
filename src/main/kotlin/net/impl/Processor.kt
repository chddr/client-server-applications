package net.impl

import net.Network
import net.PROCESSOR_THREADS
import net.packet.Message
import net.packet.Message.ClientCommandTypes.CLIENT_BYE
import net.packet.Message.ServerCommandTypes.SERVER_RESPONSE_OK
import net.packet.Packet
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Processor(private val network: Network, private val packet: Packet) : Runnable {

    companion object {
        private val service = Executors.newFixedThreadPool(PROCESSOR_THREADS)

        fun process(network: Network, packet: Packet) {
            service.submit(Processor(network, packet))
        }

        fun ExecutorService.waitForStop() {
            shutdown()
            while (!awaitTermination(10, TimeUnit.SECONDS)) {
                println("Service is waiting for shutdown")
            }
        }
    }


    override fun run() {
        println("[[PROCESSING]]    ${Thread.currentThread().id}-th working PROCESSOR thread")
        println("$packet\n")

        //simulating real work done
        Thread.sleep(2000)

        val msg = when (packet.msg.msg) {
            "hello" -> "Hello from server, it's ${LocalDateTime.now().toLocalTime()}"
            else -> "BYE"
        }


        network.send(Packet(
                clientID = 0,
                msgID = 0,
                msg = Message(SERVER_RESPONSE_OK.ordinal, userID = 0, msg = msg),
                clientAddress = packet.clientAddress))

        if (packet.msg.cType == CLIENT_BYE.ordinal)
            network.stop()
    }

}