package http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import db.DaoProduct
import db.DaoUser
import http.authentication.MyAuthenticator
import http.handlers.*
import net.PROCESSOR_THREADS
import net.common.utils.ProcessorUtils.waitForStop
import org.intellij.lang.annotations.Language
import java.io.Closeable
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class HttpServer(port: Int = 8080, dbName: String = "file.db") : Closeable {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            HttpServer()
        }
    }

    private val processor = Executors.newFixedThreadPool(PROCESSOR_THREADS)

    val objectMapper = jacksonObjectMapper()
    val userDB = DaoUser(dbName)
    val productDB = DaoProduct(dbName)

    private val server = HttpServer.create(InetSocketAddress(port), 0)

    /** Context handler with associated roots*/
    @Language("RegExp")
    private val contextHandlers = listOf(
            ProductIdHandler("^/api/product/(\\d+)$", this),
            ProductHandler("^/api/product$", this),
            LoginHandler("^/login$", this),
            GroupIdHandler("^/api/group/(\\d+)$", this),
            GroupHandler("^/api/group$", this),
            UserHandler("^/api/user$", this),
            StatsHandler("^/api/stats$", this)
    )
    private val defaultHandler = ContextNotFoundHandler(this)

    init {
        val context = server.createContext("/", mainContext())
        context.authenticator = MyAuthenticator(this)

        server.start()
    }

    private fun mainContext() = { exchange: HttpExchange ->
        val uri = exchange.requestURI.toString()
        val handler = contextHandlers.firstOrNull { it.matches(uri) } ?: defaultHandler

        processor.submit { handler.handle(exchange) }
        Unit
    }


    override fun close() {
        server.stop(0)
        processor.waitForStop("")
    }

}