package frontend.http

import HttpGet
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import db.entities.*
import db.entities.query_types.*
import http.responses.ErrorResponse
import http.responses.LoginResponse
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
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

        val request = HttpGet("$url/login").apply {
            entity = ByteArrayEntity(json)
            setHeader("Content-Type", "application/json")
        }

        loginResponse = client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                200 -> return@execute mapper.readValue<LoginResponse>(response.entity.content)
                else -> throw handleException(response)

            }
        }
        return loginResponse!!
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
                else -> throw handleException(response)
            }
        }
    }

    fun loadProduct(id: Int): Product {
        val request = HttpGet("$url/api/product/$id").apply {
            setHeader("Authorization", loginResponse?.token)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                200 -> return@execute mapper.readValue<Product>(response.entity.content)
                else -> throw handleException(response)
            }
        }
    }

    fun loadGroups(query: String? = null): ArrayList<Group> {
        val json = mapper.writeValueAsBytes(GroupQuery(query))

        val request = HttpGet("$url/api/group").apply {
            setHeader("Content-Type", "application/json")
            setHeader("Authorization", loginResponse?.token)
            entity = ByteArrayEntity(json)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                200 -> return@execute mapper.readValue<ArrayList<Group>>(response.entity.content)
                else -> throw handleException(response)
            }
        }
    }

    fun deleteUser(id: Int): Any {
        val json = mapper.writeValueAsBytes(Id(id))

        val request = frontend.http.HttpDelete("$url/api/user").apply {
            setHeader("Content-Type", "application/json")
            setHeader("Authorization", loginResponse?.token)
            entity = ByteArrayEntity(json)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                204 -> Unit
                else -> throw handleException(response)
            }
        }
    }

    fun loadUsers(): ArrayList<User> {
        val request = HttpGet("$url/api/user").apply {
            setHeader("Authorization", loginResponse?.token)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                200 -> return@execute mapper.readValue<ArrayList<User>>(response.entity.content)
                else -> throw handleException(response)
            }
        }
    }

    fun modifyProduct(prodChange: ProductChange) {
        val json = mapper.writeValueAsBytes(prodChange)

        val request = HttpPost("$url/api/product").apply {
            setHeader("Content-Type", "application/json")
            setHeader("Authorization", loginResponse?.token)
            entity = ByteArrayEntity(json)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                204 -> Unit
                else -> throw handleException(response)
            }
        }
    }

    fun deleteProduct(id: Int) {
        val request = HttpDelete("$url/api/product/$id").apply {
            setHeader("Authorization", loginResponse?.token)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                204 -> Unit
                else -> throw handleException(response)
            }
        }
    }

    fun createProduct(product: Product): Int {
        val json = mapper.writeValueAsBytes(product)

        val request = HttpPut("$url/api/product").apply {
            setHeader("Content-Type", "application/json")
            setHeader("Authorization", loginResponse?.token)
            entity = ByteArrayEntity(json)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                201 -> return@execute mapper.readValue<Id>(response.entity.content).id
                else -> throw handleException(response)
            }
        }
    }

    fun createGroup(name: String, description: String? = null): Int {
        val json = mapper.writeValueAsBytes(CreateGroup(name, description))

        val request = HttpPut("$url/api/group").apply {
            setHeader("Content-Type", "application/json")
            setHeader("Authorization", loginResponse?.token)
            entity = ByteArrayEntity(json)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                201 -> return@execute mapper.readValue<Id>(response.entity.content).id
                else -> throw handleException(response)
            }
        }
    }


    fun loadGroup(id: Int): Group {
        val request = HttpGet("$url/api/group/$id").apply {
            setHeader("Authorization", loginResponse?.token)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                200 -> return@execute mapper.readValue<Group>(response.entity.content)
                else -> throw handleException(response)
            }
        }
    }

    fun modifyGroup(group: Group) {
        val json = mapper.writeValueAsBytes(group)

        val request = HttpPost("$url/api/group").apply {
            setHeader("Content-Type", "application/json")
            setHeader("Authorization", loginResponse?.token)
            entity = ByteArrayEntity(json)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                204 -> Unit
                else -> throw handleException(response)
            }
        }
    }

    fun deleteGroup(id: Int) {
        val request = HttpDelete("$url/api/group/$id").apply {
            setHeader("Authorization", loginResponse?.token)
        }

        return client.execute(request) { response ->
            when (response.statusLine.statusCode) {
                204 -> Unit
                else -> throw handleException(response)
            }
        }
    }

    private fun handleException(response: HttpResponse): Throwable {
        if (response.statusLine.statusCode == 403)
            return UnauthorizedException("Please log in first.")

        val errMsg = try {
            mapper.readValue<ErrorResponse>(response.entity.content).message
        } catch (e: java.lang.Exception) {
            "Server-side or Client-side error. Please contact the dev."
        }
        val code = response.statusLine.statusCode
        return Exception("$code: $errMsg")
    }


    private val client = HttpClients.createDefault()
    private var loginResponse: LoginResponse? = null

    fun isLoggedIn() = loginResponse != null
    fun logout() {
        loginResponse = null
    }


}