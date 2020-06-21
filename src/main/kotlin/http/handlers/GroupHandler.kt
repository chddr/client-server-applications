package http.handlers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.Group
import db.entities.query_types.GroupQuery
import db.entities.query_types.Id
import db.exceptions.DBException
import http.HttpServer
import http.responses.ErrorResponse

class GroupHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handle(exchange: HttpExchange) {
        try {
            if (exchange.principal.realm != "admin") {
                exchange.writeResponse(403, ErrorResponse("No permission"))
                return
            }

            when (val method = exchange.requestMethod) {
                "GET" -> handleGET(exchange)
                "PUT" -> handlePUT(exchange)
                "POST" -> handlePOST(exchange)
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
        val (page, size, query) = objectMapper().readValue<GroupQuery>(exchange.requestBody)
        val groups = productDB().getGroupList(page, size, query)
        exchange.writeResponse(200, groups)
    }

    private fun handlePOST(exchange: HttpExchange) {
        val (id, name) = objectMapper().readValue<Group>(exchange.requestBody)
        productDB().changeGroupName(id, name)
        exchange.writeResponse(204, null)
    }

    private fun handlePUT(exchange: HttpExchange) {
        val group = objectMapper().readTree(exchange.requestBody)
        val name = group["name"].textValue() ?: ""
        val id = productDB().addGroup(name)
        exchange.writeResponse(201, Id(id))
    }


}