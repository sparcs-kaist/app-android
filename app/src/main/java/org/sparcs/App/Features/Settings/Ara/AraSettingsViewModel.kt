package org.sparcs.App.Features.Settings.Ara

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.sparcs.App.Domain.Models.Ara.AraUser
import org.sparcs.App.Domain.Usecases.UserUseCase
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

interface AraSettingsViewModelProtocol {
    val state: StateFlow<AraSettingsViewModel.ViewState>
    var user: StateFlow<AraUser?>
    var allowNSFW: Boolean
    var allowPolitical: Boolean
    var nickname: String
    val nicknameUpdatable: Boolean
    val nicknameUpdatableFrom: Date?

    suspend fun fetchUser() {}

    @Throws(Exception::class)
    suspend fun updateNickname() {
    }

    suspend fun updateContentPreference() {}
}


@HiltViewModel
class AraSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
) : ViewModel(), AraSettingsViewModelProtocol {
    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    private val _user = MutableStateFlow<AraUser?>(null)
    override var user: StateFlow<AraUser?> = _user

    override var allowNSFW by mutableStateOf(false)
    override var allowPolitical by mutableStateOf(false)

    override var nickname by mutableStateOf("")

    override val nicknameUpdatable: Boolean
        get() = nicknameUpdatableFrom?.let { it <= Date() } ?: true

    override val nicknameUpdatableFrom: Date?
        get() = _user.value?.nicknameUpdatedAt?.let {
            Calendar.getInstance().apply { time = it; add(Calendar.MONTH, 3) }.time
        }

    override suspend fun fetchUser() {
        _state.value = ViewState.Loading
        try {
            val fetchedUser = userUseCase.araUser
            if (fetchedUser == null) {
                _state.value = ViewState.Error("Ara User Information Not Found.")
                return
            }

            _user.value = fetchedUser
            allowNSFW = fetchedUser.allowNSFW
            allowPolitical = fetchedUser.allowPolitical
            nickname = fetchedUser.nickname
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error("Failed to fetch user: ${e.message}")
            Log.e("AraSettingsViewModel", "Failed to fetch user", e)
        }
    }

    override suspend fun updateNickname() {
        viewModelScope.launch {
            try {
                userUseCase.updateAraUser(mapOf("nickname" to nickname))
            } catch (e: Exception) {
                _state.value = ViewState.Error("Failed to update nickname: ${e.message}")
                Log.e("AraSettingsViewModel", "Failed to update nickname", e)
            }
        }
    }

    override suspend fun updateContentPreference() {
        viewModelScope.launch {
            try {
                userUseCase.updateAraUser(
                    mapOf(
                        "see_sexual" to allowNSFW,
                        "see_social" to allowPolitical
                    )
                )
            } catch (e: Exception) {
                _state.value = ViewState.Error("Failed to update content preference: ${e.message}")
                Log.e("AraSettingsViewModel", "Failed to update content preference", e)
            }
        }
    }
}
