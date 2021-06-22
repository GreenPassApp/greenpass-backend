package eu.greenpassapp.greenpassbackend.beans.passkit

import eu.greenpassapp.greenpassbackend.model.User

interface PassKit {
    fun generatePass(user: User, certificate: String, serialNumber: String): ByteArray
}