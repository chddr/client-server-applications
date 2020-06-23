package http.handlers

import com.sun.net.httpserver.HttpExchange
import db.entities.UserCredentials
import http.HttpServer
import http.authentication.JwtService
import http.responses.ErrorResponse
import http.responses.LoginResponse
import org.apache.commons.codec.digest.DigestUtils.md5Hex

class LoginHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer, privilegeRequired = listOf("user", "admin", "anonymous")) {

    override fun handleGET(exchange: HttpExchange) {

        val userCreds = try {
            objectMapper().readValue(exchange.requestBody, UserCredentials::class.java)
        } catch (e: Exception) {
            exchange.writeResponse(400, ErrorResponse("Bad request"))
            return
        }

        val user = userDB().getUser(userCreds.login)

        exchange.responseHeaders.add("Content-Type", "application/json")

        if (user != null) {
            if (user.password == md5Hex(userCreds.password)) {
                val loginResponse = LoginResponse(JwtService.generateToken(user), user.login, user.role)
                exchange.writeResponse(200, loginResponse)
            } else {
                exchange.writeResponse(401, ErrorResponse("Invalid password"))
            }
        } else {
            exchange.writeResponse(401, ErrorResponse("Invalid user"))
        }


    }
}