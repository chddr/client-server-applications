package net.impl.udp

import net.impl.udp.UtilsUDP.ClientAddress
import net.impl.udp.UtilsUDP.receiveDatagram
import net.interfaces.Server
import net.interfaces.ServerThread
import java.net.DatagramSocket

class ServerUDP(timeout: Int) : Server {


    private val packetData = HashMap<ClientAddress, ServerThreadUDP>()
    private val serverSocket = DatagramSocket(net.SERVER_PORT).also {
        it.soTimeout = timeout //timeout time, the same as ServerTCP
    }

    override fun waitForThread(): ServerThread {
        //remove connections that are already closed so they can be garbage collected
        packetData.values.removeIf(ServerThreadUDP::isStopped)

        while (true) {
            val packet = serverSocket.receiveDatagram()
            val address = ClientAddress.from(packet)
            if (address in packetData) {
                packetData[address]!!.pass(packet)
            } else {
                val thread = ServerThreadUDP(serverSocket, address).apply { pass(packet) }
                packetData[address] = thread
                return thread
            }

        }
    }

    override fun stop() {
        serverSocket.close()
    }

}
