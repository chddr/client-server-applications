package http.responses

data class LoginResponse(
        val token: String,
        val login: String,
        val role: String?
)