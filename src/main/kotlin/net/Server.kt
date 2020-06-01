package net

import net.impl.NetworkTCP
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
            else -> NetworkTCP(Role.Server) //TODO replace with NetworkUDP when done
        }

        println("Server running via:\n $network")

        for (i in 0..1){
            if(stopFlag.get()) break
            network.receive()
        }

        Processor.shutdown()
        network.close()
    }

    @JvmStatic
    fun main(args: Array<String>) = run()
}
