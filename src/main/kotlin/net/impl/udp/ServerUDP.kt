package net.impl.udp

import net.interfaces.ServerThread
import net.SERVER_THREADS
import net.impl.Processor
import net.impl.Processor.Companion.waitForStop
import net.impl.udp.UtilsUDP.receive
import net.impl.udp.UtilsUDP.send
import net.packet.Packet
import java.net.DatagramSocket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class ServerUDP: ServerThread {

    private val service = Executors.newFixedThreadPool(SERVER_THREADS)

    private val packetData = HashMap<Int, Long>()
    private var serverSocket = DatagramSocket(net.SERVER_PORT).also {
        it.soTimeout = 5_000 //timeout time, the same as ServerTCP
    }
    private val stopFlag = AtomicBoolean(false)

    init {
        println("Starting console... (type \"quit\" to quit)\n")
        runConsole()

        println("running: ${serverSocket.localAddress}")


        println("accepting connections")
        loop@ while (!stopFlag.get()) {

            try {
                val p = serverSocket.receive()
                Processor.process(this, p)
            } catch (e: Exception) {
                when (e) {
                    is SocketException -> {
                        println("socket is being closed...")
                        break@loop
                    }//naturally should end up here and end
                    is SocketTimeoutException -> {
                        println("socket has been without connections for too long, now closing")
                        break@loop
                    }
                    else -> e.printStackTrace()
                }
            }



        }

        println("not starting new connections, waiting for all current messages to be processed and then shutting down")

        service.waitForStop()
        Processor.waitForProcessorStop()

        println("fin")
    }


    private fun runConsole() {
        thread(start = true, isDaemon = true) {
            val input = Scanner(System.`in`)
            while (input.next() != "quit")
                println("enter \"quit\" to end the server")
            stop()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ServerUDP()
        }
    }

    override fun send(packet: Packet) = serverSocket.send(packet)

    fun stop() {
        stopFlag.set(true)
        serverSocket.close()
    }
}

//fun main() {
//    val isRun = AtomicBoolean(true)
//
//    thread(start = true) {
//        try {
//            Thread.sleep(10_000L)
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//        isRun.set(false)
//    }
//
//
//    DatagramSocket(net.SERVER_PORT).use { serverSocket ->
//        serverSocket.soTimeout = 2_000
//        while (isRun.get()) {
//            try {
//                val inputMessage = ByteArray(1000)
//                val packet = DatagramPacket(inputMessage, inputMessage.size)
//                serverSocket.receive(packet)
//
//                thread(start = true) {
//                    val realMessageSize = packet.length
//                    println("Message from client ${String(inputMessage, 0, realMessageSize, StandardCharsets.UTF_8)}")
//
//                    val bytes = "Server is ok".toByteArray()
//
//                    val response = DatagramPacket(bytes, bytes.size, packet.address, packet.port)
//                    serverSocket.send(response)
//
//                }
//            } catch (e: SocketTimeoutException) {
//                println("Socket timeout")
//            }
//        }
//
//
//    }
//
//}
