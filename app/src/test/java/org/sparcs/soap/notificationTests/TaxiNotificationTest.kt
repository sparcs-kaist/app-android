package org.sparcs.soap.notificationTests

import android.app.Application
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.DeepLink
import org.sparcs.soap.app.domain.helpers.NotificationContent
import org.sparcs.soap.app.domain.services.notifications.TaxiNotifications

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class TaxiNotificationTest {
    private val context get() = RuntimeEnvironment.getApplication()

    // The data-only shape Buddy's backend sends for Taxi pushes
    private fun message(data: Map<String, String>) = RemoteMessage.Builder("test").setData(data).build()

    @Test fun chatMessagesUseTheSenderAndTextAsIs() {
        val content = NotificationContent.fromMessage(context, message(mapOf(
            "title" to "Goose", "body" to "On my way", "title_loc_key" to "", "body_loc_key" to "",
        )))!!
        assertEquals("Goose", content.title)
        assertEquals("On my way", content.body)
    }

    @Test fun rideEventsAreLocalizedWithTheirArguments() {
        val content = NotificationContent.fromMessage(context, message(mapOf(
            "title" to "To Daejeon Station", "body" to "",
            "body_loc_key" to "TAXI_DEPARTURE", "body_loc_args" to "[\"15\"]",
        )))!!
        assertEquals("To Daejeon Station", content.title)
        assertEquals(context.getString(R.string.taxi_push_departure, "15"), content.body)
    }

    @Test fun taxiKeysDoNotResolveToSimilarlyNamedAppStrings() {
        // taxi_arrival is the chat screen's "Arrival" button, not the push text
        val content = NotificationContent.fromMessage(context, message(mapOf(
            "title" to "To Daejeon Station", "body_loc_key" to "TAXI_ARRIVAL", "body_loc_args" to "[]",
        )))!!
        assertEquals(context.getString(R.string.taxi_push_arrival), content.body)
    }

    @Test @Config(qualifiers = "ko-rKR")
    fun rideEventsUseTheCurrentLanguage() {
        val content = NotificationContent.fromMessage(context, message(mapOf(
            "title" to "대전역", "body_loc_key" to "TAXI_IN", "body_loc_args" to "[\"거위\"]",
        )))!!
        assertEquals("거위 님이 참여했습니다.", content.body)
    }

    @Test fun taxiPushesAreRecognisedByCategoryAndRoom() {
        assertTrue(TaxiNotifications.isTaxi(mapOf("category" to "taxi_chat", "room_id" to "r1")))
        assertTrue(TaxiNotifications.isTaxi(mapOf("category" to "taxi_event", "room_id" to "r1")))
        assertFalse(TaxiNotifications.isTaxi(mapOf("category" to "taxi_chat")))
        assertFalse(TaxiNotifications.isTaxi(mapOf("post_id" to "p1")))
    }

    @Test fun taxiPushesOpenTheRoomsChatRatherThanAFeedPost() {
        val data = mapOf("category" to "taxi_chat", "room_id" to "r1", "url" to "/myroom/r1")
        assertEquals(DeepLink.TaxiChat("r1"), DeepLink.fromNotificationData(data))
    }

    @Test fun backgroundLaunchExtrasOpenTheRoomsChat() {
        val intent = Intent().putExtra("category", "taxi_event").putExtra("room_id", "r1")
        assertEquals(DeepLink.TaxiChat("r1"), DeepLink.fromIntent(intent))
    }
}
