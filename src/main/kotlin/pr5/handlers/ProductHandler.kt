package pr5.handlers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.Product
import db.entities.query_types.Id
import db.exceptions.DBException
import db.exceptions.NameTakenException
import db.exceptions.WrongNameFormatException
import db.exceptions.WrongPriceException
import pr5.HttpServer.Companion.OBJECT_MAPPER
import pr5.HttpServer.Companion.daoProduct
import pr5.responses.ErrorResponse

class ProductHandler(urlPattern: String) : Handler(urlPattern) {

    override fun handle(exchange: HttpExchange) {
        try {
            if (exchange.principal.realm != "admin") {
                exchange.writeResponse(403, ErrorResponse("No permission"))
                return
            }

            when (exchange.requestMethod) {
                "PUT" -> handlePUT(exchange)
                "POST" -> handlePOST(exchange)
                else -> exchange.writeResponse(405, ErrorResponse("Only PUT and POST are allowed"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            exchange.writeResponse(500, ErrorResponse("Internal error"))
        }
    }

    private fun handlePUT(exchange: HttpExchange) {
        try {
            val product = OBJECT_MAPPER.readValue<Product>(exchange.requestBody)
            val id = daoProduct.insertProduct(product)
            exchange.writeResponse(201, Id(id))
        } catch (e: DBException) {
            when (e) {
                is WrongPriceException -> exchange.writeResponse(409, ErrorResponse("Wrong price"))
                is WrongNameFormatException -> exchange.writeResponse(409, ErrorResponse("Wrong name"))
                is NameTakenException ->exchange.writeResponse(409, ErrorResponse("Such name is already used"))
            }
        } catch (e: JsonProcessingException) {
            exchange.writeResponse(400, ErrorResponse("JSON couldn't be parsed"))
        }

    }

    private fun handlePOST(exchange: HttpExchange) {

    }



}