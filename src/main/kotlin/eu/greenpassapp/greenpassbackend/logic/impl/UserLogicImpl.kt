package eu.greenpassapp.greenpassbackend.logic.impl

import eu.greenpassapp.greenpassbackend.beans.jwt.JWTHelper
import eu.greenpassapp.greenpassbackend.dao.UserRepository
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
@Transactional(rollbackFor = [Exception::class])
class UserLogicImpl(
    private val userRepository: UserRepository,
    private val jwtHelper: JWTHelper,
) : UserLogic {
    override fun insert(certificate: String): Pair<User, String> {
        //TODO CHECK certificate
        val user = User("Jakob", "Stadlhuber", LocalDate.now(), "testType")

        val newUser = userRepository.saveAndFlush(user)
        val token = jwtHelper.getToken(newUser.link!!)
        return Pair(newUser, token)
    }

    override fun update(certificate: String, token: String) {
        //TODO CHECK certificate
        val user = User("Jakob2", "Stadlhuber2", LocalDate.now(), "testType")

        user.link = jwtHelper.verifyTokenAndGetLink(token)
        userRepository.saveAndFlush(user)
    }
}