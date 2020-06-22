package http.authentication

import com.sun.net.httpserver.Authenticator
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpPrincipal
import http.HttpServer
import http.Utils.AUTHENTICATION_HEADER
import http.Utils.anonymousPrincipal
import http.authentication.JwtService.getUsernameFromToken


class MyAuthenticator(private val httpServer: HttpServer) : Authenticator() {

    override fun authenticate(httpExchange: HttpExchange): Result? {
        val token = httpExchange.requestHeaders.getFirst(AUTHENTICATION_HEADER)

        return if (token != null) try {
            val username = getUsernameFromToken(token)
            val user = httpServer.userDB.getUser(username)

            when {
                user != null -> Success(HttpPrincipal(username, user.role))
                else -> Retry(401)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Failure(403)
        }
        else Success(anonymousPrincipal)
    }
}