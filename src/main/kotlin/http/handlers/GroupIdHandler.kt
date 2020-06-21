package http.handlers

import com.sun.net.httpserver.HttpExchange
import db.exceptions.DBException
import http.HttpServer
import http.responses.ErrorResponse


class GroupIdHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handle(exchange: HttpExchange) {
        try {
            if (exchange.principal.realm != "admin") {
                exchange.writeResponse(403, ErrorResponse("No permission"))
                return
            }

            val groupId = idFromUri(exchange.requestURI)

            if (groupId == null) {
                exchange.writeResponse(400, ErrorResponse("Id too long"))
                return
            }
            when (val method = exchange.requestMethod) {
                "GET" -> handleGET(exchange, groupId)
                "DELETE" -> handleDELETE(exchange, groupId)
                else -> exchange.wrongMethod(method)
            }

        } catch (e: DBException) {
            exchange.matchDbException(e)
        } catch (e: Exception) {
            e.printStackTrace()
            exchange.writeResponse(500, ErrorResponse("Internal error"))
        }
    }

    private fun handleDELETE(exchange: HttpExchange, groupId: Int) {
        productDB().deleteGroup(groupId)
        exchange.writeResponse(204, null)
    }

    private fun handleGET(exchange: HttpExchange, groupId: Int) {
        val group = productDB().getGroup(groupId)
        exchange.writeResponse(200, group)
    }

}

