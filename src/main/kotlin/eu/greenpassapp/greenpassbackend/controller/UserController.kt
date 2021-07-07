package eu.greenpassapp.greenpassbackend.controller

import eu.greenpassapp.greenpassbackend.beans.geo.GeoIP
import eu.greenpassapp.greenpassbackend.model.RawCertificateDto
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    private val geoIP: GeoIP
) {
    /**
     * Maps the User Requests IP-Address to a Country Code.
     *
     * @return the Country Code.
     */
    @GetMapping(value = ["/countryCode"])
    fun getCountryCode(): ResponseEntity<String> {
        return ResponseEntity<String>(geoIP.getCountryCode(request), HttpStatus.OK)
    }

    /**
     * Creates a pkpass file without header information.
     *
     * @param certificate EU QR-Code certificate in the body
     * @param serialNumber for generating a pass, the same serialnumber is a useful hint for the apple wallet to replace the old pkpass
     *
     * @return the Country Code.
     */
    @GetMapping(value = ["/generatePassWithBody"])
    fun generatePassKitWithBody(
        @RequestBody certificate: RawCertificateDto,
        @RequestParam("serialNumber") serialNumber: String
    ): ResponseEntity<ByteArray> {
        return ResponseEntity<ByteArray>(userLogic.generatePressKit(certificate.data, serialNumber), HttpStatus.OK)
    }

    /**
     * Creates a pkpass file with header information.
     *
     * @param certificate EU QR-Code certificate as a param
     * @param serialNumber for generating a pass, the same serialnumber is a useful hint for the apple wallet to replace the old pkpass
     *
     * @return the Country Code.
     */
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