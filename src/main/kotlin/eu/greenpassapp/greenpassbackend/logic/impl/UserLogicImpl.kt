package eu.greenpassapp.greenpassbackend.logic.impl

import eu.greenpassapp.greenpassbackend.beans.dgc.DigitalGreenCertificate
import eu.greenpassapp.greenpassbackend.beans.jwt.JWTHelper
import eu.greenpassapp.greenpassbackend.beans.passkit.PassKit
import eu.greenpassapp.greenpassbackend.dao.UserRepository
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.CovidRecover
import eu.greenpassapp.greenpassbackend.model.CovidTest
import eu.greenpassapp.greenpassbackend.model.CovidVaccinate
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import se.digg.dgc.payload.v1.DigitalCovidCertificate
import se.digg.dgc.payload.v1.RecoveryEntry
import se.digg.dgc.payload.v1.TestEntry
import se.digg.dgc.payload.v1.VaccinationEntry
import java.time.LocalDate

@Service
@Transactional(rollbackFor = [Exception::class])
class UserLogicImpl(
    private val userRepository: UserRepository,
    private val jwtHelper: JWTHelper,
    private val digitalGreenCertificate: DigitalGreenCertificate,
    private val passKit: PassKit
) : UserLogic {
    override fun insert(certificate: String): Pair<User, String> {
        val user = getParsedUserFrom(certificate)

        val newUser = userRepository.saveAndFlush(user)
        val token = jwtHelper.getToken(newUser.link!!)
        return Pair(newUser, token)
    }

    override fun update(certificate: String, token: String) {
        val user = getParsedUserFrom(certificate)

        user.link = jwtHelper.verifyTokenAndGetLink(token)
        userRepository.saveAndFlush(user)
    }

    override fun getUser(link: String): User{
        return userRepository.findByIdOrNull(link) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No User found")
    }

    override fun delete(token: String) {
        userRepository.delete(getUser(jwtHelper.verifyTokenAndGetLink(token)))
    }

    override fun generatePressKit(certificate: String): ByteArray {
        digitalGreenCertificate.validate(certificate) //TODO: probably use getParsedUserFrom and check if valid
        return passKit.generatePass(certificate)
    }

    private fun getParsedUserFrom(certificate: String): User {
        val parsedCert = digitalGreenCertificate.validate(certificate)
        val user = User(parsedCert.nam.gn, parsedCert.nam.fn, LocalDate.parse(parsedCert.dob))
        user.vaccinated = getVaccinatedFromUser(parsedCert)
        user.recovered = getRecoveredFromUser(parsedCert)
        user.tested = getTestedFromUser(parsedCert)
        return user
    }

    private fun getVaccinatedFromUser(cert : DigitalCovidCertificate): CovidVaccinate? {
        val firstVacEntry = cert.v?.firstOrNull() ?: return null
        var first: VaccinationEntry = firstVacEntry
        var last: VaccinationEntry = firstVacEntry
        for (vacEntry in cert.v){
            if(vacEntry.dt.isBefore(first.dt)) first = vacEntry
            if(vacEntry.dt.isAfter(last.dt)) last = vacEntry
        }

        //TODO: Remove commend in prod
        //if(LocalDate.now().isAfter(last.dt)) throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can't use an old certificate")

        return CovidVaccinate(last.dn, last.sd, last.dt, first.dt)
    }

    private fun getRecoveredFromUser(cert: DigitalCovidCertificate): CovidRecover? {
        val firstRecoverEntry = cert.r?.firstOrNull() ?: return null
        var last: RecoveryEntry = firstRecoverEntry
        for (recoverEntry in cert.r){
            if(recoverEntry.du.isAfter(last.du)) last = recoverEntry
        }

        //TODO: Remove commend in prod
        //if(LocalDate.now().isAfter(last.du)) throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can't use an old certificate")

        return CovidRecover(last.du)
    }

    private fun getTestedFromUser(cert: DigitalCovidCertificate): CovidTest? {
        val firstTestEntry = cert.t?.firstOrNull() ?: return null
        var last: TestEntry = firstTestEntry
        for (testEntry in cert.t){
            if(testEntry.sc.isAfter(last.sc)) last = testEntry
        }

        //TODO: Remove commend in prod
        //if(Instant.now().isAfter(last.sc)) throw ResponseStatusException(HttpStatus.FORBIDDEN, "You can't use an old certificate")

        return CovidTest(last.nm, last.sc) //TODO check nm
    }
}