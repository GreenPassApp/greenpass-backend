package eu.greenpassapp.greenpassbackend.config

import eu.greenpassapp.greenpassbackend.logic.UserLogic
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
class Scheduler(private val userLogic: UserLogic) {

    @Scheduled(fixedRate = 1000 * 60 * 60) //every 1h
    fun deleteInvalidUsers(){
        userLogic.deleteInvalidUsers()
    }
}