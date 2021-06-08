package eu.greenpassapp.greenpassbackend.model

import eu.greenpassapp.greenpassbackend.dao.UserRepository
import eu.greenpassapp.greenpassbackend.logic.UserLogic
import org.apache.commons.lang3.RandomStringUtils
import org.hibernate.annotations.GenericGenerator
import org.springframework.beans.factory.annotation.Autowired
import java.security.SecureRandom
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


@Entity
data class User(val firstName: String, val lastName: String, val birthday: LocalDate, var type: String){
    @Id
    @GenericGenerator(name="random_link",strategy="eu.greenpassapp.greenpassbackend.generator.SecureRandomGenerator")
    @GeneratedValue(generator="random_link")
    var link: String? = null
}
