package com.example.soap.Shared.ViewModelMocks

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Domain.Models.Ara.AraBoardGroup
import com.example.soap.Domain.Models.Ara.AraBoardTopic
import com.example.soap.Features.PostCompose.PostComposeViewModelProtocol

class MockPostComposeViewModel : PostComposeViewModelProtocol {
    val localizedString = LocalizedString(mapOf("en" to "Topic", "ko" to "주제"))
    override val board: AraBoard = AraBoard(
        id = 1,
        name =localizedString,
        topics = listOf(AraBoardTopic(1, "Topic1", localizedString ), AraBoardTopic(2, "Topic2", localizedString)),
        slug = "topic",
        group = AraBoardGroup(1, "Group1", localizedString),
        isReadOnly = false,
        userReadable = true,
        userWritable = true
    )

    override var selectedTopic: AraBoardTopic? = null
    override var title: String = ""
    override var content: String = ""
    override var selectedItems: List<Uri> = emptyList()
    override var selectedImages: List<Bitmap> = emptyList()
    override var writeAsAnonymous: Boolean = true
    override var isNSFW: Boolean = false
    override var isPolitical: Boolean = false

    override suspend fun writePost() {}
    override suspend fun updateSelectedImages(context: Context) {}
}