package net.interfaces

import net.packet.Packet

interface ServerThread: Runnable {
    fun send(packet: Packet)
    fun stop()
}