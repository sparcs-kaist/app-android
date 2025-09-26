package com.example.soap.Shared.Extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

val Context.screenWidth: Float
    get() {
        val displayMetrics = resources.displayMetrics
        return displayMetrics.widthPixels.toFloat()
    }

val Context.screenHeight: Float
    get() {
        val displayMetrics = resources.displayMetrics
        return displayMetrics.heightPixels.toFloat()
    }

fun Context.toBitmapDescriptor(
    @DrawableRes vectorResId: Int,
    tint: Color? = null
): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        ?: return BitmapDescriptorFactory.defaultMarker()
    val size = 15

    vectorDrawable.setBounds(
        0,
        0,
        vectorDrawable.intrinsicWidth + size,
        vectorDrawable.intrinsicHeight + size
    )

    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth + size,
        vectorDrawable.intrinsicHeight + size,
        Bitmap.Config.ARGB_8888
    )

    tint?.let {
        DrawableCompat.setTint(vectorDrawable, it.toArgb())
    }

    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
