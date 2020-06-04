package net.impl

import net.NetProtocol
import net.SERVER_THREADS
import net.impl.Processor.Companion.waitForStop
import net.impl.tcp.ServerTCP
import net.impl.udp.ServerUDP
import net.interfaces.Server
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ServerRunner(type: NetProtocol = net.type) {

    private var server: Server = when(type) {
        NetProtocol.TCP -> ServerTCP()
        NetProtocol.UDP -> ServerUDP()
    }
    private val service = Executors.newFixedThreadPool(SERVER_THREADS)


    init {
        println("Starting console... (type \"quit\" to quit)\n")
        runConsole(System.`in`)

        println("accepting connections")
        launch()
        println("not starting new connections, waiting for all current messages to be processed and then shutting down")

        service.waitForStop()
        println("fin")
    }

    private fun launch() {
        loop@ while (true) {
            try {
                val thread = server.waitForThread()
                service.submit(thread)
            } catch (e: IOException) {
                when (e) {
                    is SocketException -> {
                        println("socket is being closed...")
                        break@loop
                    }//naturally should end up here and end
                    is SocketTimeoutException -> {
                        println("socket has been without connections for too long, now closing")
                        break@loop
                    }
                }
            }
        }
    }

    fun stop() = server.stop()


    private fun runConsole(inputStream: InputStream) {
        thread(start = true, isDaemon = true) {
            val input = BufferedReader(InputStreamReader(inputStream))
            while (input.readLine() != "quit"){}
//                println("enter \"quit\" to end the server")
            stop()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ServerRunner(NetProtocol.UDP)
            Processor.waitForProcessorStop()
        }
    }

}
