package http.handlers

import com.fasterxml.jackson.core.JsonProcessingException
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import db.exceptions.*
import http.HttpServer
import http.Utils.defaultAccess
import http.responses.ErrorResponse
import java.net.URI


abstract class Handler(pattern: String, private val httpServer: HttpServer, private val privilegeRequired: Collection<String> = defaultAccess) : HttpHandler {

    private val urlPattern: Regex = Regex(pattern)

    protected fun userDB() = httpServer.userDB
    protected fun productDB() = httpServer.productDB
    protected fun objectMapper() = httpServer.objectMapper

    fun matches(uri: String) = urlPattern.matches(uri)

    fun HttpExchange.writeResponse(code: Int, response: Any?) {
        responseHeaders
                .add("Content-Type", "application/json")

        if (response != null) {
            val bytes = objectMapper().writeValueAsBytes(response)
            sendResponseHeaders(code, bytes.size.toLong())
            responseBody.write(bytes)
        } else
            sendResponseHeaders(code, 0)
        responseBody.close()
    }

    final override fun handle(exchange: HttpExchange) {
        try {
            if (exchange.principal.realm !in privilegeRequired) {
                exchange.writeResponse(403, ErrorResponse("No permission"))
                return
            }
            when (exchange.requestMethod) {
                "GET" -> handleGET(exchange)
                "PUT" -> handlePUT(exchange)
                "POST" -> handlePOST(exchange)
                "DELETE" -> handleDELETE(exchange)
                else -> defaultHandle(exchange)
            }

        } catch (e: DBException) {
            exchange.matchDbException(e)
        } catch (e: JsonProcessingException) {
            exchange.writeResponse(400, ErrorResponse("JSON couldn't be parsed"))
        } catch (e: WrongIdException) {
            exchange.writeResponse(400, ErrorResponse("Id too long or something else wrong with it"))
        } catch (e: Exception) {
            e.printStackTrace()
            exchange.writeResponse(500, ErrorResponse("Internal error"))
        }
    }

    protected open fun defaultHandle(exchange: HttpExchange) = exchange.wrongMethod()
    protected open fun handleDELETE(exchange: HttpExchange) = defaultHandle(exchange)
    protected open fun handlePOST(exchange: HttpExchange) = defaultHandle(exchange)
    protected open fun handlePUT(exchange: HttpExchange) = defaultHandle(exchange)
    protected open fun handleGET(exchange: HttpExchange) = defaultHandle(exchange)

    private fun HttpExchange.wrongMethod() {
        writeResponse(405, ErrorResponse("$requestMethod method not allowed"))
    }

    private fun HttpExchange.matchDbException(exception: DBException) {
        when (exception) {
            is NoSuchProductIdException -> writeResponse(404, ErrorResponse("No such product ID"))
            is NoSuchGroupIdException -> writeResponse(404, ErrorResponse("No such group ID"))
            is WrongPriceException -> writeResponse(409, ErrorResponse("Wrong price"))
            is WrongNameFormatException -> writeResponse(409, ErrorResponse("Wrong name"))
            is NameTakenException -> writeResponse(409, ErrorResponse("Such name is already used"))
        }
    }


    protected fun idFromUri(URI: URI): Int {
        return try {
            URI.toString().split("/").last().toInt()
        } catch (e: Exception) {
            throw WrongIdException()
        }
    }
}