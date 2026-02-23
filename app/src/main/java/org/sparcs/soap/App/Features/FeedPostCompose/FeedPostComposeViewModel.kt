package org.sparcs.soap.App.Features.FeedPostCompose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.App.Domain.Enums.Feed.FeedPostPhotoItem
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreatePost
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedImageUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedPostUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Features.FeedPostCompose.Event.FeedPostComposeViewEvent
import org.sparcs.soap.R
import java.util.UUID
import javax.inject.Inject


interface FeedPostComposeViewModelProtocol {
    var feedUser: FeedUser?
    var text: String
    var selectedComposeType: FeedPostComposeViewModel.ComposeType
    var selectedItems: List<Uri>
    var selectedImages: List<FeedPostPhotoItem>

    val alertState: AlertState?
    var isAlertPresented: Boolean
    var isUploading: Boolean

    fun fetchFeedUser()
    suspend fun writePost()
    suspend fun submitPost(): Boolean
    fun removeImage(index: Int)
    fun handleException(error: Throwable)
}

@HiltViewModel
class FeedPostComposeViewModel @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val feedImageUseCase: FeedImageUseCaseProtocol,
    private val feedPostUseCase: FeedPostUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    @ApplicationContext private val context: Context
) : ViewModel(), FeedPostComposeViewModelProtocol {

    sealed class ComposeType(val value: Int) {
        data object Publicly : ComposeType(0)
        data object Anonymously : ComposeType(1)
    }

    // MARK: - Properties
    override var feedUser by mutableStateOf<FeedUser?>(null)

    override var text by mutableStateOf("")
    override var selectedComposeType: ComposeType by mutableStateOf(ComposeType.Anonymously)

    private var _selectedItems by mutableStateOf(emptyList<Uri>())
    override var selectedItems: List<Uri>
        get() = _selectedItems
        set(value) {
            _selectedItems = value
            viewModelScope.launch {
                loadImagesAndReconcile()
            }
        }

    override var selectedImages by mutableStateOf(emptyList<FeedPostPhotoItem>())
    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)
    override var isUploading: Boolean by mutableStateOf(false)

    // MARK: - Functions
    override fun fetchFeedUser() {
        viewModelScope.launch {
            try {
                userUseCase.fetchFeedUser()
                feedUser = userUseCase.feedUser
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Fetch failed", e)
            }
        }
    }

    override suspend fun writePost() {
        val uploadedImages = coroutineScope {
            selectedImages.mapIndexed { idx, item ->
            async {
                val image = feedImageUseCase.uploadPostImage(item)
                idx to image
            }
        }.awaitAll()
        }.sortedBy { it.first }
            .map { it.second }

        val request = FeedCreatePost(
            content = text,
            isAnonymous = selectedComposeType == ComposeType.Anonymously,
            images = uploadedImages
        )
        feedPostUseCase.writePost(request)
    }

    override suspend fun submitPost(): Boolean {
        isUploading = true
        return try {
            writePost()
            analyticsService.logEvent(
                FeedPostComposeViewEvent.PostSubmitted(
                    isAnonymous = (selectedComposeType == ComposeType.Anonymously),
                    imageCount = selectedImages.size
                )
            )
            true
        } catch (e: Exception) {
            handleException(e)
            alertState = AlertState(
                titleResId = R.string.unexpected_error_uploading_post,
                message = e.localizedMessage ?: "Unknown error"
            )
            isAlertPresented = true
            false
        } finally {
            isUploading = false
        }
    }

    private suspend fun loadImagesAndReconcile() {
        val loaded = coroutineScope {
            selectedItems.mapIndexed { idx, uri ->
                async {
                    val id = UUID.randomUUID().toString()
                    val image = loadBitmapFromUri(uri)
                    idx to image?.let {
                        FeedPostPhotoItem(
                            id,
                            it,
                            spoiler = false,
                            description = ""
                        )
                    }
                }
            }.awaitAll()
        }.sortedBy { it.first }
            .mapNotNull { it.second }

        selectedImages = reconcile(new = loaded, current = selectedImages)
    }

    private fun reconcile(
        new: List<FeedPostPhotoItem>,
        current: List<FeedPostPhotoItem>
    ): List<FeedPostPhotoItem> {
        val byId = current.associateBy { it.id }
        return new.map { fresh ->
            byId[fresh.id]?.let { old ->
                fresh.copy(
                    spoiler = old.spoiler,
                    description = old.description
                )
            } ?: fresh
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    val stream = context.contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            }
        }

    override fun removeImage(index: Int) {
        val mutable = selectedImages.toMutableList()
        mutable.removeAt(index)
        selectedImages = mutable.toList()
        _selectedItems = _selectedItems.toMutableList().apply { removeAt(index) }
    }

    override fun handleException(error: Throwable) {
        crashlyticsService.recordException(error)
    }
}
