package org.sparcs.soap.App.Features.Settings.Feed

import android.util.Log
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
import okhttp3.MultipartBody
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsService
import org.sparcs.soap.App.Domain.Usecases.UserUseCase
import org.sparcs.soap.R
import retrofit2.HttpException
import javax.inject.Inject

interface FeedSettingsViewModelProtocol {
    var nickname: String
    var nicknameError: Int?
    var user: StateFlow<FeedUser?>
    val karma: Int
    val state: StateFlow<FeedSettingsViewModel.ViewState>

    suspend fun fetchUser()
    fun updateNickname()
    fun uploadProfileImage(imagePart: MultipartBody.Part)
    fun resetProfileImage()
}

@HiltViewModel
class FeedSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedUserRepository: FeedUserRepositoryProtocol,
    private val crashlyticsService: CrashlyticsService,
) : ViewModel(), FeedSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    override var nickname by mutableStateOf("")
    override var nicknameError by mutableStateOf<Int?>(null)

    private val _user = MutableStateFlow<FeedUser?>(null)
    override var user: StateFlow<FeedUser?> = _user

    override var karma by mutableIntStateOf(0)

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

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
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error(e.message ?: "Unknown Error")
        }
    }

    override fun updateNickname() {
        if (nickname == _user.value?.nickname) return

        viewModelScope.launch {
            try {
                nicknameError = null
                feedUserRepository.updateNickname(nickname)
            } catch (e: Exception) {
                nicknameError = when (e) {
                    is HttpException -> {
                        if (e.code() == 409) {
                            R.string.nickname_error_conflict
                        } else {
                            R.string.nickname_error_update_failed
                        }
                    }
                    else -> R.string.nickname_error_update_failed
                }
                Log.e("FeedSettingsViewModel", "Nickname update failed: $e")
                crashlyticsService.recordException(e)
            }
        }
    }

    override fun uploadProfileImage(imagePart: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                feedUserRepository.uploadProfileImage(imagePart)
                userUseCase.fetchFeedUser()
                _user.value = userUseCase.feedUser
            } catch (e: Exception) {
                Log.e("FeedSettingsViewModel", "Image upload failed: $e")
                crashlyticsService.recordException(e)
            }
        }
    }

    override fun resetProfileImage() {
        viewModelScope.launch {
            try {
                feedUserRepository.resetProfileImage()
                userUseCase.fetchFeedUser()
                _user.value = userUseCase.feedUser
            } catch (e: Exception) {
                Log.e("FeedSettingsViewModel", "Reset failed: $e")
                crashlyticsService.recordException(e)
            }
        }
    }
}