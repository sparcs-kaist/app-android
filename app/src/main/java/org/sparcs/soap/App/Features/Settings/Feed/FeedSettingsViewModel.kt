package org.sparcs.soap.App.Features.Settings.Feed

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Error.Feed.FeedProfileUseCaseError
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Services.CrashlyticsService
import org.sparcs.soap.App.Domain.Usecases.Feed.FeedProfileUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCase
import org.sparcs.soap.App.Features.Settings.Feed.ViewState.FeedProfileImageState
import org.sparcs.soap.App.Shared.Extensions.toMultipartBody
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

interface FeedSettingsViewModelProtocol {
    val state: StateFlow<FeedSettingsViewModel.ViewState>
    var nickname: String
    var nicknameError: Int?
    val profileImageURL: String?
    val profileImageState: StateFlow<FeedProfileImageState>

    var user: StateFlow<FeedUser?>
    val karma: Int
    var isUpdatingProfile: Boolean

    val alertState: AlertState?
    var isAlertPresented: Boolean

    suspend fun fetchUser()
    fun updateNickname()
    fun updateProfileImage(uri: Uri?, context: Context)
}

@HiltViewModel
class FeedSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedProfileUseCase: FeedProfileUseCaseProtocol,
    private val crashlyticsService: CrashlyticsService
) : ViewModel(), FeedSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    // MARK: - Properties
    override var nickname by mutableStateOf("")
    override var nicknameError by mutableStateOf<Int?>(null)

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    private val _user = MutableStateFlow<FeedUser?>(null)
    override var user: StateFlow<FeedUser?> = _user

    override var karma by mutableIntStateOf(0)

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override val profileImageURL: String?
        get() = _user.value?.profileImageURL

    private val _profileImageState = MutableStateFlow<FeedProfileImageState>(FeedProfileImageState.NoChange)
    override val profileImageState = _profileImageState.asStateFlow()

    override var isUpdatingProfile by mutableStateOf(false)

    override suspend fun fetchUser() {
        _state.value = ViewState.Loading
        try {
            val fetchedUser = userUseCase.feedUser
            if (fetchedUser == null) {
                _state.value = ViewState.Error("Feed User Information Not Found.")
                return
            }
            _user.value = fetchedUser
            nickname = fetchedUser.nickname
            karma = fetchedUser.karma
            _profileImageState.value = FeedProfileImageState.NoChange
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error(e.message ?: "Unknown Error")
        }
    }

    override fun updateNickname() {
        if (nickname == _user.value?.nickname) return

        viewModelScope.launch {
            isUpdatingProfile = true
            nicknameError = null
            try {
                feedProfileUseCase.updateNickname(nickname)
                userUseCase.fetchFeedUser()
            } catch (e: Exception) {
                val useCaseError = e as? FeedProfileUseCaseError
                if (useCaseError is FeedProfileUseCaseError.NicknameConflict) {
                    nicknameError = R.string.nickname_error_conflict
                } else {
                    alertState = AlertState(
                        titleResId = R.string.error,
                        messageResId = useCaseError?.messageRes
                            ?: R.string.nickname_error_update_failed
                    )
                    isAlertPresented = true
                }
                Timber.e(e, "Nickname update failed")
                crashlyticsService.recordException(e)
            } finally {
                isUpdatingProfile = false
            }
        }
    }

    override fun updateProfileImage(uri: Uri?, context: Context) {
        viewModelScope.launch {
            isUpdatingProfile = true
            try {
                val imagePart = uri?.toMultipartBody(context)
                feedProfileUseCase.updateProfileImage(imagePart)

                userUseCase.fetchFeedUser()
                _user.value = userUseCase.feedUser

                _profileImageState.value = FeedProfileImageState.Updated(uri)
            } catch (e: Exception) {
                _profileImageState.value = FeedProfileImageState.NoChange

                val useCaseError = e as? FeedProfileUseCaseError
                alertState = AlertState(
                    titleResId = R.string.error,
                    messageResId = useCaseError?.messageRes ?: R.string.error_feed_image_update_failed
                )
                isAlertPresented = true
                Timber.e(e, "Image update failed")
                crashlyticsService.recordException(e)
            } finally {
                isUpdatingProfile = false
            }
        }
    }
}