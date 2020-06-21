package http.handlers

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import db.exceptions.*
import http.HttpServer
import http.responses.ErrorResponse
import java.net.URI


abstract class Handler(pattern: String, private val httpServer: HttpServer) : HttpHandler {

    private val urlPattern: Regex = Regex(pattern)

    protected fun userDB() = httpServer.userDB
    protected fun productDB() = httpServer.productDB
    protected fun objectMapper() = httpServer.objectMapper

    fun matches(uri: String) = urlPattern.matches(uri)

    fun HttpExchange.wrongMethod(method: String) = writeResponse(405, ErrorResponse("$method method not allowed"))

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

    protected fun HttpExchange.matchDbException(exception: DBException) {
        when (exception) {
            is NoSuchProductIdException -> writeResponse(404, ErrorResponse("No such product ID"))
            is NoSuchGroupIdException -> writeResponse(404, ErrorResponse("No such group ID"))
            is WrongPriceException -> writeResponse(409, ErrorResponse("Wrong price"))
            is WrongNameFormatException -> writeResponse(409, ErrorResponse("Wrong name"))
            is NameTakenException -> writeResponse(409, ErrorResponse("Such name is already used"))
        }
    }


    protected fun idFromUri(URI: URI): Int? {
        return try {
            URI.toString().split("/").last().toInt()
        } catch (e: Exception) {
            null
        }
    }

}