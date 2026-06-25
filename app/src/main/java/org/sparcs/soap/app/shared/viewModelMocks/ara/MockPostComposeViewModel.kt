package org.sparcs.soap.app.shared.viewModelMocks.ara

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol

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

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override suspend fun writePost(): Boolean { return false }
    override suspend fun updateSelectedImages(context: Context) {}
    override fun removeImage(index: Int) {}
}