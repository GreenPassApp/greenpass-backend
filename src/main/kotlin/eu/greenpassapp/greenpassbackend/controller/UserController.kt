package eu.greenpassapp.greenpassbackend.controller

import eu.greenpassapp.greenpassbackend.dto.RawCertificateDto
import eu.greenpassapp.greenpassbackend.dto.UserArtifactsDto
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping(
    value = ["/api/user"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class UserController(
    private val userLogic: UserLogic,
) {
    @PostMapping(value = ["/insert"])
    fun insertUser(@RequestBody certificate: RawCertificateDto): ResponseEntity<UserArtifactsDto> {
        val result = userLogic.insert(certificate.data)
        return ResponseEntity<UserArtifactsDto>(UserArtifactsDto(result.first.link!!, result.second), HttpStatus.OK)
    }

    @PostMapping(value = ["/update"])
    fun updateUser(@RequestBody certificate: RawCertificateDto, @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<Any> {
        val token = headers["authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")
        userLogic.update(certificate.data, token)
        return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping(value = ["/delete"])
    fun deleteUser(@RequestHeader headers: Map<String, String>,
    ): ResponseEntity<Any> {
        val token = headers["authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")
        userLogic.delete(token)
        return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
    }

    @GetMapping(value = ["/get/{link}"])
    fun getUser(@PathVariable link: String): ResponseEntity<User> {
        return ResponseEntity<User>(userLogic.getUser(link),HttpStatus.OK)
    }

    @GetMapping(value = ["/pass"])
    fun generatePassKit(@RequestBody certificate: RawCertificateDto): ResponseEntity<ByteArray> {
        return ResponseEntity<ByteArray>(userLogic.generatePressKit(certificate.data), HttpStatus.OK)
    }
}