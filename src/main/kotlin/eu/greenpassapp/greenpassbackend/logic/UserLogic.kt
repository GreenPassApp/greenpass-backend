package eu.greenpassapp.greenpassbackend.logic


interface UserLogic {
    fun generatePressKit(certificate: String, serialNumber: String): ByteArray
}