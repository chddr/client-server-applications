package net.common

import net.NetProtocol
import net.SERVER_THREADS
import net.SERVER_TIMEOUT
import net.common.Processor.waitForStop
import net.tcp.ServerTCP
import net.udp.ServerUDP
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class ServerRunner(type: NetProtocol = net.type, timeout: Int = SERVER_TIMEOUT) {

    private val stopFlag = AtomicBoolean(false)
    private var server: Server = when (type) {
        NetProtocol.TCP -> ServerTCP(timeout)
        NetProtocol.UDP -> ServerUDP(timeout)
    }
    private val service = Executors.newFixedThreadPool(SERVER_THREADS)


    init {
        println("Starting console... (type \"quit\" to quit)\n")
        runConsole(System.`in`)

        println("accepting connections through $type")
        launch()
        println("not starting new connections, waiting for all current messages to be processed and then shutting down")

        service.waitForStop("ServerRunner")
        println("fin")
    }

    private fun launch() {
        while (!stopFlag.get()) {
            try {
                val thread = server.waitForThread()
                service.submit(thread)
            } catch (e: IOException) {
                when (e) {
                    is SocketException -> println("socket is being closed...")
                    is SocketTimeoutException -> println("socket has been without connections for too long, now closing")//naturally should end up here and end
                    else -> println("IOException encountered, emergency closing")
                }
                stop()
            }
        }
    }

    fun stop() {
        stopFlag.set(true)
        server.close()
    }


    private fun runConsole(inputStream: InputStream) {
        thread(start = true, isDaemon = true) {
            val input = BufferedReader(inputStream.reader())
            while (input.readLine() != "quit") {
//                print("enter \"quit\" to end the server")
                Unit
            }
            stop()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ServerRunner(NetProtocol.TCP)
            Processor.waitForProcessorStop()
        }
    }

}
