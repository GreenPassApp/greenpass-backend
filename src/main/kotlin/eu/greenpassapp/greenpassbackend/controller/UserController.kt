package eu.greenpassapp.greenpassbackend.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import eu.greenpassapp.greenpassbackend.beans.jwt.JWTHelper
import eu.greenpassapp.greenpassbackend.dto.RawCertificateDto
import eu.greenpassapp.greenpassbackend.dto.UserArtifactsDto
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
    private val jwtHelper: JWTHelper,
) {

    @PostMapping(value = ["/insert"])
    fun insert(@RequestBody certificate: RawCertificateDto): ResponseEntity<UserArtifactsDto> {
        //TODO CHECK certificate
        val user = User("Jakob", "Stadlhuber", LocalDate.now(), "testType") //TODO check if user.link already exists

        val newUser = userLogic.insertOrUpdate(user)
        return ResponseEntity<UserArtifactsDto>(UserArtifactsDto(newUser.link, jwtHelper.getToken(newUser.link)), HttpStatus.OK) //insert new user
    }

    @PostMapping(value = ["/update"])
    fun update(@RequestBody certificate: RawCertificateDto, @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<Any> {
        //TODO CHECK certificate
        val user = User("Jakob2", "Stadlhuber2", LocalDate.now(), "testType")

        val token = headers["authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")
        user.link = jwtHelper.verifyTokenAndGetLink(token)
        userLogic.insertOrUpdate(user) //update existing user
        return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
    }
}