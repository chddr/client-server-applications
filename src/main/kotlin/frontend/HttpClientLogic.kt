package frontend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import db.entities.Criterion
import db.entities.Product
import db.entities.UserCredentials
import http.responses.ErrorResponse
import http.responses.LoginResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients
import java.util.*

class HttpClientLogic(val uri: String) {

    companion object {
        private val mapper = jacksonObjectMapper()
    }

    fun sendLogin(login: String, password: String): LoginResponse {
        val json = mapper.writeValueAsBytes(
                UserCredentials(login, password)
        )

        val request = HttpPost("http://localhost:8080/login")
        request.entity = ByteArrayEntity(json)
        request.setHeader("Content-Type", "application/json")
        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                200 -> {
                    loginResponse = mapper.readValue<LoginResponse>(response.entity.content)
                    return@execute loginResponse!!
                }
                else -> {
                    val errMsg = mapper.readValue<ErrorResponse>(response.entity.content).message
                    val code = response.statusLine.statusCode
                    throw Exception("$code: $errMsg")
                }
            }
        }
    }

    fun loadProducts(page: Int, size: Int, criterion: Criterion): ArrayList<Product> {
        return arrayListOf(
                Product("wheat", 4.0),
                Product("banana", 3.0),
                Product("kale", 5.0),
                Product("buckwheat", 1.9),
                Product("rice", 7.4),
                Product("cabbage", 1.5),
                Product("tofu", 9.9),
                Product("quinoa", 0.5),
                Product("yeast", 6.0)
        )
    }

    private val client = HttpClients.createDefault()

    private var loginResponse: LoginResponse? = null


}