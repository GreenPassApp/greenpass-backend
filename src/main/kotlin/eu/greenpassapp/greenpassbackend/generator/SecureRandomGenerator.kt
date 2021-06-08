package eu.greenpassapp.greenpassbackend.generator

import org.apache.commons.lang3.RandomStringUtils
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import java.io.Serializable
import java.security.SecureRandom

class SecureRandomGenerator: IdentifierGenerator {
    override fun generate(session: SharedSessionContractImplementor?, `object`: Any?): Serializable {
        var link: String?
        do {
            link = RandomStringUtils.random(10, 0, 0, true, true, null, SecureRandom())
            val query = session!!.createQuery("SELECT u FROM User u WHERE u.link = :param")
            query.setParameter("param", link)
        } while (query.list().isNotEmpty())

        return link!!
    }
}