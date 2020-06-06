package net.impl.udp

import net.impl.udp.UtilsUDP.ClientAddress
import net.impl.udp.UtilsUDP.send
import net.impl.udp.UtilsUDP.validate
import net.interfaces.ServerThread
import net.protocol.Packet
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class ServerThreadUDP(private var socket: DatagramSocket, private var address: ClientAddress) : ServerThread() {

    private val stopFlag = AtomicBoolean(false)
    private val packetQueue = LinkedBlockingQueue<DatagramPacket>()

    override fun send(packet: Packet) = socket.send(packet, address)

    override fun run() {
        println("$address connection accepted")
        processPackets()
        println("$address connection closing")
    }

    override fun processPackets() {//TODO since timeout here is not working (we have no thread, we need to do it ourselves) got to add some sort of closing after inactivity
        while (!stopFlag.get()) {
            while (!packetQueue.isEmpty()) {
                try {
                    val packet = validate(packetQueue.poll())
                    process(packet)
                } catch (e: Exception) {
                    println("had issues processing packet")
                }
            }
        }
    }

    fun pass(p: DatagramPacket) {
        packetQueue.add(p)
    }

    override fun close() {
        stopFlag.set(true)
    }

    fun isStopped(): Boolean {
        return stopFlag.get()
    }
}