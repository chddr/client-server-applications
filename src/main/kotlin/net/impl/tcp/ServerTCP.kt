package net.impl.tcp

import net.interfaces.Server
import net.interfaces.ServerThread
import java.net.ServerSocket

class ServerTCP: Server {

    private var serverSocket = ServerSocket(net.SERVER_PORT).also {
        it.soTimeout = net.SERVER_TIMEOUT //setting timeout to reset accept()
    }

    override fun waitForThread(): ServerThread {
        return ServerThreadTCP(serverSocket.accept())
    }

    override fun stop() {
        serverSocket.close()
    }


}
