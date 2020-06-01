package net.impl.tcp

import net.Network
import net.impl.Processor
import net.impl.tcp.UtilsTCP.receive
import net.impl.tcp.UtilsTCP.send
import net.packet.Packet
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class ServerThreadTCP(private val socket: Socket) : Runnable, Network {

    private val stopFlag = AtomicBoolean(false)

    override fun run() {
        println("$socket accepted")

        while (!stopFlag.get()) {

            try {
                val packet = socket.receive()
                Processor.process(this, packet)
            } catch (e: Exception) {
                println("failed reading the packet")
            } //TODO ignored for now, later should be changed to something meaningful

        }

        println("$socket closing")
        socket.close()
    }

    override fun send(packet: Packet) = socket.send(packet)

    override fun stop() {
        stopFlag.set(true)
    }


}