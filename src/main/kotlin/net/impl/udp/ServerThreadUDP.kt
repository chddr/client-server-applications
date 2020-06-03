package net.impl.udp

import net.impl.Processor
import net.impl.udp.UtilsUDP.send
import net.interfaces.ServerThread
import net.packet.Packet
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class ServerThreadUDP(private var socket: DatagramSocket, private var address: Packet.ClientAddress): ServerThread {

    private val stopFlag = AtomicBoolean(false)
    private val packetQueue = ConcurrentLinkedQueue<Packet>()


    fun pass(p: Packet) {
        packetQueue.add(p)
    }

    override fun send(packet: Packet) {
        socket.send(packet)
    }

    override fun run() {
        println("$address connection accepted")

        while (!stopFlag.get()) {
            while (!packetQueue.isEmpty()) {
                val packet = packetQueue.poll()
                println("working on $packet")
                Processor.process(this, packet)
            }
        }

        println("$address coonnection closing")
    }

    override fun stop() {
        stopFlag.set(true)
    }

    fun isStopped(): Boolean {
        return stopFlag.get()
    }
}