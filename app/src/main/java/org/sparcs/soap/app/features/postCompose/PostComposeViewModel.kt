package org.sparcs.soap.app.features.postCompose

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.ara.AraPostNicknameType
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.helpers.UploadImageDecoder
import org.sparcs.soap.app.domain.helpers.tiptap.toJson
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardTopic
import org.sparcs.soap.app.domain.models.ara.AraCreatePost
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraUpdatePost
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.ara.AraBoardUseCaseProtocol
import org.sparcs.soap.app.features.post.attachedImageUrls
import org.sparcs.soap.app.features.postCompose.event.PostComposeViewEvent
import org.sparcs.soap.app.shared.extensions.toAlertState
import javax.inject.Inject

interface PostComposeViewModelProtocol {
    val board: AraBoard
    val editingPost: AraPost?
    var selectedTopic: AraBoardTopic?
    var title: String
    val editor: ComposeEditorState

    var writeAsAnonymous: Boolean
    val canChooseAnonymity: Boolean
    var isNSFW: Boolean
    var isPolitical: Boolean

    var price: Long?
    var saleState: AraMarketListing.State
    val files: List<ComposeFile>

    val canSubmit: Boolean
    val hasChanges: Boolean
    val alertState: AlertState?
    var isAlertPresented: Boolean

    fun addFiles(uris: List<Uri>)
    fun removeFile(file: ComposeFile)
    suspend fun submit(): Boolean
}

