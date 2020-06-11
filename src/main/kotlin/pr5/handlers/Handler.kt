package pr5.handlers

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import pr5.HttpServer


abstract class Handler(pattern: String) : HttpHandler {

    private val urlPattern: Regex = Regex(pattern)

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

//    private fun parameters(uri: String): List<String> {
//        val matcher = urlPattern.matcher(uri)
//
//        val list = arrayListOf<String>()
//        for (i in 0 until matcher.groupCount()) {
//            list.add(matcher.group(i))
//        }
//
//        return list
//    }

    fun matches(uri: String) = urlPattern.matches(uri)

//    fun HttpExchange.getParameter(n: Int): String = parameters(requestURI.toString())[n]

    fun HttpExchange.writeResponse(code: Int, response: Any) {
        val bytes = HttpServer.OBJECT_MAPPER.writeValueAsBytes(response)
        sendResponseHeaders(code, bytes.size.toLong())
        responseBody.write(bytes)
        responseBody.close()
    }
}