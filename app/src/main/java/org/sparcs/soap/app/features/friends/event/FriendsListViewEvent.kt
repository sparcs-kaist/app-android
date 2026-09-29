package org.sparcs.soap.app.features.friends.event

import org.sparcs.soap.app.domain.enums.Event

sealed class FriendsListViewEvent : Event {
    data object FriendsLoaded : FriendsListViewEvent()
    data object FriendAdded : FriendsListViewEvent()
    data object FriendDeleted : FriendsListViewEvent()
    data object FavoriteToggled : FriendsListViewEvent()

    override val source: String
        get() = "FriendsListView"

    override val name: String
        get() = when (this) {
            is FriendsLoaded -> "friends_loaded"
            is FriendAdded -> "friend_added"
            is FriendDeleted -> "friend_deleted"
            is FavoriteToggled -> "friend_favorite_toggled"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}