@HiltViewModel
class PostComposeViewModel @Inject constructor(
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    savedStateHandle: SavedStateHandle,
    private val analyticsService: AnalyticsServiceProtocol,
    @param:ApplicationContext private val context: Context,
) : ViewModel(), PostComposeViewModelProtocol {

    override val board: AraBoard = savedStateHandle.decode<AraBoard>("board_json")
        ?: throw IllegalStateException("board_json is null. PostComposeViewModel requires a board_json to initialize.")
    override val editingPost: AraPost? = savedStateHandle.decode<AraPost>("post_json")

    override var selectedTopic: AraBoardTopic? by mutableStateOf(editingPost?.topic)
    override var title: String by mutableStateOf(editingPost?.title.orEmpty())
    override val editor = ComposeEditorState(
        editingPost?.content?.let { ComposeDocument.fromContent(it, Constants.ARA_SHARE_URL) }.orEmpty()
    )

    override var writeAsAnonymous: Boolean by mutableStateOf(editingPost?.isAnonymous ?: board.allows(AraPostNicknameType.ANONYMOUS))
    override val canChooseAnonymity: Boolean
        get() = board.allows(AraPostNicknameType.ANONYMOUS) &&
            (board.allows(AraPostNicknameType.REGULAR) || board.allows(AraPostNicknameType.REALNAME))
    override var isNSFW: Boolean by mutableStateOf(editingPost?.isNSFW ?: false)
    override var isPolitical: Boolean by mutableStateOf(editingPost?.isPolitical ?: false)

    private var _price by mutableStateOf(editingPost?.marketListing?.price)
    override var price: Long?
        get() = _price
        set(value) {
            _price = value?.coerceIn(0, AraMarketListing.MAX_PRICE)
        }
    override var saleState: AraMarketListing.State by mutableStateOf(editingPost?.marketListing?.state ?: AraMarketListing.State.ON_SALE)
    override var files: List<ComposeFile> by mutableStateOf(initialFiles(editingPost))
        private set

    override val canSubmit: Boolean
        get() = title.isNotEmpty() && !editor.isBlank

    private val initialState = snapshot()

    override val hasChanges: Boolean
        get() = editor.isModified || snapshot() != initialState

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    val nicknameType: AraPostNicknameType
        get() = when {
            writeAsAnonymous && board.allows(AraPostNicknameType.ANONYMOUS) -> AraPostNicknameType.ANONYMOUS
            board.allows(AraPostNicknameType.REGULAR) -> AraPostNicknameType.REGULAR
            board.allows(AraPostNicknameType.REALNAME) -> AraPostNicknameType.REALNAME
            else -> AraPostNicknameType.ANONYMOUS
        }

    override fun addFiles(uris: List<Uri>) {
        files = files + uris.map { ComposeFile.picked(context.contentResolver, it) }
    }

    override fun removeFile(file: ComposeFile) {
        files = files.filterNot { it.id == file.id }
    }

    override suspend fun submit(): Boolean = try {
        val uploads = uploadImages()
        val document = ComposeDocument.toTiptap(editor.blocks, uploads)
        val fileIds = uploadFiles()
        val post = editingPost
        if (post != null) {
            val existingUrls = post.attachments.orEmpty().mapNotNull { a -> a.file?.toString()?.let { it to a.id } }.toMap()
            val imageIds = ComposeDocument.imageIds(document, existingUrls)
            araBoardUseCase.updatePost(
                post.id,
                AraUpdatePost(
                    title = title,
                    content = document.toJson(),
                    attachmentIds = (fileIds + imageIds).distinct(),
                    isNSFW = isNSFW,
                    isPolitical = isPolitical,
                    marketListing = marketListing(),
                )
            )
            analyticsService.logEvent(PostComposeViewEvent.PostEdited)
        } else {
            araBoardUseCase.writePost(
                AraCreatePost(
                    title = title,
                    content = document.toJson(),
                    attachmentIds = uploads.values.map { it.first } + fileIds,
                    topic = selectedTopic,
                    isNSFW = isNSFW,
                    isPolitical = isPolitical,
                    nicknameType = nicknameType,
                    board = board,
                    marketListing = marketListing(),
                )
            )
            analyticsService.logEvent(PostComposeViewEvent.PostSubmitted)
        }
        true
    } catch (e: Exception) {
        alertState = e.toAlertState(R.string.unexpected_error_uploading_post)
        isAlertPresented = true
        false
    }

    private suspend fun uploadImages(): Map<String, Pair<Int, String>> = buildMap {
        editor.pickedImages.forEach { block ->
            val uri = (block.source as ComposeBlock.Image.Source.Picked).uri
            val bitmap = withContext(Dispatchers.IO) { UploadImageDecoder.decode(context.contentResolver, uri) } ?: return@forEach
            val attachment = araBoardUseCase.uploadImage(bitmap)
            attachment.file?.let { put(block.id, attachment.id to it.toString()) }
        }
    }

    private suspend fun uploadFiles(): List<Int> = files.map { file ->
        when (val source = file.source) {
            is ComposeFile.Source.Existing -> source.attachmentId
            is ComposeFile.Source.Picked -> {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(source.uri)?.use { it.readBytes() }
                } ?: throw IllegalStateException("Unable to read ${file.name}")
                araBoardUseCase.uploadFile(bytes, file.name, file.mimeType).id
            }
        }
    }

    private fun marketListing(): AraMarketListing? {
        if (!board.isMarket) return null
        return AraMarketListing(
            price = price,
            currency = editingPost?.marketListing?.currency ?: AraMarketListing.DEFAULT_CURRENCY,
            state = saleState,
        )
    }

    private fun snapshot(): List<Any?> =
        listOf(title, selectedTopic, writeAsAnonymous, isNSFW, isPolitical, price, saleState, files.map { it.id })

    private fun initialFiles(post: AraPost?): List<ComposeFile> {
        post ?: return emptyList()
        val attachedImages = post.attachedImageUrls.toSet()
        return post.attachments.orEmpty()
            .filter { !it.mimeType.startsWith("image/") || it.file?.toString() in attachedImages }
            .map(ComposeFile::existing)
    }

    private companion object {
        inline fun <reified T> SavedStateHandle.decode(key: String): T? =
            get<String>(key)?.let { Gson().fromJson(Uri.decode(it), T::class.java) }
    }
}
