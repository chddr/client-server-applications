package http.handlers

import com.sun.net.httpserver.HttpExchange
import db.exceptions.DBException
import http.HttpServer
import http.responses.ErrorResponse


class ProductIdHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handle(exchange: HttpExchange) {
        try {
            //checking user privilege
            if (exchange.principal.realm != "admin") {
                exchange.writeResponse(403, ErrorResponse("No permission"))
                return
            }
            val prodId = idFromUri(exchange.requestURI)

            if (prodId == null) {
                exchange.writeResponse(400, ErrorResponse("Id too long"))
                return
            }

            when (val method = exchange.requestMethod) {
                "GET" -> handleGET(exchange, prodId)
                "DELETE" -> handleDELETE(exchange, prodId)
                else -> exchange.wrongMethod(method)
            }

        } catch (e: DBException) {
            exchange.matchDbException(e)
        } catch (e: Exception) {
            e.printStackTrace()
            exchange.writeResponse(500, ErrorResponse("Internal error"))
        }
    }


    private fun handleDELETE(exchange: HttpExchange, prodId: Int) {
        productDB().deleteProduct(prodId)
        exchange.writeResponse(204, null)
    }


    private fun handleGET(exchange: HttpExchange, productId: Int) {
        val product = productDB().getProduct(productId)
        exchange.writeResponse(200, product)
    }
}