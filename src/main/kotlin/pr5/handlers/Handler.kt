package pr5.handlers

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import pr5.HttpServer


abstract class Handler(pattern: String, private val httpServer: HttpServer) : HttpHandler {

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
}