package org.sparcs.soap.app.domain.helpers

/** A kind of notification you can turn on or off on each device, as Buddy's backend knows it. */
enum class NotificationCategory(val rawValue: String) {
    /** Comments on your Feed posts. */
    FEED_COMMENT("feed_comment"),

    /** Replies to your Feed comments. */
    FEED_REPLY("feed_reply"),

    /** Chats in your Taxi rides. */
    TAXI_CHAT("taxi_chat"),

    /** Updates on your Taxi rides, such as riders joining, settlement and departure reminders. */
    TAXI_EVENT("taxi_event"),
    ARA("ara"),
    TIMETABLE("timetable");

    companion object {
        fun fromRawValue(value: String): NotificationCategory? = entries.find { it.rawValue == value }
    }
}
