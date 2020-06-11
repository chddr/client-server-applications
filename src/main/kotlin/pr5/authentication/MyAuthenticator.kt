package pr5.authentication

import com.sun.net.httpserver.Authenticator
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpPrincipal
import pr5.HttpServer
import pr5.Utils.AUTHENTICATION_HEADER
import pr5.Utils.anonymousPrincipal
import pr5.authentication.JwtService.getUsernameFromToken


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