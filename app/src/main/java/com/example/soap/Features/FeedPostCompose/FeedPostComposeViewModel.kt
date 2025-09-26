package com.example.soap.Features.FeedPostCompose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Feed.FeedComment
import com.example.soap.Domain.Models.Feed.FeedCreatePost
import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Domain.Repositories.Feed.FeedImageRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import com.example.soap.Features.FeedPost.FeedPostViewModel.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FeedPostComposeViewModel @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val feedImageRepository: FeedImageRepositoryProtocol,
    private val feedPostRepository: FeedPostRepositoryProtocol
) : ViewModel(), FeedPostComposeViewModelProtocol {

    enum class ComposeType(val value: Int) {
        PUBLICLY(0),
        ANONYMOUSLY(1)
    }

    // MARK: - Properties
    private val _feedUser = MutableStateFlow<FeedUser?>(null)
    override var feedUser: FeedUser?
        get() = _feedUser.value
        set(value) {
            _feedUser.value = value
        }

    override var text by mutableStateOf("")
    override var selectedComposeType by mutableStateOf(ComposeType.ANONYMOUSLY)

    private var _selectedItems by mutableStateOf(emptyList<Uri>())
    override var selectedItems: List<Uri>
        get() = _selectedItems
        set(value) {
            _selectedItems = value
            viewModelScope.launch { loadImagesAndReconcile() }
        }

    override var selectedImages by mutableStateOf(listOf<FeedPostPhotoItem>())

    // MARK: - Functions
    override suspend fun fetchFeedUser() {
        viewModelScope.launch {
            feedUser = userUseCase.feedUser()
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
            isAnonymous = selectedComposeType == ComposeType.ANONYMOUSLY,
            images = uploadedImages
        )
        feedPostRepository.writePost(request)
    }

    private fun reconcile(new: List<FeedPostPhotoItem>, current: List<FeedPostPhotoItem>): List<FeedPostPhotoItem> {
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

    private suspend fun loadImagesAndReconcile(context: Context) {
        val loaded = coroutineScope {
            selectedItems.mapIndexed { idx, uri ->
                async {
                    val id = UUID.randomUUID().toString()
                    val image = loadBitmapFromUri(uri, context)
                    idx to image?.let { FeedPostPhotoItem(id, it, spoiler = false, description = "") }
                }
            }.awaitAll()
        }.sortedBy { it.first }
            .mapNotNull { it.second }

        selectedImages = reconcile(new = loaded, current = selectedImages)
    }

    private suspend fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }
}
