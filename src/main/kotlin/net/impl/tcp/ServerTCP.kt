package net.impl.tcp

import net.SERVER_THREADS
import net.impl.Processor
import net.impl.Processor.Companion.waitForStop
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ServerTCP {

    private val service = Executors.newFixedThreadPool(SERVER_THREADS)
    private var serverSocket = ServerSocket(net.SERVER_PORT).also {
        it.soTimeout = net.SOCKET_TIMEOUT_TIME_MILLISECONDS //setting timeout to reset accept()
    }
    init {
        println("Starting console... (type \"quit\" to quit)\n")
        runConsole(System.`in`)

        println("accepting connections")
        while (true) {
            try{
                service.submit(ServerThreadTCP(serverSocket.accept()))
            } catch (e: IOException) {
                when (e) {
                    is SocketException -> {
                        println("socket is being closed...")
                        stop()
                    }//naturally should end up here and end
                    is SocketTimeoutException -> {
                        println("socket has been without connections for too long, now closing")
                        stop()
                    }
                }
            }
        }
        println("not starting new connections, waiting for all current messages to be processed and then shutting down")

        service.waitForStop()
        Processor.waitForProcessorStop()
        println("fin")
    }


    private fun runConsole(inputStream: InputStream) {
        thread(start = true, isDaemon = true) {
            val input = BufferedReader(InputStreamReader(inputStream))
            while (input.readLine() != "quit")
                println("enter \"quit\" to end the server")
            stop()
        }
    }

    private fun stop() {
        println("stop()")
        serverSocket.close()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ServerTCP()
        }
    }

}
