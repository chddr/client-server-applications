package net.impl.tcp

import net.impl.Processor
import net.impl.tcp.UtilsTCP.receive
import net.impl.tcp.UtilsTCP.send
import net.interfaces.ServerThread
import net.packet.Packet
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class ServerThreadTCP(private val socket: Socket) : ServerThread {

    private val stopFlag = AtomicBoolean(false)

    init {
        socket.soTimeout = net.SOCKET_TIMEOUT_TIME_MILLISECONDS/4
    }

    override fun run() {
        println("$socket accepted")

        while (!stopFlag.get()) {
            try {
                val packet = socket.receive()
                Processor.process(this, packet)
            } catch (e: Exception) {
                when (e) {
                    is SocketException -> println("socket is being closed")
                    is SocketTimeoutException -> {
                        stop()
                        println("socket timed out, closing")
                    }
                    is IOException -> println("closing the socket")
                    else -> println("failed reading the packet")
                }
            }
        }

        println("$socket closing")
        socket.close()
    }

    override fun send(packet: Packet) {
        socket.send(packet)
    }

    override fun stop() {
        stopFlag.set(true)
        socket.close()
    }


}