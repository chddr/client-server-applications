package frontend

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import db.entities.UserCredentials
import http.responses.ErrorResponse
import http.responses.LoginResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients

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

    private val client = HttpClients.createDefault()

    private var loginResponse: LoginResponse? = null


}