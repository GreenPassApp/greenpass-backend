package eu.greenpassapp.greenpassbackend.beans.passkit.impl

import com.ryantenney.passkit4j.Pass
import com.ryantenney.passkit4j.PassResource
import com.ryantenney.passkit4j.PassSerializer
import com.ryantenney.passkit4j.model.*
import com.ryantenney.passkit4j.sign.PassSignerImpl
import eu.greenpassapp.greenpassbackend.beans.passkit.PassKit
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

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

    override fun generatePass(certificate: String): ByteArray {
        val pass = Pass()
            .teamIdentifier(teamId)
            .passTypeIdentifier("pass.eu.greenapp.certest")
            .organizationName("Boulder Coffee Co.")
            .description("Boulder Coffee Rewards Card")
            .serialNumber("p69f2J")
            .barcode(
                Barcode(
                    BarcodeFormat.QR,
                    certificate
                )
            )
            .logoText("Boulder Coffee")
            .labelColor(Color.WHITE)
            .foregroundColor(Color.WHITE)
            .backgroundColor(Color(118, 74, 50))
            .files(
                PassResource("en.lproj/pass.strings", File("src/main/resources/storecard/en.lproj/pass.strings")),
                PassResource("src/main/resources/storecard/icon.png"),
                PassResource("src/main/resources/storecard/icon@2x.png"),
                PassResource("src/main/resources/storecard/logo.png"),
                PassResource("src/main/resources/storecard/logo@2x.png"),
                PassResource("src/main/resources/storecard/strip.png"),
                PassResource("src/main/resources/storecard/strip@2x.png")
            )
            .passInformation(
                StoreCard()
                    .headerFields(
                        TextField("balance", "balance_label", "25")
                            .textAlignment(TextAlignment.RIGHT)
                    )
                    .auxiliaryFields(
                        TextField("level", "level_label", "level_gold"),
                        TextField("usual", "usual_label", "Iced Mocha")
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