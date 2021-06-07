package eu.greenpassapp.greenpassbackend.logic.impl

import eu.greenpassapp.greenpassbackend.dao.UserRepository
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class UserLogicImpl(private val userRepository: UserRepository): UserLogic {
    override fun insertOrUpdate(user: User): User {
        return userRepository.saveAndFlush(user)
    }
}