package eu.greenpassapp.greenpassbackend.controller

import eu.greenpassapp.greenpassbackend.dto.RawCertificateDto
import eu.greenpassapp.greenpassbackend.dto.UserArtifactsDto
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
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
    fun updateUser(
        @RequestBody certificate: RawCertificateDto, @RequestHeader headers: Map<String, String>,
    ): ResponseEntity<Any> {
        val token = headers["authorization"] ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No token set")
        userLogic.update(certificate.data, token)
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
    fun getUser(@PathVariable link: String): ResponseEntity<User> {
        return ResponseEntity<User>(userLogic.getUser(link),HttpStatus.OK)
    }

    @GetMapping(value = ["/pass"])
    fun generatePassKit(@RequestBody certificate: RawCertificateDto): ResponseEntity<ByteArray> {
        return ResponseEntity<ByteArray>(userLogic.generatePressKit(certificate.data), HttpStatus.OK)
    }

    @GetMapping(value = ["/passWithOutBody.pkpass"])
    fun generatePassKitTest(): ResponseEntity<ByteArrayResource> {

        val header = HttpHeaders()
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.pkpass")
        header.add("Cache-Control", "no-cache, no-store, must-revalidate")
        header.add("Pragma", "no-cache")
        header.add("Expires", "0")

        val resource = ByteArrayResource(userLogic.generatePressKit("NCFTW2H:7*I06R3W/J:O6:P4QB3+7RKFVJWV66UBCE//UXDT:*ML-4D.NBXR+SRHMNIY6EB8I595+6UY9-+0DPIO6C5%0SBHN-OWKCJ6BLC2M.M/NPKZ4F3WNHEIE6IO26LB8:F4:JVUGVY8*EKCLQ..QCSTS+F\$:0PON:.MND4Z0I9:GU.LBJQ7/2IJPR:PAJFO80NN0TRO1IB:44:N2336-:KC6M*2N*41C42CA5KCD555O/A46F6ST1JJ9D0:.MMLH2/G9A7ZX4DCL*010LGDFI\$MUD82QXSVH6R.CLIL:T4Q3129HXB8WZI8RASDE1LL9:9NQDC/O3X3G+A:2U5VP:IE+EMG40R53CG9J3JE1KB KJA5*\$4GW54%LJBIWKE*HBX+4MNEIAD\$3NR E228Z9SS4E R3HUMH3J%-B6DRO3T7GJBU6O URY858P0TR8MDJ\$6VL8+7B5\$G CIKIPS2CPVDK%K6+N0GUG+TG+RB5JGOU55HXDR.TL-N75Y0NHQTZ3XNQMTF/ZHYBQ\$8IR9MIQHOSV%9K5-7%ZQ/.15I0*-J8AVD0N0/0USH.3"))

        return ResponseEntity.ok()
            .headers(header)
            //.contentLength(resource.length())
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body<ByteArrayResource>(resource)
    }
}