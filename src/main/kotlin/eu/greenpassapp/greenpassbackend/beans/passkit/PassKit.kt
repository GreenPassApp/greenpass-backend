package eu.greenpassapp.greenpassbackend.beans.passkit

interface PassKit {
    fun generatePass(certificate: String): ByteArray
}