package eu.greenpassapp.greenpassbackend.beans.dgc.impl

class PublicCert(
    val certificateType: String,
    val country: String,
    val kid: String,
    val rawData: String,
    val signature: String,
    val thumbprint: String,
    val timestamp: String
)