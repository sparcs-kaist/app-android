package org.sparcs.soap.app.features.postCompose

import androidx.compose.ui.graphics.Color
import org.sparcs.soap.app.shared.views.tiptap.TiptapColor
import kotlin.math.roundToInt

object ComposeColors {
    val PALETTE = listOf(
        "#e03e2d",
        "#e67e23",
        "#2dc26b",
        "#3598db",
        "#b96ad9",
        "#7e8c8d",
    )

    fun normalize(css: String?): String? = TiptapColor.parse(css)?.let(::hex)

    fun color(hex: String?): Color? = TiptapColor.parse(hex)

    private fun hex(color: Color): String =
        "#" + listOf(color.red, color.green, color.blue).joinToString("") { channel ->
            (channel.coerceIn(0f, 1f) * 255).roundToInt().toString(16).padStart(2, '0')
        }
}
