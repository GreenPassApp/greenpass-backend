package eu.greenpassapp.greenpassbackend.dao

import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
}