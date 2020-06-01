package net

import net.packet.Packet

interface Network {
    fun send(packet: Packet)
    fun stop()
}