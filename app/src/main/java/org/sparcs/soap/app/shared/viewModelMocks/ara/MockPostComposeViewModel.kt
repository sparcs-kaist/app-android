package org.sparcs.soap.app.shared.viewModelMocks.ara

import android.net.Uri
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.postCompose.ComposeEditorState
import org.sparcs.soap.app.features.postCompose.ComposeFile
import org.sparcs.soap.app.features.postCompose.PostComposeViewModelProtocol

class MockPostComposeViewModel(boardSlug: String = "topic") : PostComposeViewModelProtocol {
    private val localizedString = LocalizedString(mapOf("en" to "Topic", "ko" to "주제"))
    override val board: AraBoard = AraBoard(
        id = 1,
        name = localizedString,
        topics = listOf(AraBoardTopic(1, "Topic1", localizedString), AraBoardTopic(2, "Topic2", localizedString)),
        slug = boardSlug,
        group = AraBoardGroup(1, "Group1", localizedString),
        isReadOnly = false,
        userReadable = true,
        userWritable = true
    )
    override val editingPost: AraPost? = null

    override var selectedTopic: AraBoardTopic? = null
    override var title: String = ""
    override val editor = ComposeEditorState()

    override var writeAsAnonymous: Boolean = true
    override val canChooseAnonymity: Boolean = true
    override var isNSFW: Boolean = false
    override var isPolitical: Boolean = false

    override var price: Long? = null
    override var saleState: AraMarketListing.State = AraMarketListing.State.ON_SALE
    override val files: List<ComposeFile> = emptyList()

    override val canSubmit: Boolean = false
    override val hasChanges: Boolean = false
    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override fun addFiles(uris: List<Uri>) {}
    override fun removeFile(file: ComposeFile) {}
    override suspend fun submit(): Boolean = false
}
