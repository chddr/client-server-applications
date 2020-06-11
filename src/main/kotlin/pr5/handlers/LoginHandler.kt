package pr5.handlers

import com.sun.net.httpserver.HttpExchange
import db.entities.UserCredentials
import org.apache.commons.codec.digest.DigestUtils.md5Hex
import pr5.HttpServer
import pr5.authentication.JwtService
import pr5.responses.ErrorResponse
import pr5.responses.LoginResponse

class LoginHandler(urlPattern: String, httpServer: HttpServer) : Handler(urlPattern, httpServer) {

    override fun handle(exchange: HttpExchange) {
        exchange.requestBody.use {
            val userCreds = try {
                objectMapper().readValue(it, UserCredentials::class.java)
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
                    exchange.writeResponse(401, ErrorResponse("invalid password"))
                }
            } else {
                exchange.writeResponse(401, ErrorResponse("unknown user"))
            }

        }
    }
}