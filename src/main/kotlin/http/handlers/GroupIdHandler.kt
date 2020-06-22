package http.handlers

import com.sun.net.httpserver.HttpExchange
import http.HttpServer


class GroupIdHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handleDELETE(exchange: HttpExchange) {
        productDB().deleteGroup(idFromUri(exchange.requestURI))
        exchange.writeResponse(204, null)
    }

    override fun handleGET(exchange: HttpExchange) {
        val group = productDB().getGroup(idFromUri(exchange.requestURI))
        exchange.writeResponse(200, group)
    }

}

