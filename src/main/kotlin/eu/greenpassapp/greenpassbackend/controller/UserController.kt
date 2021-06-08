package eu.greenpassapp.greenpassbackend.controller

import eu.greenpassapp.greenpassbackend.beans.jwt.JWTHelper
import eu.greenpassapp.greenpassbackend.dto.RawCertificateDto
import eu.greenpassapp.greenpassbackend.dto.UserArtifactsDto
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.User
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
) {
    @PostMapping(value = ["/insert"])
    fun insert(@RequestBody certificate: RawCertificateDto): ResponseEntity<UserArtifactsDto> {
        val result = userLogic.insert(certificate.data)
        return ResponseEntity<UserArtifactsDto>(UserArtifactsDto(result.first.link!!, result.second), HttpStatus.OK)
    }

    @PostMapping(value = ["/update"])
    fun update(@RequestBody certificate: RawCertificateDto, @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<Any> {
        val token = headers["authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")
        userLogic.update(certificate.data, token)
        return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
    }
}