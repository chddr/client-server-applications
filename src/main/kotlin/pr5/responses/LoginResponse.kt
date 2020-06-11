package pr5.responses

data class LoginResponse(
        private val token: String,
        private val login: String,
        private val role: String?
)