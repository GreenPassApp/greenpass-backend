package eu.greenpassapp.greenpassbackend.controller

import eu.greenpassapp.greenpassbackend.beans.geo.GeoIP
import eu.greenpassapp.greenpassbackend.dto.RawCertificateDto
import eu.greenpassapp.greenpassbackend.dto.UserArtifactsDto
import eu.greenpassapp.greenpassbackend.dto.UserWithCountryCode
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import org.springframework.core.io.ByteArrayResource
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping(
    value = ["/user"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@CrossOrigin
class UserController(
    private val userLogic: UserLogic,
    private val request: HttpServletRequest,
    private val geoIP: GeoIP
) {
    @PostMapping(value = ["/insert"])
    fun insertUser(
        @RequestBody certificate: RawCertificateDto,
        @RequestParam("validUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) validUntil: LocalDateTime
    ): ResponseEntity<UserArtifactsDto> {
        val result = userLogic.insert(certificate.data, validUntil)
        return ResponseEntity<UserArtifactsDto>(UserArtifactsDto(result.first.link!!, result.second), HttpStatus.OK)
    }

    @PostMapping(value = ["/update"])
    fun updateUser(
        @RequestBody certificate: RawCertificateDto,
        @RequestHeader headers: Map<String, String>,
        @RequestParam("validUntil") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) validUntil: LocalDateTime
    ): ResponseEntity<Any> {
        val token = headers["authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")
        userLogic.update(certificate.data, validUntil, token)
        return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping(value = ["/delete"])
    fun deleteUser(
        @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<Any> {
        val token = headers["authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")
        userLogic.delete(token)
        return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
    }

    @GetMapping(value = ["/get/{link}"])
    fun getUser(@PathVariable link: String): ResponseEntity<UserWithCountryCode> {
        return ResponseEntity<UserWithCountryCode>(UserWithCountryCode(userLogic.getUser(link), geoIP.getCountryCode(request)),HttpStatus.OK)
    }

    @GetMapping(value = ["/countryCode"])
    fun getCountryCode(): ResponseEntity<String> {
        return ResponseEntity<String>(geoIP.getCountryCode(request), HttpStatus.OK)
    }

    @GetMapping(value = ["/generatePassWithBody"])
    fun generatePassKitWithBody(
        @RequestBody certificate: RawCertificateDto,
        @RequestParam("serialNumber") serialNumber: String
    ): ResponseEntity<ByteArray> {
        return ResponseEntity<ByteArray>(userLogic.generatePressKit(certificate.data, serialNumber), HttpStatus.OK)
    }

    @GetMapping(value = ["/pass"])
    fun generatePassKit(
        @RequestParam("cert") cert: String,
        @RequestParam("serialNumber") serialNumber: String
    ): ResponseEntity<ByteArrayResource> {
        val header = HttpHeaders()
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pass.pkpass")
        header.add("Cache-Control", "no-cache, no-store, must-revalidate")
        header.add("Pragma", "no-cache")
        header.add("Expires", "0")

        val resource = ByteArrayResource(userLogic.generatePressKit(cert, serialNumber))

        return ResponseEntity.ok()
            .headers(header)
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body(resource)
    }
}