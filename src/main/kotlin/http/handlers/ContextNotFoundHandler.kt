package http.handlers

import com.sun.net.httpserver.HttpExchange
import http.HttpServer
import java.io.IOException

class ContextNotFoundHandler(server: HttpServer) : Handler("", server, allAccess) {

    override fun defaultHandle(exchange: HttpExchange) {
        try {
            exchange.sendResponseHeaders(404, 0)
            exchange.responseBody.close()
        } catch (e: IOException) {

        }
    }

}