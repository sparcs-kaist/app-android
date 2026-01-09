package org.sparcs.App.Shared.ViewModelMocks.Feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MultipartBody
import org.sparcs.App.Domain.Models.Feed.FeedUser
import org.sparcs.App.Features.Settings.Feed.FeedSettingsViewModel
import org.sparcs.App.Features.Settings.Feed.FeedSettingsViewModelProtocol

class MockFeedSettingsViewModel(initialState: FeedSettingsViewModel.ViewState) :
    FeedSettingsViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<FeedSettingsViewModel.ViewState> = _state

    override var nickname by mutableStateOf("테스트유저")
    override var user by mutableStateOf<FeedUser?>(
        FeedUser(
            id = "test-id",
            nickname = "테스트유저",
            profileImageURL = null,
            karma = 100
        )
    )

    override var karma by mutableIntStateOf(100)

    override suspend fun fetchUser() {}

    override fun editInformation(imagePart: MultipartBody.Part?) {}

    override fun resetProfileImage() {}
}