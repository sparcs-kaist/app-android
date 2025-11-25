package com.sparcs.soap.Domain.Enums.Ara

sealed class PostOrigin {
        data object All : PostOrigin()
        data object Board : PostOrigin()
        data class Topic(val topicID: String) : PostOrigin()
        data object None : PostOrigin()
}