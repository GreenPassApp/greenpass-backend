package eu.greenpassapp.greenpassbackend.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import eu.greenpassapp.greenpassbackend.dto.RawCertificateDto
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate


@RestController
@RequestMapping(
    value = ["/api"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class UserController(
    private val userLogic: UserLogic,
    @Value("\${jwt.secret}") private val secret: String
) {

    @GetMapping(value = ["/test"])
    fun test(): String {
        try {
            val algorithm: Algorithm = Algorithm.HMAC256(secret)
            val test = JWT.create()
                .withClaim("link", "google.de")
                .sign(algorithm)
        } catch (exception: JWTCreationException) {
            //Invalid Signing configuration / Couldn't convert Claims.
        }

        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsaW5rIjoiZ29vZ2xlLmRlIn0.wAYra-Zb6XNmbthgn4ZAUa0kAN26jqwnqL7T3wFvw45"
        try {
            val algorithm = Algorithm.HMAC256(secret)
            val verifier = JWT.require(algorithm)
                //.withClaim("link", "google.de")
                .build()
            val jwt = verifier.verify(token)
            return jwt.getClaim("link").asString()
        } catch (exception: JWTVerificationException) {
            //Invalid signature/claims
        }

        return "TEST"
    }

    @PostMapping(value = ["/insert"])
    fun insert(@RequestBody certificate: RawCertificateDto): ResponseEntity<Pair<String, String>> { //TODO: refactor pair with dto
        //TODO CHECK certificate

        val user = User("Jakob", "Stadlhuber", LocalDate.now(), "testType")

        //TODO check if user.link already exists

        val newUser = userLogic.insertOrUpdate(user)
        try {
            val algorithm: Algorithm = Algorithm.HMAC256(secret)
            val newToken = JWT.create()
                .withClaim("link", newUser.link)
                .sign(algorithm)
            return ResponseEntity<Pair<String, String>>(Pair(newUser.link, newToken), HttpStatus.OK) //insert new user
        } catch (exception: JWTCreationException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Couldn't generate Token")
        }

    }

    @PostMapping(value = ["/update"])
    fun update(@RequestBody certificate: RawCertificateDto, @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<Any> {

        //TODO CHECK certificate

        val user = User("Jakob", "Stadlhuber", LocalDate.now(), "testType")

        //TODO refactor jwt into standalone bean
        val token = headers["Authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")

        try {
            val algorithm = Algorithm.HMAC256(secret)
            val verifier = JWT.require(algorithm)
                .build()
            val jwt = verifier.verify(token) //TODO string split Bearer and check if exist
            val link =  jwt.getClaim("link").asString()
            user.link = link
            userLogic.insertOrUpdate(user) //update existing user
            return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
        } catch (exception: JWTVerificationException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Wrong Token")
        }
    }
}