package net.impl.tcp

import net.SERVER_THREADS
import net.impl.Processor.Companion.waitForStop
import java.io.IOException
import java.net.ServerSocket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ServerTCP {

    private val service = Executors.newFixedThreadPool(SERVER_THREADS)
    private var serverSocket = ServerSocket(net.SERVER_PORT).also {
        it.soTimeout = 20_000 //setting timeout to reset accept()
    }

    init {
        println("Starting console... (type \"quit\" to quit)\n")
        runConsole()

        println("accepting connections")
        loop@ while (true) {
            try{
                service.submit(ServerThreadTCP(serverSocket.accept()))
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
        println("not starting new connections, waiting for all current messages to be processed and then shutting down")

        service.waitForStop()

        println("fin")
    }


    private fun runConsole() {
        thread(start = true, isDaemon = true) {
            val input = Scanner(System.`in`)
            while (input.next() != "quit")
                println("enter \"quit\" to end the server")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ServerTCP()
        }
    }

}
