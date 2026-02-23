package org.sparcs.soap.App.Features.PostCompose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.App.Domain.Enums.Ara.AraPostNicknameType
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Domain.Models.Ara.AraBoardTopic
import org.sparcs.soap.App.Domain.Models.Ara.AraCreatePost
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.Ara.AraBoardUseCaseProtocol
import org.sparcs.soap.App.Features.PostCompose.Event.PostComposeViewEvent
import javax.inject.Inject

interface PostComposeViewModelProtocol {
    val board: AraBoard
    var selectedTopic: AraBoardTopic?
    var title: String
    var content: String
    var selectedItems: List<Uri>
    var selectedImages: List<Bitmap>

    var writeAsAnonymous: Boolean
    var isNSFW: Boolean
    var isPolitical: Boolean

    suspend fun writePost()
    suspend fun updateSelectedImages(context: Context)
    fun removeImage(index: Int)
}

@HiltViewModel
class PostComposeViewModel @Inject constructor(
    private val araBoardUseCase: AraBoardUseCaseProtocol,
    savedStateHandle: SavedStateHandle,
    private val analyticsService: AnalyticsServiceProtocol,
    @ApplicationContext private val context: Context
) : ViewModel(), PostComposeViewModelProtocol {

    // MARK: - Board
    private val initialBoard: AraBoard by lazy {
        val json = savedStateHandle.get<String>("board_json")
            ?: throw IllegalStateException("board_json is null. PostListViewModel requires a board_json to initialize.")
        Gson().fromJson(Uri.decode(json), AraBoard::class.java)
    }

    override var board: AraBoard = initialBoard
    // MARK: - Properties
    override var selectedTopic: AraBoardTopic? by mutableStateOf(null)
    override var title: String by mutableStateOf("")
    override var content: String by mutableStateOf("")

    private var _selectedItems by mutableStateOf(emptyList<Uri>())
    override var selectedItems: List<Uri>
        get() = _selectedItems
        set(value) {
            _selectedItems = value
            viewModelScope.launch { updateSelectedImages(context) }
        }

    override var selectedImages: List<Bitmap> by mutableStateOf(emptyList())

    override var writeAsAnonymous: Boolean by mutableStateOf(true)
    override var isNSFW: Boolean by mutableStateOf(false)
    override var isPolitical: Boolean by mutableStateOf(false)

    override suspend fun updateSelectedImages(context: Context) {
        val bitmaps = withContext(Dispatchers.IO) {
            selectedItems.mapNotNull { uri ->
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(stream)
                } catch (e: Exception) {
                    null
                }
            }
        }
        selectedImages = bitmaps
    }

    override suspend fun writePost() {
        val attachments = selectedImages.map { bitmap ->
            viewModelScope.async { araBoardUseCase.uploadImage(bitmap) }
        }.awaitAll()

        val request = AraCreatePost(
            title = title,
            content = content,
            attachments = attachments,
            topic = selectedTopic,
            isNSFW = isNSFW,
            isPolitical = isPolitical,
            nicknameType = if (writeAsAnonymous) AraPostNicknameType.ANONYMOUS else AraPostNicknameType.REGULAR,
            board = board
        )

        araBoardUseCase.writePost(request)
        analyticsService.logEvent(PostComposeViewEvent.PostSubmitted)
    }

    override fun removeImage(index: Int) {
        val mutable = selectedImages.toMutableList()
        mutable.removeAt(index)
        selectedImages = mutable.toList()
        _selectedItems = _selectedItems.toMutableList().apply { removeAt(index) }
    }
}
