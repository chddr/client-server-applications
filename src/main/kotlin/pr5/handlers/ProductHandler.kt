package pr5.handlers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.Product
import db.entities.query_types.Id
import db.entities.query_types.ProductChange
import db.exceptions.*
import pr5.HttpServer
import pr5.responses.ErrorResponse

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
                else -> exchange.writeResponse(405, ErrorResponse("$method not allowed (only PUT and POST)"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            exchange.writeResponse(500, ErrorResponse("Internal error"))
        }
    }

    private fun handlePUT(exchange: HttpExchange) {
        try {
            val product = objectMapper().readValue<Product>(exchange.requestBody)
            val id = productDB().insertProduct(product)
            exchange.writeResponse(201, Id(id))
        } catch (e: DBException) {
            when (e) {
                is WrongPriceException -> exchange.writeResponse(409, ErrorResponse("Wrong price"))
                is WrongNameFormatException -> exchange.writeResponse(409, ErrorResponse("Wrong name"))
                is NameTakenException -> exchange.writeResponse(409, ErrorResponse("Such name is already used"))
            }
        } catch (e: JsonProcessingException) {
            exchange.writeResponse(400, ErrorResponse("JSON couldn't be parsed"))
        }

    }

    private fun handlePOST(exchange: HttpExchange) {// number below is ignored because it shouldn't be "set" - there should
        try {                                       // be separate methods responsible for addition/removal of certain amount
            val (id, name, price, _, groupId) = objectMapper().readValue<ProductChange>(exchange.requestBody)
            productDB().updateProduct(id, name, price, groupId)

            exchange.writeResponse(204, null)
        } catch (e: DBException) {
            when (e) {
                is NoSuchProductIdException -> exchange.writeResponse(404, ErrorResponse("No such product ID"))
                is NoSuchGroupIdException -> exchange.writeResponse(404, ErrorResponse("No such group ID"))
                is WrongPriceException -> exchange.writeResponse(409, ErrorResponse("Wrong price"))
                is WrongNameFormatException -> exchange.writeResponse(409, ErrorResponse("Wrong name"))
                is NameTakenException -> exchange.writeResponse(409, ErrorResponse("Such name is already used"))
            }
        } catch (e: JsonProcessingException) {
            exchange.writeResponse(400, ErrorResponse("JSON couldn't be parsed"))
        }
    }


}