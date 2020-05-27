package tcp

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

const val SERVER_PORT = 2222;

fun main() {
    ServerSocket(SERVER_PORT).use { serverSocket ->
        while (true) {
            runClient(serverSocket.accept())
        }
    }

}

fun runClient(socket: Socket) {

    thread(start = true) {
        try {
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            val inputMessage = ByteArray(100)

            inputStream.read(inputMessage)
            println("Message from client: ${String(inputMessage)}")

            outputStream.write("Server is OK".toByteArray())

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            socket.close()
        }
    }
}
