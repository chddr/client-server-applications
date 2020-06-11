package pr5

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import db.DaoProduct
import db.DaoUser
import org.intellij.lang.annotations.Language
import pr5.authentication.MyAuthenticator
import pr5.handlers.LoginHandler
import pr5.handlers.ProductIdHandler
import java.io.IOException
import java.net.InetSocketAddress

class HttpServer(port: Int) {

    companion object {
        val OBJECT_MAPPER = jacksonObjectMapper()
        val daoUser = DaoUser("file.db")
        val daoProduct = DaoProduct("file.db")
    }

    private val server = HttpServer.create(InetSocketAddress(port), 0)

    /** Context handler with associated roots*/
    @Language("RegExp")
    private val contextHandlers = listOf(
            ProductIdHandler("^/api/product/(\\d+)$"),
//            ProductHandler("^/api/product/available$"),
            LoginHandler("^/login$")
    )


    init {
        val context = server.createContext("/", mainContext())
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
            exchange.responseBody.close()
        } catch (e: IOException) {

        }

    }

    public fun stop() = server.stop(1)

}

fun main() {
    HttpServer(8080)
}