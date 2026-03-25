package org.sparcs.soap.App.Domain.Helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiLocation

object TaxiDeepLinkHelper {

    fun getKakaoTUri(source: TaxiLocation, destination: TaxiLocation): Uri {
        return Uri.parse("kakaot://taxi/set").buildUpon()
            .appendQueryParameter("origin_lat", source.latitude.toString())
            .appendQueryParameter("origin_lng", source.longitude.toString())
            .appendQueryParameter("dest_lat", destination.latitude.toString())
            .appendQueryParameter("dest_lng", destination.longitude.toString())
            .build()
    }

    fun getUberUri(source: TaxiLocation, destination: TaxiLocation): Uri {
        val uriString = "uber://?action=setPickup&client_id=a" +
                "&pickup[latitude]=${source.latitude}&pickup[longitude]=${source.longitude}" +
                "&dropoff[latitude]=${destination.latitude}&dropoff[longitude]=${destination.longitude}"
        return Uri.parse(uriString)
    }

    fun getKakaoPayUri(context: Context, account: String?): Uri {
        account?.let {
            val accountNo = it.split(" ").lastOrNull() ?: ""
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("account", accountNo)
            clipboard.setPrimaryClip(clip)
        }
        return Uri.parse("kakaotalk://kakaopay/money/to/bank")
    }

    fun getTossUri(account: String?): Uri {
        val components = account?.split(" ") ?: emptyList()
        val bankName = components.firstOrNull() ?: ""
        val accountNo = components.lastOrNull() ?: ""
        val bankCode = Constants.taxiBankCodeMap[bankName] ?: ""

        return Uri.parse("supertoss://send").buildUpon()
            .appendQueryParameter("bankCode", bankCode)
            .appendQueryParameter("accountNo", accountNo)
            .build()
    }
}