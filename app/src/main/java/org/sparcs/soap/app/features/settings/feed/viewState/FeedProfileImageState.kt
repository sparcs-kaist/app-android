package org.sparcs.soap.app.features.settings.feed.viewState


import android.net.Uri

sealed interface FeedProfileImageState {
    data object NoChange : FeedProfileImageState
    data class Updated(val image: Uri?) : FeedProfileImageState
    data class Loading(val progress: Float) : FeedProfileImageState
    data class Error(val message: String) : FeedProfileImageState
}