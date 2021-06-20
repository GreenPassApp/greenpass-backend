package eu.greenpassapp.greenpassbackend.beans.dgc.impl

import com.beust.klaxon.Klaxon
import com.nimbusds.jose.util.Base64
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import se.digg.dgc.signatures.CertificateProvider
import java.net.URL
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@Service
@Configuration
@EnableScheduling
class CertificateProviderImpl() : CertificateProvider {
    var certificates = mutableMapOf<String, String>()

    init {
        reloadCerts()
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 12) //every 12h
    private fun reloadCerts() {
        val trustList = URL("https://de.dscg.ubirch.com/trustList/DSC/").readText()
        var json = trustList.substringAfter("\"certificates\":")
        json = json.substring(0, json.length - 1)
        val parsedCerts = Klaxon().parseArray<PublicCert>(json)
        certificates = mutableMapOf()
        parsedCerts?.forEach {
            certificates[it.kid] = "-----BEGIN CERTIFICATE-----\n${it.rawData}\n-----END CERTIFICATE-----"
        }
    }

    override fun getCertificates(country: String?, kid: ByteArray?): MutableList<X509Certificate> {
        val encodedKid = Base64.encode(kid).toString()
        val cf = CertificateFactory.getInstance("X.509")
        val cert = certificates[encodedKid] ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Can't find a matching certificate"
        )
        return mutableListOf(cf.generateCertificate(cert.byteInputStream()) as X509Certificate)
    }
}