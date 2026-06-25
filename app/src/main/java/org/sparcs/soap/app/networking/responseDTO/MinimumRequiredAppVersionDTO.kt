package org.sparcs.soap.app.networking.responseDTO

import com.google.gson.annotations.SerializedName

data class MinimumRequiredAppVersionDTO(

    @SerializedName("ios")
    val ios: String? = null,


    @SerializedName("android")
    val android: String? = null
)