package eu.greenpassapp.greenpassbackend.model

import java.time.Instant

data class CovidTest(var type: String, var dateOfSampling: Instant)