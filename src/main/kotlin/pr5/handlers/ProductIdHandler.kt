package pr5.handlers

import com.sun.net.httpserver.HttpExchange
import db.DaoProduct.NoSuchProductIdException
import pr5.HttpServer
import pr5.responses.ErrorResponse


class ProductIdHandler(urlPattern: String) : Handler(urlPattern) {

    override fun handle(exchange: HttpExchange) {
        try {
            exchange.responseHeaders
                    .add("Content-Type", "application/json")

            if (exchange.principal.realm != "admin") {
                exchange.writeResponse(403, ErrorResponse("No permission"))
                return
            }

            val productId = try {
                exchange.getParameter(0).toInt()
            } catch (e: NoSuchProductIdException) {
                exchange.writeResponse(400, ErrorResponse("Id too long"))
                return
            }

            try {
                val product = HttpServer.daoProduct.getProduct(productId)
                exchange.writeResponse(200, product)
            } catch (e: NoSuchProductIdException) {
                exchange.writeResponse(404, ErrorResponse("No such product"))
            }
        } catch (e: Exception) {
            exchange.writeResponse(500, ErrorResponse("Internal error"))
        }
    }
}