package eu.greenpassapp.greenpassbackend.logic

import eu.greenpassapp.greenpassbackend.model.User
import java.time.LocalDateTime

interface UserLogic {
    fun insert(certificate: String, validUntil: LocalDateTime): Pair<User, String>
    fun update(certificate: String, validUntil: LocalDateTime, token: String)
    fun getUser(link: String): User
    fun delete(token: String)
    fun generatePressKit(certificate: String, serialNumber: String): ByteArray
    fun deleteInvalidUsers()
}