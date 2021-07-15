package eu.greenpassapp.greenpassbackend.controller

import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.RawCertificateDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

/**
 * User REST Controller
 *
 * This class represents the view layer which delegate to the logic layer
 *
 */
@RestController
@RequestMapping(
    value = ["/user"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@CrossOrigin
class UserController(
    private val userLogic: UserLogic,
    private val request: HttpServletRequest,
    @Value("\${controller.apikey}") private val apikey: String
) {
    /**
     * Creates a pkpass file without header information.
     *
     * @param userApiKey apiKey
     * @param certificate EU QR-Code certificate in the body
     * @param serialNumber for generating a pass, the same serialnumber is a useful hint for the apple wallet to replace the old pkpass
     *
     * @return the Country Code.
     */
    @GetMapping(value = ["/generatePassWithBody"])
    fun generatePassKitWithBody(
        @RequestHeader("Authorization") userApiKey: String,
        @RequestHeader("X-Serial-Number") serialNumber: String,
        @RequestBody certificate: RawCertificateDto
    ): ResponseEntity<ByteArray> {
        if(apikey != userApiKey) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Authorization forbidden")
        return ResponseEntity<ByteArray>(userLogic.generatePressKit(certificate.data, serialNumber), HttpStatus.OK)
    }

    /**
     * Creates a pkpass file with header information.
     *
     * @param userApiKey apiKey
     * @param certificate EU QR-Code certificate as a header param
     * @param serialNumber for generating a pass, the same serialnumber is a useful hint for the apple wallet to replace the old pkpass
     *
     * @return the Country Code.
     */
    @GetMapping(value = ["/pass"])
    fun generatePassKit(
        @RequestHeader("Authorization") userApiKey: String,
        @RequestHeader("X-Digital-Certificate") cert: String,
        @RequestHeader("X-Serial-Number") serialNumber: String
    ): ResponseEntity<ByteArrayResource> {
        if(apikey != userApiKey) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Authorization forbidden")

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