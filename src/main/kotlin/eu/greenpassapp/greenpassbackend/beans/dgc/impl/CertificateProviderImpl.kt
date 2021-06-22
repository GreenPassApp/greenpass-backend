package eu.greenpassapp.greenpassbackend.beans.dgc.impl

import com.beust.klaxon.Klaxon
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import se.digg.dgc.signatures.CertificateProvider
import java.io.FileReader
import java.math.BigInteger
import java.net.URL
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@Service
@Configuration
@EnableScheduling
class CertificateProviderImpl(
    @Value("\${cert.path}") private val certPath: String
) : CertificateProvider {
    private var certificates = mutableMapOf<String, String>()
    private var publicKey: PublicKey

    init {
        val file = FileReader("$certPath/certs.pub")
        val reader = PemReader(file)
        publicKey = readPemKeys(reader.readText()).first()
        reloadCerts()
    }

    //TODO: replace fixedRate with cron
    @Scheduled(fixedRate = 1000 * 60 * 60 * 12) //every 12h
    private fun reloadCerts() {
        val trustList = URL("https://de.dscg.ubirch.com/trustList/DSC/").readText()
        val json = decodeDscList(trustList)

        val parsedCerts = Klaxon().parse<TrustList>(json)
        certificates = mutableMapOf()
        parsedCerts?.certificates?.forEach {
            certificates[it.kid] = "-----BEGIN CERTIFICATE-----\n${it.rawData}\n-----END CERTIFICATE-----"
        }
    }

    override fun getCertificates(country: String?, kid: ByteArray?): MutableList<X509Certificate> {
        val encodedKid = com.nimbusds.jose.util.Base64.encode(kid).toString()
        val cf = CertificateFactory.getInstance("X.509")
        val cert = certificates[encodedKid] ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Can't find a matching certificate"
        )
        return mutableListOf(cf.generateCertificate(cert.byteInputStream()) as X509Certificate)
    }

    private fun decodeDscList(data: String): String {
        val encodedSignature = data.substringBefore("{")
        val signature = org.bouncycastle.util.encoders.Base64.decode(encodedSignature)
        val trustedList = data.substring(encodedSignature.length).trim()
        validateSignature(
            key = publicKey,
            data = trustedList.toByteArray(),
            signature = signature,
            algorithm = "SHA256withECDSA",
        )
        return trustedList
    }

    private fun validateSignature(key: PublicKey, data: ByteArray, signature: ByteArray, algorithm: String) {
        val javaSignature = if (algorithm.endsWith("withECDSA")) {
            val (r, s) = signature.splitHalves()
            DERSequence(
                arrayOf(
                    ASN1Integer(BigInteger(1, r)),
                    ASN1Integer(BigInteger(1, s)),
                )
            ).encoded
        } else {
            signature
        }
        val verifier = Signature.getInstance(algorithm).apply {
            initVerify(key)
            update(data)
        }
        if (!verifier.verify(javaSignature)) {
            throw SignatureException()
        }
    }

    private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> = take(size / 2).toByteArray() to drop(size / 2).toByteArray()

    private fun readPemKeys(data: String): List<PublicKey> {
        val converter = JcaPEMKeyConverter()
        return readRawPem(data)
            .mapNotNull { SubjectPublicKeyInfo.getInstance(it) }
            .map { converter.getPublicKey(it) }
            .toList()
    }

    private fun readRawPem(data: String): Sequence<Any> = sequence {
        val parser = PEMParser(data.reader())
        while (true) {
            val item = parser.readObject()
                ?: break
            yield(item)
        }
    }
}