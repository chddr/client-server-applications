package pr5.responses

data class LoginResponse(
        val token: String,
        val login: String,
        val role: String?
)