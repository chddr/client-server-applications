package frontend

import HttpGet
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import db.entities.Criterion
import db.entities.Product
import db.entities.UserCredentials
import db.entities.query_types.PagesAndCriterion
import frontend.http.UnauthorizedException
import http.responses.ErrorResponse
import http.responses.LoginResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients

class HttpClientLogic(private val url: String) {

    companion object {
        private val mapper = jacksonObjectMapper()
    }

    fun sendLogin(login: String, password: String): LoginResponse {
        val json = mapper.writeValueAsBytes(
                UserCredentials(login, password)
        )

        val request = HttpPost("$url/login").apply {
            entity = ByteArrayEntity(json)
            setHeader("Content-Type", "application/json")
        }

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
        val json = mapper.writeValueAsBytes(
                PagesAndCriterion(page, size, criterion)
        )

        val request = HttpGet("$url/api/product").apply {
            entity = ByteArrayEntity(json)
            setHeader("Content-Type", "application/json")
            setHeader("Authorization", loginResponse?.token)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                200 -> return@execute mapper.readValue<ArrayList<Product>>(response.entity.content)
                403 -> throw UnauthorizedException("Please log in first.")
                else -> {
                    val errMsg = try {
                        mapper.readValue<ErrorResponse>(response.entity.content).message
                    } catch (e: java.lang.Exception) {
                        "Server-side or Client-side error. Please contact the dev."
                    }
                    val code = response.statusLine.statusCode
                    throw Exception("$code: $errMsg")
                }
            }
        }
    }


    private val client = HttpClients.createDefault()
    private var loginResponse: LoginResponse? = null

    fun isLoggedIn() = loginResponse != null


}