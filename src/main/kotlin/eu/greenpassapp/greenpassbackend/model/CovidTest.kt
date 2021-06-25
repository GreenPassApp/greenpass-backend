package eu.greenpassapp.greenpassbackend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class CovidTest(var type: String, var dateOfSampling: Instant) {
    @Id
    @GeneratedValue
    @JsonIgnore
    val id: Int? = null
}
