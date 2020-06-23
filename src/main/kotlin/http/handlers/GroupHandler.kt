package http.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import com.sun.net.httpserver.HttpExchange
import db.entities.Group
import db.entities.query_types.GroupQuery
import db.entities.query_types.Id
import http.HttpServer

class GroupHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handleGET(exchange: HttpExchange) {
        val (query) = objectMapper().readValue<GroupQuery>(exchange.requestBody)
        val groups = productDB().getGroupList(query)
        exchange.writeResponse(200, groups)
    }

    override fun handlePOST(exchange: HttpExchange) {
        val (id, name) = objectMapper().readValue<Group>(exchange.requestBody)
        productDB().updateGroup(id, name)
        exchange.writeResponse(204, null)
    }

    override fun handlePUT(exchange: HttpExchange) {
        val group = objectMapper().readTree(exchange.requestBody)
        val name = group["name"].textValue() ?: ""
        val id = productDB().addGroup(name)
        exchange.writeResponse(201, Id(id))
    }


}