package net.interfaces

import net.protocol.Packet

interface ServerThread: Runnable {
    fun send(packet: Packet)
    fun stop()
}