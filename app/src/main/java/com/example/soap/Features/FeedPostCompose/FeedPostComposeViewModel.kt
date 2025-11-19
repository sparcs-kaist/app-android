package com.example.soap.Features.FeedPostCompose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Enums.FeedPostPhotoItem
import com.example.soap.Domain.Models.Feed.FeedCreatePost
import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Domain.Repositories.Feed.FeedImageRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject


interface FeedPostComposeViewModelProtocol {
    var feedUser: FeedUser?
    var text: String
    var selectedComposeType: FeedPostComposeViewModel.ComposeType
    var selectedItems: List<Uri>
    var selectedImages: List<FeedPostPhotoItem>

    suspend fun fetchFeedUser()
    suspend fun writePost()
    suspend fun loadImagesAndReconcile(context: Context)
    fun removeImage(index: Int)
}

@HiltViewModel
class FeedPostComposeViewModel @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val feedImageRepository: FeedImageRepositoryProtocol,
    private val feedPostRepository: FeedPostRepositoryProtocol,
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
        }

    override var selectedImages by mutableStateOf(listOf<FeedPostPhotoItem>())

    // MARK: - Functions
    override suspend fun fetchFeedUser() {
        viewModelScope.launch {
            userUseCase.fetchFeedUser()
            feedUser = userUseCase.feedUser
        }
    }

    override suspend fun writePost() {
        val uploadedImages = coroutineScope {
            selectedImages.mapIndexed { idx, item ->
                async {
                    val image = feedImageRepository.uploadPostImage(item)
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
        feedPostRepository.writePost(request)
    }

    private fun reconcile(
        new: List<FeedPostPhotoItem>,
        current: List<FeedPostPhotoItem>,
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

    override suspend fun loadImagesAndReconcile(context: Context) {
        val loaded = coroutineScope {
            selectedItems.mapIndexed { idx, uri ->
                async {
                    val id = UUID.randomUUID().toString()
                    val image = loadBitmapFromUri(uri, context)
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


    private suspend fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? =
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
}
