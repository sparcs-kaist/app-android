package org.sparcs.soap.app.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import org.sparcs.soap.app.features.feedPost.FeedPostViewModel
import javax.inject.Inject

/** Keeps only the active post alive while its UI moves between detail and panel. */
@HiltViewModel
class ActiveFeedPostViewModel @Inject constructor(
    private val factory: FeedPostViewModel.Factory,
) : ViewModel() {
    private val store = ViewModelStore()
    private var postID: String? = null

    operator fun get(postID: String?): FeedPostViewModel? {
        if (this.postID != postID) {
            store.clear()
            this.postID = postID
        }
        if (postID == null) return null
        return ViewModelProvider(store, viewModelFactory {
            initializer { factory.create(postID) }
        })[FeedPostViewModel::class.java]
    }

    override fun onCleared() {
        store.clear()
    }
}
