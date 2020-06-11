package pr5

import com.sun.net.httpserver.HttpPrincipal

object Utils {
    const val AUTHENTICATION_HEADER = "Authorization"
    val anonymousPrincipal = HttpPrincipal("anonymous", "anonymous")

}