package net

import net.packet.Packet


interface Network {
    fun receive(): Packet?
    fun send(packet: Packet)
}