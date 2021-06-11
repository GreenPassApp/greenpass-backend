package eu.greenpassapp.greenpassbackend.model

import java.time.LocalDate
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class CovidVaccinate(var highestCurrDose: Int, var dosesNeeded: Int, var dateOfLastVaccinate: LocalDate, var dateOfFirst: LocalDate) {
    @Id
    @GeneratedValue
    val id: UUID? = null
}
