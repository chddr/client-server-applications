package net.impl

import net.Network
import net.Role
import net.packet.Packet
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer


class NetworkTCP(private var role: Role) : Network {

    private var socket: Socket?
    private var serverSocket: ServerSocket?

    init {
        when (role) {
            Role.Server -> {
                serverSocket = ServerSocket(net.SERVER_PORT)
                socket = null
            }
            Role.Client -> {
                serverSocket = null
                socket = Socket("localhost", net.SERVER_PORT)
            }
        }
    }

    private enum class State { MAGIC, START, LENGTH, MESSAGE, END }

    override fun receive(): Packet? {
        serverSocket!!.accept().use { socket: Socket ->

            val byteBuffer = ByteBuffer.wrap(
                    ByteArray(Packet.MAX_SIZE).also {
                        socket.getInputStream().read(it)
                    })

            val packet = byteBuffer.getInt(Packet.BEFORE_LEN).let {
                byteBuffer.array().copyOf(Packet.WITHOUT_MESSAGE_LEN + it)
            }.let { Packet.fromBytes(it) }

            println("Received:")
            println("$packet\n")

            return when (role) {
                Role.Server -> {
                    Processor.process(this, packet)
                    null
                }
                Role.Client -> packet
            }
        }
    }


    override fun send(packet: Packet) {
        println("Sending:")
        println("$packet\n")

        socket?.getOutputStream()?.run {
            write(packet.toPacket())
            flush()
        }
    }

    override fun toString(): String {
        return "NetworkTCP\nrole=$role\nsocket=$socket\n\n"
    }


}