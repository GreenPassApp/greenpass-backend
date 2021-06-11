package eu.greenpassapp.greenpassbackend.model

import java.time.LocalDate
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class CovidRecover(var validUntil: LocalDate) {
    @Id
    @GeneratedValue
    val id: UUID? = null
}
