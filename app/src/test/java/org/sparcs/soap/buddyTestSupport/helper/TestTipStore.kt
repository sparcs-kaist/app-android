package org.sparcs.soap.buddyTestSupport.helper

import android.content.Context
import org.sparcs.soap.app.shared.tips.TipStore
import java.time.Clock
import java.time.ZoneId

fun testTipStore(context: Context, name: String = "tips_test"): TipStore {
    val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    preferences.edit().clear().commit()
    return TipStore(preferences, Clock.system(ZoneId.of("Asia/Seoul")))
}
