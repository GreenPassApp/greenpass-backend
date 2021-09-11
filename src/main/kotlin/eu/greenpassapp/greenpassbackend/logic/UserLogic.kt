package eu.greenpassapp.greenpassbackend.logic


/**
 * User Logic
 *
 * The Logic Layer will be used from the view layer.
 * This class is used for the generation of the Apple Wallet pkpass Files and to map a IP to a CountryCode.
 * Everything appears locally on the server without third party server connections
 *
 */
interface UserLogic {
    fun generatePressKit(certificate: String, serialNumber: String): ByteArray
}