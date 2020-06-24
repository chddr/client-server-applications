package http.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.query_types.AddRemoveProduct
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

    override fun handlePOST(exchange: HttpExchange) {
        val (amount, operation) = objectMapper().readValue<AddRemoveProduct>(exchange.requestBody)
        val id = idFromUri(exchange.requestURI)

        when (operation) {
            "add" -> productDB().addItems(id, amount)
            "remove" -> productDB().removeItems(id, amount)
            else -> return
        }
        exchange.writeResponse(200, productDB().getProduct(id).number)
    }
}