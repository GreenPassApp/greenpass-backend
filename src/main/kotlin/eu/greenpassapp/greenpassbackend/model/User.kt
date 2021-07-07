package eu.greenpassapp.greenpassbackend.model

import java.time.LocalDate
import java.time.LocalDateTime


data class User(val firstName: String, val lastName: String, val birthday: LocalDate){
    var vaccinated: CovidVaccinate? = null

    var tested: CovidTest? = null

    var recovered: CovidRecover? = null
}
