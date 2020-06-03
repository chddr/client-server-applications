package net.impl.udp

import net.impl.udp.UtilsUDP.receive
import net.interfaces.Server
import net.interfaces.ServerThread
import net.protocol.Packet.ClientAddress
import java.net.DatagramSocket

class ServerUDP : Server {


    private val packetData = HashMap<ClientAddress, ServerThreadUDP>()
    private var serverSocket = DatagramSocket(net.SERVER_PORT).also {
        it.soTimeout = net.SERVER_TIMEOUT //timeout time, the same as ServerTCP TODO change here and in TCP to 0
    }

    override fun waitForThread(): ServerThread {
        //remove connections that are already closed so they can be garbage collected
        packetData.values.removeIf(ServerThreadUDP::isStopped)

        while (true) {
            val p = serverSocket.receive() //TODO make here catch bad decoding exception, if it doesn't decode, just ignore
            val address = p.clientAddress!!
            if (address in packetData) {
                packetData[address]!!.pass(p)
            } else {
                val thread = ServerThreadUDP(serverSocket, address).apply { pass(p) }
                packetData[address] = thread
                return thread
            }

        }
    }

    override fun stop() {
        serverSocket.close()
    }

}
