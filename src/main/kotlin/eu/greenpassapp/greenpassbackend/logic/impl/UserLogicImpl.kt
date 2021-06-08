package eu.greenpassapp.greenpassbackend.logic.impl

import eu.greenpassapp.greenpassbackend.beans.dgc.DigitalGreenCertificate
import eu.greenpassapp.greenpassbackend.beans.jwt.JWTHelper
import eu.greenpassapp.greenpassbackend.dao.UserRepository
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
@Transactional(rollbackFor = [Exception::class])
class UserLogicImpl(
    private val userRepository: UserRepository,
    private val jwtHelper: JWTHelper,
    private val digitalGreenCertificate: DigitalGreenCertificate
) : UserLogic {
    override fun insert(certificate: String): Pair<User, String> {
        val parsedCert = digitalGreenCertificate.validate(certificate)
        val user = User(parsedCert.nam.gn, parsedCert.nam.fn, LocalDate.parse(parsedCert.dob), "testType")

        val newUser = userRepository.saveAndFlush(user)
        val token = jwtHelper.getToken(newUser.link!!)
        return Pair(newUser, token)
    }

    override fun update(certificate: String, token: String) {
        val parsedCert = digitalGreenCertificate.validate(certificate)
        val user = User(parsedCert.nam.gn, parsedCert.nam.fn, LocalDate.parse(parsedCert.dob), "testType")

        user.link = jwtHelper.verifyTokenAndGetLink(token)
        userRepository.saveAndFlush(user)
    }

    override fun getUser(link: String): User{
        return userRepository.findByIdOrNull(link) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No User found")
    }

    override fun delete(token: String) {
        userRepository.delete(getUser(jwtHelper.verifyTokenAndGetLink(token)))
    }
}