package org.sparcs.soap.BuddyPreviewSupport.Feed

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Features.Settings.Feed.FeedSettingsViewModel
import org.sparcs.soap.App.Features.Settings.Feed.FeedSettingsViewModelProtocol
import org.sparcs.soap.App.Features.Settings.Feed.ViewState.FeedProfileImageState

class PreviewFeedSettingsViewModel(
    state: FeedSettingsViewModel.ViewState = FeedSettingsViewModel.ViewState.Loaded,
) : FeedSettingsViewModelProtocol {
    // MARK: - Properties
    override var nickname by mutableStateOf("NICKNAME")
    override var nicknameError by mutableStateOf<Int?>(null)

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    private val _user = MutableStateFlow<FeedUser?>(null)
    override var user: StateFlow<FeedUser?> = _user

    override var karma by mutableIntStateOf(100)

    private val _state =
        MutableStateFlow<FeedSettingsViewModel.ViewState>(FeedSettingsViewModel.ViewState.Loaded)
    override val state: StateFlow<FeedSettingsViewModel.ViewState> = _state.asStateFlow()

    override val profileImageURL: String? = null

    private val _profileImageState =
        MutableStateFlow<FeedProfileImageState>(FeedProfileImageState.NoChange)
    override val profileImageState = _profileImageState.asStateFlow()

    override var isUpdatingProfile by mutableStateOf(false)

    // MARK: - Functions
    override suspend fun fetchUser() {
    }

    override fun updateNickname() {
    }

    override fun updateProfileImage(uri: Uri?, context: Context) {
    }
}