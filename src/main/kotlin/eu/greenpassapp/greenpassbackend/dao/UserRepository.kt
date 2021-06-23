package eu.greenpassapp.greenpassbackend.dao

import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun deleteAllByValidUtilBefore(validUntil: LocalDateTime): Long
    fun findByLinkAndValidUtilAfter(link: String, validUntil: LocalDateTime): User?
}