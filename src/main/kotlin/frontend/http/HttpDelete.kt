package frontend.http

import org.apache.http.client.methods.HttpPost

class HttpDelete(url: String?) : HttpPost(url) {
    override fun getMethod() = "DELETE"
}