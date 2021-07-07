package eu.greenpassapp.greenpassbackend.model

import java.time.LocalDate

data class CovidVaccinate(var highestCurrDose: Int, var dosesNeeded: Int, var dateOfLastVaccinate: LocalDate, var dateOfFirst: LocalDate)