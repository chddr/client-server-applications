package http.handlers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.Product
import db.entities.query_types.Id
import db.entities.query_types.PagesAndCriterion
import db.entities.query_types.ProductChange
import db.exceptions.DBException
import http.HttpServer
import http.responses.ErrorResponse

class ProductHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handle(exchange: HttpExchange) {
        try {
            if (exchange.principal.realm != "admin") {
                exchange.writeResponse(403, ErrorResponse("No permission"))
                return
            }

            when (val method = exchange.requestMethod) {
                "PUT" -> handlePUT(exchange)
                "POST" -> handlePOST(exchange)
                "GET" -> handleGET(exchange)
                else -> exchange.wrongMethod(method)
            }

        } catch (e: DBException) {
            exchange.matchDbException(e)
        } catch (e: JsonProcessingException) {
            exchange.writeResponse(400, ErrorResponse("JSON couldn't be parsed"))
        } catch (e: Exception) {
            e.printStackTrace()
            exchange.writeResponse(500, ErrorResponse("Internal error"))
        }
    }

    private fun handleGET(exchange: HttpExchange) {
        val (page, size, criterion) = objectMapper().readValue<PagesAndCriterion>(exchange.requestBody)
        val products = productDB().getProductList(page, size, criterion)

        exchange.writeResponse(200, products)

    }

    private fun handlePUT(exchange: HttpExchange) {
        val product = objectMapper().readValue<Product>(exchange.requestBody)
        val id = productDB().insertProduct(product)
        exchange.writeResponse(201, Id(id))
    }

    // number below is ignored because it shouldn't be "set" - there should
    // be separate methods responsible for addition/removal of certain amount
    private fun handlePOST(exchange: HttpExchange) {
        val (id, name, price, _, groupId) = objectMapper().readValue<ProductChange>(exchange.requestBody)
        productDB().updateProduct(id, name, price, groupId)

        exchange.writeResponse(204, null)
    }

}