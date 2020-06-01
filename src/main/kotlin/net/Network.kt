package net

import net.packet.Packet


interface Network {
    fun receive(): Packet?
    fun close()
    fun send(packet: Packet)
}