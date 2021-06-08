package eu.greenpassapp.greenpassbackend.beans.dgc.impl

import eu.greenpassapp.greenpassbackend.beans.dgc.DigitalGreenCertificate
import org.springframework.stereotype.Service
import se.digg.dgc.payload.v1.DigitalCovidCertificate
import se.digg.dgc.service.impl.DefaultDGCDecoder
import se.digg.dgc.signatures.CertificateProvider

@Service
class DigitalGreenCertificateImpl(private val certificateProvider: CertificateProvider): DigitalGreenCertificate {
    val dgcDecoder = DefaultDGCDecoder(null, certificateProvider)

    override fun validate(certificate: String): DigitalCovidCertificate {
        return dgcDecoder.decode(certificate)
    }
}