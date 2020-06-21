package http.handlers

import com.sun.net.httpserver.HttpExchange
import http.HttpServer


class ProductIdHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handleDELETE(exchange: HttpExchange) {
        productDB().deleteProduct(idFromUri(exchange.requestURI))
        exchange.writeResponse(204, null)
    }


    override fun handleGET(exchange: HttpExchange) {
        val product = productDB().getProduct(idFromUri(exchange.requestURI))
        exchange.writeResponse(200, product)
    }
}