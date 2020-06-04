package net.impl.udp

import net.impl.udp.UtilsUDP.receiveDatagram
import net.interfaces.Server
import net.interfaces.ServerThread
import net.protocol.Packet.ClientAddress
import java.io.IOException
import java.net.DatagramSocket
import java.net.SocketException
import java.net.SocketTimeoutException

class ServerUDP : Server {


    private val packetData = HashMap<ClientAddress, ServerThreadUDP>()
    private var serverSocket = DatagramSocket(net.SERVER_PORT).also {
        it.soTimeout = net.SERVER_TIMEOUT //timeout time, the same as ServerTCP
    }

    override fun waitForThread(): ServerThread {
        //remove connections that are already closed so they can be garbage collected
        packetData.values.removeIf(ServerThreadUDP::isStopped)

        while (true) {
            try {
                val packet = serverSocket.receiveDatagram()
                val address = ClientAddress(packet.address, packet.port)
                if (address in packetData) {
                    packetData[address]!!.pass(packet)
                } else {
                    val thread = ServerThreadUDP(serverSocket, address).apply { pass(packet) }
                    packetData[address] = thread
                    return thread
                }
            } catch (e: Exception) {
                when (e) {
                    is SocketException -> stop()
                    is SocketTimeoutException -> stop()
                    is IOException -> stop()
                    else -> {
                    }
                }
            }

        }
    }

    override fun stop() {
        serverSocket.close()
    }

}
