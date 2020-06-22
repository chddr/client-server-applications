import org.apache.http.client.methods.HttpPost

class HttpGet(url: String?) : HttpPost(url) {
    override fun getMethod() = "GET"
}