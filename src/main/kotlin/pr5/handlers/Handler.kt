package pr5.handlers

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import pr5.HttpServer
import java.util.regex.Pattern


abstract class Handler(pattern: String) : HttpHandler {

    private val urlPattern: Pattern = Pattern.compile(pattern.replace("/", "\\/"))

    //companion object {
    //    val swaps = listOf("/" to "\\/",
    //            "{product_id}" to "(\\d+)")}
    //init {
    //    var pattern = pattern
    //    for ((from, to) in swaps)
    //        pattern.replace(from, to)
    //    pattern = "^$pattern$"
    //    urlPattern = Pattern.compile(pattern);
    //}

    fun parameters(uri: String): List<String> {
        val matcher = urlPattern.matcher(uri)

        val list = arrayListOf<String>()
        for (i in 0 until matcher.groupCount()) {
            list.add(matcher.group(i))
        }

        return list
    }

    fun matches(uri: String) = urlPattern.matcher(uri).matches()

    fun HttpExchange.getParameter(n: Int): String = parameters(String(requestBody.readBytes()))[n]

    fun HttpExchange.writeResponse(code: Int, response: Any) {
        val bytes = HttpServer.OBJECT_MAPPER.writeValueAsBytes(response)
        sendResponseHeaders(code, bytes.size.toLong())
        responseBody.write(bytes)
    }

}