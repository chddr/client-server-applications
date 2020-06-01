package net.impl

import net.Network
import net.Role
import net.impl.NetworkTCP.State.*
import net.packet.MAGIC_BYTE
import net.packet.Packet
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer


class NetworkTCP(private var role: Role) : Network {

    private var socket: Socket
    private var outStream: OutputStream
    private var inStream: InputStream

    private enum class State { MAGIC, START, LENGTH, MESSAGE, END }

    init {
        socket = when (role) {
            Role.Server -> ServerSocket(net.SERVER_PORT).accept()
            Role.Client -> Socket("localhost", net.SERVER_PORT)
        }
        outStream = socket.getOutputStream()
        inStream = socket.getInputStream()
    }

    override fun receive(): Packet? {
        var state = MAGIC
        var incompletePackageFlag = true;

        var byteBuffer = ByteBuffer.allocate(1)
        val packet = ByteArrayOutputStream()
        val readByte = ByteArray(1)

        try {
            while (incompletePackageFlag && inStream.read(readByte) != -1) {
                byteBuffer.put(readByte)
                if (!byteBuffer.hasRemaining()) {
                    state = when (state) {
                        MAGIC -> {
                            if (readByte[0] == MAGIC_BYTE) {
                                byteBuffer = ByteBuffer.allocate(1 + 8) //1 clientID byte + 8 msgID bytes
                                START
                            } else {
                                MAGIC
                            }
                        }
                        START -> {
                            byteBuffer = ByteBuffer.allocate(Integer.BYTES)
                            LENGTH
                        }
                        LENGTH -> {
                            val wLen = byteBuffer.getInt(0)
                            byteBuffer = ByteBuffer.allocate(2 + wLen + 2) // commandType, userID - 4, both CRCs - 2,
                            MESSAGE
                        }
                        MESSAGE -> {
                            incompletePackageFlag = false
                            END
                        }
                        END -> END //shouldn't be here
                    }
                }
                packet.write(readByte)
            }
        } catch (e: Exception) {
            throw IOException(e)
        }

        if (incompletePackageFlag) throw IOException("Packet couldn't be read")


        val received: Packet?

        try {
            received = Packet.fromBytes(packet.toByteArray())
            println("Received:")
            println("$received\n")

        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error decoding the packet")
        }

        return when (role) {
            Role.Server -> {
                Processor.process(this, received)
                null
            }
            Role.Client -> received
        }
    }

    override fun close() = socket.close()

    override fun send(packet: Packet) {
        val packetBytes: ByteArray = packet.toPacket()
        outStream.write(packetBytes)
        outStream.flush()

        println("Sending:")
        println("$packet\n")
    }

    override fun toString(): String {
        return "NetworkTCP\nrole=$role\n socket=$socket\n\n"
    }


}