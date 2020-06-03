package net.impl.tcp

import net.SERVER_THREADS
import net.impl.Processor.Companion.waitForStop
import net.interfaces.Server
import java.net.ServerSocket
import java.util.concurrent.Executors

class ServerTCP: Server {

    private val service = Executors.newFixedThreadPool(SERVER_THREADS)
    private var serverSocket = ServerSocket(net.SERVER_PORT).also {
        it.soTimeout = net.SOCKET_TIMEOUT_TIME_MILLISECONDS //setting timeout to reset accept()
    }

    override fun serverCycle() {
        service.submit(ServerThreadTCP(serverSocket.accept()))
    }

    override fun stop() {
        serverSocket.close()
    }

    override fun waitForStop() = service.waitForStop()


}
