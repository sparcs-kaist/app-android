package com.example.soap.Shared.Extensions

import android.content.Context

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
