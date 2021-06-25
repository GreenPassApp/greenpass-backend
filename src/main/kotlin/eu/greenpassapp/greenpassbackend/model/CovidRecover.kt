package eu.greenpassapp.greenpassbackend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class CovidRecover(var validUntil: LocalDate) {
    @Id
    @GeneratedValue
    @JsonIgnore
    val id: Int? = null
}
