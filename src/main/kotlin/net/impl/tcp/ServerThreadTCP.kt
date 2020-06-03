package net.impl.tcp

import net.interfaces.ServerThread
import net.impl.Processor
import net.impl.tcp.UtilsTCP.receive
import net.impl.tcp.UtilsTCP.send
import net.packet.Message.ServerCommandTypes.SERVER_RESPONSE_BYE
import net.packet.Packet
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class ServerThreadTCP(private val socket: Socket) : Runnable, ServerThread {

    private val stopFlag = AtomicBoolean(false)
    private val queue = ConcurrentLinkedQueue<Packet>()

    override fun run() {
        println("$socket accepted")

        while (!stopFlag.get()) {
            try {
                val packet = socket.receive()
                Processor.process(this, packet)
            } catch (e: Exception) {
                when (e) {
                    is SocketException -> println("socket is being closed")
                    is IOException -> println("closing the socket")
                    else -> println("failed reading the packet")
                }
            } //TODO ignored for now, later should be changed to something meaningful

        }

        println("$socket closing")
        socket.close()
    }

    override fun send(packet: Packet) {
        socket.send(packet)
        if (packet.msg.cType == SERVER_RESPONSE_BYE.ordinal)
            stop()
    }

    private fun stop() {
        stopFlag.set(true)
        socket.close()
    }


}