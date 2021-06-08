package eu.greenpassapp.greenpassbackend.beans.jwt.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import eu.greenpassapp.greenpassbackend.beans.jwt.JWTHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class JWTHelperImpl(
    @Value("\${jwt.secret}") private val secret: String
): JWTHelper {

    override fun getToken(linkClaim: String): String {
        try {
            val algorithm: Algorithm = Algorithm.HMAC256(secret)
            return JWT.create()
                .withClaim("link", linkClaim)
                .sign(algorithm)
        } catch (exception: JWTCreationException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Couldn't generate Token")
        }
    }

    override fun verifyTokenAndGetLink(token: String): String {
        try {
            val algorithm = Algorithm.HMAC256(secret)
            val verifier = JWT.require(algorithm)
                .build()
            val jwt = verifier.verify(token) //TODO string split Bearer and check if exist
            return jwt.getClaim("link").asString()
        } catch (exception: JWTVerificationException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong Token")
        }
    }
}