package pr5

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import db.DaoProduct
import db.DaoUser
import org.intellij.lang.annotations.Language
import pr5.authentication.MyAuthenticator
import pr5.handlers.LoginHandler
import pr5.handlers.ProductHandler
import pr5.handlers.ProductIdHandler
import java.io.Closeable
import java.io.IOException
import java.net.InetSocketAddress

class HttpServer(port: Int = 8080, dbName: String = "file.db"): Closeable {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            HttpServer()
        }
    }

    val objectMapper = jacksonObjectMapper()
    val userDB = DaoUser(dbName)
    val productDB = DaoProduct(dbName)

    private val server = HttpServer.create(InetSocketAddress(port), 0)

    /** Context handler with associated roots*/
    @Language("RegExp")
    private val contextHandlers = listOf(
            ProductIdHandler("^/api/product/(\\d+)$", this),
            ProductHandler("^/api/product$", this),
            LoginHandler("^/login$", this)
    )


    init {
        val context = server.createContext("/", mainContext())
        context.authenticator = MyAuthenticator(this)

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

    override fun close() = server.stop(0)

}