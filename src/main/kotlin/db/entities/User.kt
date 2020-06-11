package db.entities

data class User(
        val login: String,
        val password: String,
        val role: String,
        val id: Int? = null
)