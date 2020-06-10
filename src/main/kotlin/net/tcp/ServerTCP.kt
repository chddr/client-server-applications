package net.tcp

import net.common.Server
import net.common.ServerThread
import java.net.ServerSocket

class ServerTCP(timeout: Int): Server {

    private var serverSocket = ServerSocket(net.SERVER_PORT).also {
        it.soTimeout = timeout //setting timeout to reset accept()
    }

    override fun waitForThread(): ServerThread {
        return ServerThreadTCP(serverSocket.accept())
    }

    override fun close() {
        serverSocket.close()
    }


}
