package http.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.query_types.Id
import db.entities.query_types.Price
import http.HttpServer

class GroupStatsHandler(match: String, httpServer: HttpServer) : Handler(match, httpServer) {

    override fun handleGET(exchange: HttpExchange) {
        val id = objectMapper().readValue<Id>(exchange.requestBody).id

        val totalWorth = productDB().totalSumByGroup(id)
        exchange.writeResponse(200, Price(totalWorth))
    }
}
