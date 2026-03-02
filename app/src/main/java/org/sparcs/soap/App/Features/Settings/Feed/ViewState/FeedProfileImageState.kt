package org.sparcs.soap.App.Features.Settings.Feed.ViewState


import android.net.Uri

sealed interface FeedProfileImageState {
    data object NoChange : FeedProfileImageState
    data class Updated(val image: Uri) : FeedProfileImageState
    data object Removed : FeedProfileImageState
    data class Loading(val progress: Float) : FeedProfileImageState
    data class Error(val message: String) : FeedProfileImageState
}