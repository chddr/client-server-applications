package http.handlers

import com.sun.net.httpserver.HttpExchange
import db.entities.query_types.Price
import http.HttpServer

class StatsHandler(match: String, httpServer: HttpServer) : Handler(match, httpServer) {

    override fun handleGET(exchange: HttpExchange) {
        val totalWorth = productDB().totalSum()
        exchange.writeResponse(200, Price(totalWorth))
    }
}
