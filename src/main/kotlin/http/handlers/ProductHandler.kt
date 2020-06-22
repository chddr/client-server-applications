package http.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.Product
import db.entities.query_types.Id
import db.entities.query_types.PagesAndCriterion
import db.entities.query_types.ProductChange
import http.HttpServer

class ProductHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handleGET(exchange: HttpExchange) {
        val (page, size, criterion) = objectMapper().readValue<PagesAndCriterion>(exchange.requestBody)
        val products = productDB().getProductList(page, size, criterion)

        exchange.writeResponse(200, products)
    }

    override fun handlePUT(exchange: HttpExchange) {
        val product = objectMapper().readValue<Product>(exchange.requestBody)
        val id = productDB().insertProduct(product)
        exchange.writeResponse(201, Id(id))
    }

    // number below is ignored because it shouldn't be "set" - there should
    // be separate methods responsible for addition/removal of certain amount
    override fun handlePOST(exchange: HttpExchange) {
        val (id, name, price, _, groupId) = objectMapper().readValue<ProductChange>(exchange.requestBody)
        productDB().updateProduct(id, name, price, groupId)

        exchange.writeResponse(204, null)
    }

}