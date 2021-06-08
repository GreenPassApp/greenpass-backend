package eu.greenpassapp.greenpassbackend.beans.dgc.impl

import com.nimbusds.jose.util.Base64
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import se.digg.dgc.signatures.CertificateProvider
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

@Service
class CertificateProviderImpl(
    @Value("\${cert.path}") private val certPath: String
) : CertificateProvider {
    val certificates = mutableMapOf<String, String>()

    init {
        Files.walk(Paths.get(certPath)).use { paths ->
            paths
                .filter(Files::isRegularFile)
                .forEach {
                    val file = FileReader("$certPath/${it.name}")
                    val reader = PemReader(file)
                    certificates[it.nameWithoutExtension] = reader.readText()
                }
        }
    }

    override fun getCertificates(country: String?, kid: ByteArray?): MutableList<X509Certificate> {
        val encodedKid = Base64.encode(kid).toString()
        val cf = CertificateFactory.getInstance("X.509")
        val cert = certificates[encodedKid] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Can't find a matching certificate")
        return mutableListOf(cf.generateCertificate(cert.byteInputStream()) as X509Certificate)
    }
}