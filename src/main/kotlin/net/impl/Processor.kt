package net.impl

import net.PROCESSOR_THREADS
import net.interfaces.ServerThread
import net.packet.Message
import net.packet.Message.ServerCommandTypes.SERVER_RESPONSE_BYE
import net.packet.Message.ServerCommandTypes.SERVER_RESPONSE_OK
import net.packet.Packet
import java.time.LocalDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Processor(private val serverThread: ServerThread, private val packet: Packet) : Runnable {

    companion object {
        private val service = Executors.newFixedThreadPool(PROCESSOR_THREADS)

        fun process(serverThread: ServerThread, packet: Packet) {
            service.submit(Processor(serverThread, packet))
        }

        fun ExecutorService.waitForStop() {
            shutdown()
            while (!isTerminated) {
                awaitTermination(10, TimeUnit.SECONDS)
                println("Service is waiting for shutdown")
            }
        }

        fun waitForProcessorStop() = service.waitForStop()
    }


    override fun run() {
        println("[[STARTED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread")
        println("$packet\n")

        //simulating real work done
        Thread.sleep(2000)

        val msg: String
        val cType: Int

        when (packet.msg.msg) {
            "hello" -> {
                msg = "Hello from server, it's ${LocalDateTime.now().toLocalTime()}"
                cType = SERVER_RESPONSE_OK.ordinal
            }
            else -> {
                msg = "BYE"
                cType = SERVER_RESPONSE_BYE.ordinal
            }
        }


        serverThread.send(Packet(
                clientID = 0,
                msgID = 0,
                msg = Message(cType, userID = 0, msg = msg),
                clientAddress = packet.clientAddress))

        if (packet.msg.cType == SERVER_RESPONSE_BYE.ordinal)
            serverThread.stop()
        println("[[ENDED THREAD]]    ${Thread.currentThread().id}-th working PROCESSOR thread")

    }

}