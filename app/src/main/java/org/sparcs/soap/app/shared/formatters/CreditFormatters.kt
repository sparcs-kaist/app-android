package org.sparcs.soap.app.shared.formatters

import java.text.DecimalFormat

fun formatGPA(gpa: Double?): String = gpa?.let { DecimalFormat("0.0#").format(it) } ?: "—"

fun creditProgress(taken: Int, minimum: Int): Float =
    if (minimum <= 0) 1f else (taken.toFloat() / minimum).coerceIn(0f, 1f)
