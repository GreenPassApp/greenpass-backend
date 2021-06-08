package eu.greenpassapp.greenpassbackend.beans.dgc

import se.digg.dgc.payload.v1.DigitalCovidCertificate

interface DigitalGreenCertificate {

    fun validate(certificate: String): DigitalCovidCertificate
}