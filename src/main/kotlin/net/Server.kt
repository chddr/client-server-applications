package net

import net.impl.NetworkTCP
import net.impl.NetworkUDP
import net.impl.Processor
import java.io.InputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

object Server {

    private val stopFlag = AtomicBoolean(false)

    private fun runConsole(inputStream: InputStream) {
        thread(start = true, isDaemon = true) {
            val input = Scanner(inputStream)
            while (input.next() != "quit")
                println("enter \"quit\" to end the server")
            println("waiting for all current messages to be processed and then shutting down")
            stopFlag.set(true);
        }
    }

    private fun run() {
        runConsole(System.`in`)

        val network = when (type) {
            NetProtocol.TCP -> NetworkTCP(Role.Server)
            else -> NetworkUDP(Role.Server) //TODO replace with NetworkUDP when done
        }

        println("Server running via:\n$network")

        while(true){
            if(stopFlag.get()) break
            try {
                network.receive()
            } catch (ignore: Exception){}
        }

        Processor.shutdown()
    }

    @JvmStatic
    fun main(args: Array<String>) = run()
}
