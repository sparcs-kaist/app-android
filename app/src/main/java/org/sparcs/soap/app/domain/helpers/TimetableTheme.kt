package org.sparcs.soap.app.domain.helpers

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TimetableTheme(
    val id: String,
    val name: String,
    val hexColors: List<String>,
    val textColorHex: String,
    val separatorColorHex: String? = null,
    val backgroundColorHex: String? = null,
    val gridLabelColorHex: String? = null,
    val isBuiltIn: Boolean = false,
) {
    val isValid: Boolean
        get() = name.isNotBlank() && hexColors.size in 1..16 &&
                (hexColors + listOfNotNull(
                    textColorHex,
                    separatorColorHex,
                    backgroundColorHex,
                    gridLabelColorHex
                ))
                    .all { it.matches(Regex("[0-9a-fA-F]{6}")) }
    val textColor get() = color(textColorHex)
    val backgroundColor get() = backgroundColorHex?.let(::color)
    val separatorColor get() = separatorColorHex?.let(::color)
    val gridLabelColor: Color?
        get() = gridLabelColorHex?.let(::color) ?: backgroundColorHex?.let {
            val value = it.toLong(16)
            val brightness =
                .299 * ((value shr 16) and 255) + .587 * ((value shr 8) and 255) + .114 * (value and 255)
            if (brightness / 255 < .55) Color.White else Color.Black
        }

    fun colorFor(id: Int): Color = color(hexColors[Math.floorMod(id, hexColors.size)])
    fun duplicate(name: String) =
        copy(id = "custom.${UUID.randomUUID()}", name = name, isBuiltIn = false)

    companion object {
        fun color(hex: String) = Color(0xFF000000L or hex.toLong(16))
        val builtIn = listOf(
            TimetableTheme(
                "builtin.default",
                "Default",
                listOf(
                    "C3BA0A",
                    "E34B6C",
                    "307878",
                    "B4C94B",
                    "1D8253",
                    "FA9C3E",
                    "F06E70",
                    "233575",
                    "63A763",
                    "9D4EDD",
                    "68C2D9",
                    "DD615D",
                    "8F64C5",
                    "F26549",
                    "67929E",
                    "6476C5"
                ),
                "FFFFFF",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.legacy",
                "Legacy",
                listOf(
                    "F2CECE",
                    "F4B3AE",
                    "F2BCA0",
                    "F0D3AB",
                    "F1E1A9",
                    "F4F2B3",
                    "DBF4BE",
                    "BEEDD7",
                    "B7E2DE",
                    "C9EAF4",
                    "B4D3ED",
                    "B9C5ED",
                    "CCC6ED",
                    "D8C1F0",
                    "EBCAEF",
                    "F4BADB"
                ),
                "000000",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.olive",
                "Olive",
                listOf(
                    "3F4A1F",
                    "5E6B2B",
                    "6B7A26",
                    "1F4A22",
                    "336B3A",
                    "1D4438",
                    "2C6152",
                    "5A2F12",
                    "8B4A1E",
                    "A35C22",
                    "6B4A0F",
                    "8A6A14",
                    "4A4A14",
                    "757518",
                    "2F5C1F",
                    "47661F"
                ),
                "FFFDE9",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.cherryBlossom",
                "Cherry Blossom",
                listOf(
                    "FFB7C5",
                    "F48FB1",
                    "F06292",
                    "F8BBD0",
                    "E1BEE7",
                    "CE93D8",
                    "FFCDD2",
                    "EF9A9A",
                    "F6C1CE",
                    "DCEDC8",
                    "AED581",
                    "B2EBF2",
                    "80DEEA",
                    "FFF9C4",
                    "FFE082",
                    "D7CCC8"
                ),
                "4A2C35",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.spring",
                "Spring",
                listOf(
                    "A8E6A3",
                    "7FD67A",
                    "C5E1A5",
                    "AED581",
                    "DCE775",
                    "FFF176",
                    "FFD54F",
                    "FFAB91",
                    "F48FB1",
                    "CE93D8",
                    "B39DDB",
                    "90CAF9",
                    "80DEEA",
                    "A5D6A7",
                    "E6EE9C",
                    "FFCC80"
                ),
                "1F3A1F",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.summer",
                "Summer",
                listOf(
                    "0E7C7B",
                    "17A2A0",
                    "00A6A6",
                    "05A87F",
                    "E07A5F",
                    "EF476F",
                    "D62246",
                    "E09F3E",
                    "C1651A",
                    "118AB2",
                    "0B6E99",
                    "073B4C",
                    "3D348B",
                    "6A4C93",
                    "1B9AAA",
                    "F25C54"
                ),
                "FFFFFF",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.autumn",
                "Autumn",
                listOf(
                    "9C2B1E",
                    "B34724",
                    "D2691E",
                    "E08A3C",
                    "C9A227",
                    "A67C00",
                    "7A5C1E",
                    "6B4226",
                    "8B4513",
                    "A0522D",
                    "5C4033",
                    "7D5A3C",
                    "B7410E",
                    "8A3324",
                    "4E6E58",
                    "6E7F4C"
                ),
                "FFF6E9",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.winter",
                "Winter",
                listOf(
                    "CFE8F5",
                    "A9D6EB",
                    "8ABFDC",
                    "BFD7ED",
                    "D7E3F4",
                    "AEB8E0",
                    "C3B9DD",
                    "9FB3D9",
                    "B8E0DC",
                    "8FCFCB",
                    "D5E5E3",
                    "E2E8F0",
                    "C1CBD9",
                    "A3B1C2",
                    "DCE4EC",
                    "B6C6D6"
                ),
                "12283A",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.ocean",
                "Ocean",
                listOf(
                    "03045E",
                    "023E8A",
                    "0077B6",
                    "0096C7",
                    "00879B",
                    "006D77",
                    "13505B",
                    "1B4965",
                    "2A6F97",
                    "01497C",
                    "014F86",
                    "2C7DA0",
                    "468FAF",
                    "1D3557",
                    "457B9D",
                    "0B525B"
                ),
                "FFFFFF",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.sunset",
                "Sunset",
                listOf(
                    "6A0572",
                    "AB2346",
                    "D7263D",
                    "F02D3A",
                    "E85D04",
                    "DC2F02",
                    "9D0208",
                    "6A040F",
                    "BC3908",
                    "D97706",
                    "C9184A",
                    "A4133C",
                    "800F2F",
                    "7B2CBF",
                    "5A189A",
                    "3C096C"
                ),
                "FFFFFF",
                isBuiltIn = true
            ),
            TimetableTheme(
                "builtin.monochrome",
                "Monochrome",
                listOf(
                    "1C1C1E",
                    "2C2C2E",
                    "3A3A3C",
                    "48484A",
                    "545456",
                    "636366",
                    "6E6E73",
                    "7C7C80",
                    "1F2933",
                    "323F4B",
                    "3E4C59",
                    "52606D",
                    "616E7C",
                    "2D3436",
                    "414A4F",
                    "4F5B62"
                ),
                "FFFFFF",
                isBuiltIn = true
            ),
        )
        val Default get() = builtIn.first()
    }
}
