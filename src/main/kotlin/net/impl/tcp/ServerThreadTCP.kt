package net.impl.tcp

import net.impl.tcp.UtilsTCP.receive
import net.impl.tcp.UtilsTCP.send
import net.interfaces.ServerThread
import net.protocol.Packet
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class ServerThreadTCP(private val socket: Socket) : ServerThread() {

    private val stopFlag = AtomicBoolean(false)

    init {
        socket.soTimeout = net.SINGLE_THREAD_TIMEOUT
    }

    override fun run() {
        println("$socket accepted")

        processPackets()

        println("$socket closing")
    }

    override fun processPackets() {
        while (!stopFlag.get()) {
            try {
                val packet = socket.receive()
                process(packet)
            } catch (e: Exception) {
                when (e) {
                    is SocketException -> stop()
                    is SocketTimeoutException -> stop()
                    is IOException -> stop()
                    else -> {
                    }
                }
            }
        }
    }

    override fun send(packet: Packet) = socket.send(packet)

    override fun stop() {
        println("socket is closing")
        if (!socket.isClosed)
            socket.close()
        stopFlag.set(true)
    }


}