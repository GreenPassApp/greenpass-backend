package eu.greenpassapp.greenpassbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GreenpassBackendApplication

fun main(args: Array<String>) {
    runApplication<GreenpassBackendApplication>(*args)
}
