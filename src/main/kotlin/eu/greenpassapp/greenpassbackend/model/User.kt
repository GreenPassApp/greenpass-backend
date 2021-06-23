package eu.greenpassapp.greenpassbackend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*


@Entity
data class User(val firstName: String, val lastName: String, val birthday: LocalDate, @JsonIgnore val validUtil: LocalDateTime?){
    @Id
    @GenericGenerator(name="random_link",strategy="eu.greenpassapp.greenpassbackend.generator.SecureRandomGenerator")
    @GeneratedValue(generator="random_link")
    var link: String? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    var vaccinated: CovidVaccinate? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    var tested: CovidTest? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    var recovered: CovidRecover? = null
}
