package eu.greenpassapp.greenpassbackend.model

import org.apache.commons.lang3.RandomStringUtils
import java.security.SecureRandom
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id


@Entity
data class User(val firstName: String, val lastName: String, val birthday: LocalDate, var type: String){
    @Id
    var link: String = RandomStringUtils.random(10, 0, 0, true, true, null, SecureRandom()) //TODO check if link exists
}
