package org.sparcs.soap.app.domain.services.notifications

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Removes Taxi's notifications, once their chats are read or you sign out. */
interface TaxiNotificationCleaner {
    /** Removes the room's notifications. */
    fun cancel(roomId: String)

    /** Removes every Taxi notification and what they leave behind. */
    fun clearAll()
}

class SystemTaxiNotificationCleaner @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TaxiNotificationCleaner {
    override fun cancel(roomId: String) = TaxiNotifications.cancel(context, roomId)

    override fun clearAll() = TaxiNotifications.clearAll(context)
}
