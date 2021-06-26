package eu.greenpassapp.greenpassbackend.beans.geo.impl

import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import eu.greenpassapp.greenpassbackend.beans.geo.GeoIP
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.util.*
import javax.servlet.http.HttpServletRequest

@Service
class GeoIPImpl(
    @Value("\${geo.path}") private val geoPath: String
): GeoIP {
    private val reader: DatabaseReader = DatabaseReader.Builder(javaClass.getResourceAsStream(geoPath)).withCache(CHMCache()).build()

    override fun getCountryCode(ip: String): String {
        return reader.city(InetAddress.getByName(ip)).country.isoCode
    }

    override fun getCountryCode(request: HttpServletRequest): String {
        return getCountryCode(getClientIpAddress(request))
    }

    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedForHeader = request.getHeader("X-Forwarded-For")
        return if (xForwardedForHeader == null) {
            request.remoteAddr
        } else {
            StringTokenizer(xForwardedForHeader, ",").nextToken().trim { it <= ' ' }
        }
    }
}