package pr5.authentication

import db.entities.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.security.Key


object JwtService {
    private val SECRET_KEY: Key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)

    fun generateToken(user: User): String {
        return Jwts.builder()
                .setSubject(user.login)
                .signWith(SECRET_KEY)
                .claim("role", user.role)
                .compact()
    }

    fun getUsernameFromToken(jwt: String?): String {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject()
    }
}