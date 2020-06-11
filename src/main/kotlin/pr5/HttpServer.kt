package pr5

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import db.DaoProduct
import db.DaoUser
import pr5.authentication.MyAuthenticator
import pr5.handlers.LoginHandler
import pr5.handlers.ProductIdHandler
import java.io.IOException
import java.net.InetSocketAddress

class HttpServer(port: Int) {

    companion object {
        val OBJECT_MAPPER = ObjectMapper()
        val daoUser = DaoUser("file.db:memory")
        val daoProduct = DaoProduct("file.db:memory")
    }

    private val server = HttpServer.create(InetSocketAddress(port), 0)

    /** Context handler with associated roots*/
    private val contextHandlers = listOf(
            ProductIdHandler("^/api/product/(\\d+)$"),
//            ProductHandler("^/api/product/available$"),
            LoginHandler("/login")
    )


    init {

        val context = server.createContext("/") { print("i;m here")}
        context.authenticator = MyAuthenticator()

        server.start()
    }

    private fun mainContext() = { exchange: HttpExchange ->
        println("main")
        val uri = exchange.requestURI.toString()

        contextHandlers
                .firstOrNull { it.matches(uri) }
                ?.handle(exchange)
                ?: contextNotFound(exchange)
    }

    private fun contextNotFound(exchange: HttpExchange) {
        try {
            exchange.sendResponseHeaders(404, 0)
            println("bs")
        } catch (e: IOException) {

        }

    }

    public fun stop() {
        server.stop(1)
    }

}

fun main() {
    HttpServer(8080)
}