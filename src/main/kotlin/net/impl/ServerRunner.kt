package net.impl.tcp

import net.NetProtocol
import net.impl.Processor
import net.impl.udp.ServerUDP
import net.interfaces.Server
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

class ServerRunner(type: net.NetProtocol = net.type) {

    private var server: Server = when(type) {
        NetProtocol.TCP -> ServerTCP()
        NetProtocol.UDP -> ServerUDP()
    }
    init {
        println("Starting console... (type \"quit\" to quit)\n")
        runConsole(System.`in`)

        println("accepting connections")
        loop@ while (true) {
            try{
               server.serverCycle()
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

        server.waitForStop()
        println("fin")
    }


    private fun runConsole(inputStream: InputStream) {
        thread(start = true, isDaemon = true) {
            val input = BufferedReader(InputStreamReader(inputStream))
            while (input.readLine() != "quit")
                println("enter \"quit\" to end the server")
            server.stop()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ServerRunner()
            Processor.waitForProcessorStop()
        }
    }

}
