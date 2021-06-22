package eu.greenpassapp.greenpassbackend.beans.passkit.impl

import com.ryantenney.passkit4j.Pass
import com.ryantenney.passkit4j.PassResource
import com.ryantenney.passkit4j.PassSerializer
import com.ryantenney.passkit4j.model.*
import com.ryantenney.passkit4j.sign.PassSignerImpl
import eu.greenpassapp.greenpassbackend.beans.passkit.PassKit
import eu.greenpassapp.greenpassbackend.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Service
class PassKitImpl(
    @Value("\${presskit.teamId}") private val teamId: String,
    @Value("\${presskit.certPw}") private val certPw: String

) : PassKit {
    private val signer = PassSignerImpl.builder()
        .keystore(FileInputStream("src/main/resources/Zertifikate.p12"), certPw)
        .alias("Jakob Stadlhuber")
        .intermediateCertificate(FileInputStream("src/main/resources/AppleWWDRCA.cer"))
        .build()

    override fun generatePass(user: User, certificate: String, serialNumber: String): ByteArray {
        val dateOfLastVaccinate = user.vaccinated?.dateOfLastVaccinate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val validUntilRecovered = user.recovered?.validUntil?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())

        val listOfTextField = mutableListOf<TextField>()
        if(user.vaccinated != null) listOfTextField.add(TextField("certTypeVac", dateOfLastVaccinate, "vac"))
        if(user.recovered != null) listOfTextField.add(TextField("certTypeRec", validUntilRecovered, "rec"))
        if(user.tested != null) listOfTextField.add(TextField("certTypeTest", formatter.format(user.tested?.dateOfSampling), "test"))

        val birthday = user.birthday.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        val pass = Pass()
            .teamIdentifier(teamId)
            .passTypeIdentifier("pass.eu.greenapp.certest")
            .organizationName("GreenPass App")
            .description("GreenPass App")
            .serialNumber(serialNumber)
            .barcode(
                Barcode(
                    BarcodeFormat.QR,
                    certificate
                )
            )
            .logoText("GreenPass")
            .labelColor(Color.WHITE)
            .foregroundColor(Color.WHITE)
            .backgroundColor(Color(19, 90, 207))
            .files(
                PassResource("en.lproj/pass.strings", File("src/main/resources/storecard/en.lproj/pass.strings")),
                PassResource("de.lproj/pass.strings", File("src/main/resources/storecard/de.lproj/pass.strings")),
                PassResource("src/main/resources/storecard/icon.png"),
                PassResource("src/main/resources/storecard/icon@2x.png"),
                PassResource("src/main/resources/storecard/logo.png"),
                PassResource("src/main/resources/storecard/logo@2x.png"),
            )
            .passInformation(
                StoreCard()
                    .headerFields(
                        TextField("certificat_label", "certificate", "Covid-19")
                            .textAlignment(TextAlignment.RIGHT)
                    )
                    .primaryFields(
                        listOfTextField as List<Field<*>>?
                    )
                    .auxiliaryFields(
                        TextField("name", "name_label", "${user.firstName} ${user.lastName}"),
                        TextField("birthday", "birthday_label", birthday)
                    )
                    .backFields(
                        TextField("terms", "terms_label", "terms_value")
                    )
            )

        val result = ByteArrayOutputStream()
        PassSerializer.writePkPassArchive(pass, signer, result)
        return result.toByteArray()
    }
}