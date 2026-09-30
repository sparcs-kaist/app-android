package org.sparcs.soap.notificationTests

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import com.google.firebase.messaging.RemoteMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.DeepLink
import org.sparcs.soap.app.domain.helpers.NotificationContent

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class FeedNotificationTest {
    private val context get() = RuntimeEnvironment.getApplication()

    private fun message(data: Map<String, String>) = RemoteMessage.Builder("test").setData(data).build()

    @Test fun dataLocalizationKeyOverridesGenericTitleForCommentsAndReplies() {
        for ((key, resource) in listOf("NEW_COMMENT" to R.string.new_comment, "NEW_REPLY" to R.string.new_reply)) {
            val message = RemoteMessage(Bundle().apply {
                putString("gcm.n.e", "1")
                putString("gcm.n.title", "test title")
                putString("gcm.n.body", "comment content")
                putString("title_loc_key", key)
            })
            val content = NotificationContent.fromMessage(context, message)!!
            assertEquals(context.getString(resource), content.title)
            assertEquals("comment content", content.body)
        }
    }

    @Test fun backgroundSdkCanResolveTheExactUppercaseResourceKeys() {
        for ((key, resource) in listOf("NEW_COMMENT" to R.string.new_comment, "NEW_REPLY" to R.string.new_reply)) {
            val id = context.resources.getIdentifier(key, "string", context.packageName)
            assertEquals(context.getString(resource), context.getString(id))
        }
    }

    @Test @Config(qualifiers = "ko-rKR")
    fun notificationKeysUseTheCurrentLanguage() {
        val content = NotificationContent.fromMessage(context,
            message(mapOf("title_loc_key" to "NEW_REPLY", "body" to "reply")))!!
        assertEquals(context.getString(R.string.new_reply), content.title)
        assertEquals(context.getString(R.string.new_reply), context.getString(R.string.NEW_REPLY))
    }

    @Test fun missingOrUnknownTitleDoesNotDropTheBody() {
        val content = NotificationContent.fromMessage(context,
            message(mapOf("title_loc_key" to "MISSING_NOTIFICATION_KEY", "body" to "comment")))!!
        assertEquals(context.getString(R.string.app_name), content.title)
        assertEquals("comment", content.body)
    }

    @Test fun localizationArgumentsAreAppliedAndMalformedArgumentsFallBack() {
        val content = NotificationContent.fromMessage(context, message(mapOf(
            "title_loc_key" to "feed_replying_to", "title_loc_args" to "[\"Buddy\"]", "body" to "reply",
        )))!!
        assertEquals(context.getString(R.string.feed_replying_to, "Buddy"), content.title)
        val malformed = NotificationContent.fromMessage(context, message(mapOf(
            "title_loc_key" to "missing_notification_key", "title_loc_args" to "invalid", "title" to "fallback", "body" to "reply",
        )))!!
        assertEquals("fallback", malformed.title)
    }

    @Test fun emptyDataDoesNotCreateAnEmptyNotification() {
        assertNull(NotificationContent.fromMessage(context, message(emptyMap())))
    }

    @Test fun backgroundLaunchExtrasPreserveReplyDestination() {
        val intent = Intent().putExtra("post_id", "post-1").putExtra("comment_id", "reply-2")
        assertEquals(DeepLink.FeedPost("post-1", "reply-2"), DeepLink.fromIntent(intent))
    }

    @Test fun feedDestinationsRoundTripWithoutLosingEncodedIds() {
        val destination = DeepLink.FeedPost("post/a?b", "reply&c=d")
        assertEquals(destination, DeepLink.fromUri(destination.toUri()))
        assertEquals(destination, DeepLink.fromIntent(Intent(Intent.ACTION_VIEW, destination.toUri())))
    }

    @Test fun separateNotificationsKeepSeparateCommentDestinations() {
        val first = DeepLink.fromNotificationData(mapOf("post_id" to "post", "comment_id" to "first"))
        val second = DeepLink.fromNotificationData(mapOf("post_id" to "post", "comment_id" to "second"))
        assertEquals(DeepLink.FeedPost("post", "first"), first)
        assertEquals(DeepLink.FeedPost("post", "second"), second)
    }

    @Test fun missingPostIdCannotInventACommentDestination() {
        assertNull(DeepLink.fromNotificationData(mapOf("title_loc_key" to "NEW_COMMENT")))
        assertNull(DeepLink.fromNotificationData(mapOf("post_id" to " ", "comment_id" to "reply")))
        assertNull(DeepLink.fromUri("https://example.com/feed/post?commentId=reply".toUri()))
        assertEquals(DeepLink.FeedPost("post"), DeepLink.fromUri("https://sparcs.org/feed/post".toUri()))
    }
}
