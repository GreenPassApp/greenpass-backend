package eu.greenpassapp.greenpassbackend.beans.dgc.impl

import com.beust.klaxon.Klaxon
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import se.digg.dgc.signatures.CertificateProvider
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.URL
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


/**
 * CertificateProviderImpl Impl
 *
 * This class uses the public keys from the different EU countries to validate a certificate.
 * @constructor Saves and validate the current public keys
 *
 */
@Service
@Configuration
@EnableScheduling
class CertificateProviderImpl : CertificateProvider {
    private var certificates = mutableMapOf<String, String>()
    private lateinit var publicKey: PublicKey

    init {
        val inputStream = javaClass.getResourceAsStream("/certs/certs.pub")
        val reader = BufferedReader(InputStreamReader(inputStream!!))

        publicKey = readPemKeys(reader.readText()).first()
        reloadCerts()
    }

    /**
     * Saves and validate the current public keys.
     */
    //TODO: replace fixedRate with cron
    @Scheduled(fixedRate = 1000 * 60 * 60 * 12) //every 12h
    private fun reloadCerts() {
        val trustList = try {
            URL("https://de.dscg.ubirch.com/trustList/DSC/").readText()
        } catch (e: Exception){
            val inputStream = javaClass.getResourceAsStream("/certs/FallbackTrustList")
            val reader = BufferedReader(InputStreamReader(inputStream!!))
            reader.readText()
        }

        val json = decodeDscList(trustList)

        val parsedCerts = Klaxon().parse<TrustList>(json)
        certificates = mutableMapOf()
        parsedCerts?.certificates?.forEach {
            certificates[it.kid] = "-----BEGIN CERTIFICATE-----\n${it.rawData}\n-----END CERTIFICATE-----"
        }
    }

    /**
     * Creates a pkpass bytearray.
     *
     * @param country not used
     * @param kid fingerprint for public keys
     *
     * @return All matching kid certificates.
     */
    override fun getCertificates(country: String?, kid: ByteArray?): MutableList<X509Certificate> {
        val encodedKid = com.nimbusds.jose.util.Base64.encode(kid).toString()
        val cf = CertificateFactory.getInstance("X.509")
        val cert = certificates[encodedKid] ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Can't find a matching certificate"
        )
        return mutableListOf(cf.generateCertificate(cert.byteInputStream()) as X509Certificate)
    }

    /**
     * https://github.com/Digitaler-Impfnachweis/covpass-android/blob/1077e700e30b29944777b6303eac6862dd2215a4/covpass-sdk/src/main/java/de/rki/covpass/sdk/cert/DscListDecoder.kt
     */
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

    /**
     * https://github.com/Digitaler-Impfnachweis/covpass-android/blob/1077e700e30b29944777b6303eac6862dd2215a4/covpass-sdk/src/main/java/de/rki/covpass/sdk/crypto/SignatureUtils.kt
     */
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

    /**
     * https://github.com/Digitaler-Impfnachweis/covpass-android/blob/1077e700e30b29944777b6303eac6862dd2215a4/covpass-sdk/src/main/java/de/rki/covpass/sdk/crypto/SignatureUtils.kt
     */
    private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> = take(size / 2).toByteArray() to drop(size / 2).toByteArray()

    /**
     * https://github.com/Digitaler-Impfnachweis/covpass-android/blob/3b95b05221e8108ea8cc3cb8e4268a5c1af124a6/covpass-sdk/src/main/java/de/rki/covpass/sdk/crypto/PemUtils.kt
     */
    private fun readPemKeys(data: String): List<PublicKey> {
        val converter = JcaPEMKeyConverter()
        return readRawPem(data)
            .mapNotNull { SubjectPublicKeyInfo.getInstance(it) }
            .map { converter.getPublicKey(it) }
            .toList()
    }

    /**
     * https://github.com/Digitaler-Impfnachweis/covpass-android/blob/3b95b05221e8108ea8cc3cb8e4268a5c1af124a6/covpass-sdk/src/main/java/de/rki/covpass/sdk/crypto/PemUtils.kt
     */
    private fun readRawPem(data: String): Sequence<Any> = sequence {
        val parser = PEMParser(data.reader())
        while (true) {
            val item = parser.readObject()
                ?: break
            yield(item)
        }
    }
}