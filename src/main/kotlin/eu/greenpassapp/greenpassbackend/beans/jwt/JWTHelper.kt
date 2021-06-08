package eu.greenpassapp.greenpassbackend.beans.jwt

interface JWTHelper {
    fun getToken(linkClaim: String): String
    fun verifyTokenAndGetLink(token: String): String
}