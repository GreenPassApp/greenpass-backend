package eu.greenpassapp.greenpassbackend.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import javax.persistence.*


@Entity
data class User(val firstName: String, val lastName: String, val birthday: LocalDate){
    @Id
    @GenericGenerator(name="random_link",strategy="eu.greenpassapp.greenpassbackend.generator.SecureRandomGenerator")
    @GeneratedValue(generator="random_link")
    var link: String? = null

    @OneToOne(cascade = [CascadeType.ALL])
    var vaccinated: CovidVaccinate? = null

    @OneToOne(cascade = [CascadeType.ALL])
    var tested: CovidTest? = null

    @OneToOne(cascade = [CascadeType.ALL])
    var recovered: CovidRecover? = null
}
