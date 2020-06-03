package net.interfaces

import net.packet.Packet

interface ServerThread {
    fun send(packet: Packet)
}