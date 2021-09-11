package eu.greenpassapp.greenpassbackend.logic.impl

import eu.greenpassapp.greenpassbackend.beans.dgc.DigitalGreenCertificate
import eu.greenpassapp.greenpassbackend.beans.passkit.PassKit
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.CovidRecover
import eu.greenpassapp.greenpassbackend.model.CovidTest
import eu.greenpassapp.greenpassbackend.model.CovidVaccinate
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import se.digg.dgc.payload.v1.DigitalCovidCertificate
import se.digg.dgc.payload.v1.RecoveryEntry
import se.digg.dgc.payload.v1.TestEntry
import se.digg.dgc.payload.v1.VaccinationEntry
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * User Logic Impl
 *
 * The concrete implementation of UserLogic
 *
 */
@Service
class UserLogicImpl(
    private val digitalGreenCertificate: DigitalGreenCertificate,
    private val passKit: PassKit
) : UserLogic {
    /**
     * Creates a pkpass.
     *
     * @param certificate EU QR-Code certificate
     * @param serialNumber for generating a pass, the same serialnumber is a useful hint for the apple wallet to replace the old pkpass
     *
     * @return Pkpass as a ByteArray.
     */
    override fun generatePressKit(certificate: String, serialNumber: String): ByteArray {
        return passKit.generatePass(getParsedUserFrom(certificate), certificate, serialNumber)
    }

    /**
     * Validates and creates certificate.
     *
     * @param certificate EU QR-Code certificate
     *
     * @return User Object generated from the certificate.
     */
    private fun getParsedUserFrom(certificate: String): User {
        val parsedCert = digitalGreenCertificate.validate(certificate)
        val user = User(parsedCert.nam.gn, parsedCert.nam.fn, LocalDate.parse(parsedCert.dob))
        user.vaccinated = getVaccinatedFromUser(parsedCert)
        user.recovered = getRecoveredFromUser(parsedCert)
        user.tested = getTestedFromUser(parsedCert)
        return user
    }

    /**
     * Parses Vaccinating information from certificate.
     *
     * @param cert parsed certificate
     *
     * @return Optional CovidVaccinate.
     */
    private fun getVaccinatedFromUser(cert : DigitalCovidCertificate): CovidVaccinate? {
        val firstVacEntry = cert.v?.firstOrNull() ?: return null
        var first: VaccinationEntry = firstVacEntry
        var last: VaccinationEntry = firstVacEntry
        for (vacEntry in cert.v){
            if(vacEntry.dt.isBefore(first.dt)) first = vacEntry
            if(vacEntry.dt.isAfter(last.dt)) last = vacEntry
        }

        if(first.tg != "840539006" || last.tg != "840539006") throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can only use Covid Certificates")

        return CovidVaccinate(last.dn, last.sd, last.dt, first.dt)
    }

    /**
     * Parses recovering information from certificate.
     *
     * @param cert parsed certificate
     *
     * @return Optional CovidRecover.
     */
    private fun getRecoveredFromUser(cert: DigitalCovidCertificate): CovidRecover? {
        val firstRecoverEntry = cert.r?.firstOrNull() ?: return null
        var last: RecoveryEntry = firstRecoverEntry
        for (recoverEntry in cert.r){
            if(recoverEntry.du.isAfter(last.du)) last = recoverEntry
        }

        if(last.tg != "840539006") throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can only use Covid Certificates")
        if(last.du.isBefore(LocalDate.now())) throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can't use an old Recovery Certificate")

        return CovidRecover(last.du)
    }

    /**
     * Parses testing information from certificate.
     *
     * @param cert parsed certificate
     *
     * @return Optional CovidTest.
     */
    private fun getTestedFromUser(cert: DigitalCovidCertificate): CovidTest? {
        val firstTestEntry = cert.t?.firstOrNull() ?: return null
        var last: TestEntry = firstTestEntry
        for (testEntry in cert.t){
            if(testEntry.sc.isAfter(last.sc)) last = testEntry
        }

        if(last.tg != "840539006") throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can only use Covid Certificates")

        if(last.tr == "260373001") throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can't use a positive test")

        return CovidTest(last.tt, last.sc)
    }
}