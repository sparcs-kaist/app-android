package org.sparcs.soap.App.Features.PostList.Event

import org.sparcs.soap.App.Domain.Enums.Event

sealed class PostListViewEvent : Event {
    data object PostsRefreshed : PostListViewEvent()
    data object NextPageLoaded : PostListViewEvent()
    data class SearchPerformed(val keyword: String) : PostListViewEvent()

    override val source: String = "PostListView"

    override val name: String
        get() = when (this) {
            is PostsRefreshed -> "posts_refreshed"
            is NextPageLoaded -> "next_page_loaded"
            is SearchPerformed -> "search_performed"
        }

    override val parameters: Map<String, Any>
        get() = when (this) {
            is SearchPerformed -> mapOf(
                "source" to source,
                "keyword" to keyword
            )
            else -> mapOf("source" to source)
        }
}