package org.sparcs.soap.buddyPreviewSupport.feed

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.feed.FeedUser
import org.sparcs.soap.app.features.settings.feed.FeedSettingsViewModel
import org.sparcs.soap.app.features.settings.feed.FeedSettingsViewModelProtocol
import org.sparcs.soap.app.features.settings.feed.viewState.FeedProfileImageState

class PreviewFeedSettingsViewModel(
    initialState: FeedSettingsViewModel.ViewState = FeedSettingsViewModel.ViewState.Loaded,
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

    override fun updateNickname(onComplete: (Boolean) -> Unit) {
    }

    override fun updateProfileImage(uri: Uri?, context: Context) {
    }
}