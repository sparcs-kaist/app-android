package org.sparcs.soap.app.domain.enums.ara

sealed class PostOrigin {
        data object All : PostOrigin()
        data object Board : PostOrigin()
        data class Topic(val topicID: String) : PostOrigin()
        data object None : PostOrigin()
}