package http.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.User
import db.entities.query_types.Id
import http.HttpServer

class UserHandler(match: String, httpServer: HttpServer) : Handler(match, httpServer, adminAccess) {

    override fun handleGET(exchange: HttpExchange) {
        val users = userDB().getUsers()
        exchange.writeResponse(200, users)
    }

    override fun handlePUT(exchange: HttpExchange) {
        val user = objectMapper().readValue<User>(exchange.requestBody)
        val id = userDB().insert(user)

        exchange.writeResponse(201, Id(id))
    }

    override fun handleDELETE(exchange: HttpExchange) {
        val id = objectMapper().readValue<Id>(exchange.requestBody).id
        userDB().delete(id)

        exchange.writeResponse(204, null)
    }

}
