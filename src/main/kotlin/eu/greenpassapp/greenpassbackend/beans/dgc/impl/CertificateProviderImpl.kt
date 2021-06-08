package eu.greenpassapp.greenpassbackend.beans.dgc.impl

import com.nimbusds.jose.util.Base64
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import se.digg.dgc.signatures.CertificateProvider
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@Service
class CertificateProviderImpl : CertificateProvider {
    val certificates = mapOf(
        "2Rk3X8HntrI=" to
"""-----BEGIN CERTIFICATE-----
MIIBvTCCAWOgAwIBAgIKAXk8i88OleLsuTAKBggqhkjOPQQDAjA2MRYwFAYDVQQDDA1BVCBER0MgQ1NDQSAxMQswCQYDVQQGEwJBVDEPMA0GA1UECgwGQk1TR1BLMB4XDTIxMDUwNTEyNDEwNloXDTIzMDUwNTEyNDEwNlowPTERMA8GA1UEAwwIQVQgRFNDIDExCzAJBgNVBAYTAkFUMQ8wDQYDVQQKDAZCTVNHUEsxCjAIBgNVBAUTATEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAASt1Vz1rRuW1HqObUE9MDe7RzIk1gq4XW5GTyHuHTj5cFEn2Rge37+hINfCZZcozpwQKdyaporPUP1TE7UWl0F3o1IwUDAOBgNVHQ8BAf8EBAMCB4AwHQYDVR0OBBYEFO49y1ISb6cvXshLcp8UUp9VoGLQMB8GA1UdIwQYMBaAFP7JKEOflGEvef2iMdtopsetwGGeMAoGCCqGSM49BAMCA0gAMEUCIQDG2opotWG8tJXN84ZZqT6wUBz9KF8D+z9NukYvnUEQ3QIgdBLFSTSiDt0UJaDF6St2bkUQuVHW6fQbONd731/M4nc=
-----END CERTIFICATE-----"""
    )

    override fun getCertificates(country: String?, kid: ByteArray?): MutableList<X509Certificate> {
        val encodedKid = Base64.encode(kid).toString()
        val cf = CertificateFactory.getInstance("X.509")
        val cert = certificates[encodedKid] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Can't find a matching certificate")
        return mutableListOf(cf.generateCertificate(cert.byteInputStream()) as X509Certificate)
    }
}