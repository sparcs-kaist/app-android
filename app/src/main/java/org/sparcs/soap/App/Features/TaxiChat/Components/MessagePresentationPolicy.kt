package org.sparcs.soap.App.Features.TaxiChat.Components

import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat

interface MessagePresentationPolicy {
    fun metadata(
        kind: TaxiChat.ChatType,
        isMine: Boolean,
        indexInCluster: Int,
        clusterCount: Int,
        isStandalone: Boolean
    ): MetadataVisibility
}

class DefaultMessagePresentationPolicy : MessagePresentationPolicy {
    override fun metadata(
        kind: TaxiChat.ChatType,
        isMine: Boolean,
        indexInCluster: Int,
        clusterCount: Int,
        isStandalone: Boolean
    ): MetadataVisibility {
        if (isStandalone) {
            return MetadataVisibility(
                showName = !isMine,
                showAvatar = !isMine,
                showTime = true
            )
        }

        val isFirst = indexInCluster == 0
        val isLast = indexInCluster == clusterCount - 1

        return if (isMine) {
            MetadataVisibility(
                showName = false,
                showAvatar = false,
                showTime = isLast
            )
        } else {
            MetadataVisibility(
                showName = isFirst,
                showAvatar = isLast,
                showTime = isLast
            )
        }
    }
}