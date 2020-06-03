package net.impl.udp

import net.SERVER_THREADS
import net.impl.Processor.Companion.waitForStop
import net.impl.udp.UtilsUDP.receive
import net.interfaces.Server
import net.packet.Packet.ClientAddress
import java.net.DatagramSocket
import java.util.concurrent.Executors

class ServerUDP : Server {

    private val service = Executors.newFixedThreadPool(SERVER_THREADS)

    private val packetData = HashMap<ClientAddress, ServerThreadUDP>()
    private var serverSocket = DatagramSocket(net.SERVER_PORT).also {
        it.soTimeout = net.SOCKET_TIMEOUT_TIME_MILLISECONDS //timeout time, the same as ServerTCP
    }


    override fun serverCycle() {
        val p = serverSocket.receive()

        val address = p.clientAddress!!
        if (address !in packetData) {
            val thread = ServerThreadUDP(serverSocket, address)
            service.submit(thread)
            packetData[address] = thread
        }
        packetData[address]!!.pass(p)

        //remove connections that are already closed so they can be garbage collected
        packetData.values.removeIf(ServerThreadUDP::isStopped)
    }

    override fun stop() {
        serverSocket.close()
    }

    override fun waitForStop() = service.waitForStop()

}
