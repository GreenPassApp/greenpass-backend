package eu.greenpassapp.greenpassbackend.beans.geo

import javax.servlet.http.HttpServletRequest

interface GeoIP {
    fun getCountryCode(ip: String): String
    fun getCountryCode(request: HttpServletRequest): String
}