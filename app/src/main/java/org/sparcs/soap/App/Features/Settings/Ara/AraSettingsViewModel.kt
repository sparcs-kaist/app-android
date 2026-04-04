package org.sparcs.soap.App.Features.Settings.Ara

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Ara.AraUser
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.R
import timber.log.Timber
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
    fun updateNickname() {
    }

    fun updateContentPreference() {}
}


@HiltViewModel
class AraSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
) : ViewModel(), AraSettingsViewModelProtocol {
    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val error: Exception, val resId: Int? = null) : ViewState()
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
                _state.value = ViewState.Error(
                    NoSuchElementException(),
                    resId = R.string.error_ara_user_not_found
                )
                return
            }

            _user.value = fetchedUser
            allowNSFW = fetchedUser.allowNSFW
            allowPolitical = fetchedUser.allowPolitical
            nickname = fetchedUser.nickname
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
            Timber.e(e, "Failed to fetch user")
        }
    }

    override fun updateNickname() {
        viewModelScope.launch {
            try {
                userUseCase.updateAraUser(mapOf("nickname" to nickname))
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
                Timber.e(e, "Failed to update nickname")
            }
        }
    }

    override fun updateContentPreference() {
        viewModelScope.launch {
            try {
                userUseCase.updateAraUser(
                    mapOf(
                        "see_sexual" to allowNSFW,
                        "see_social" to allowPolitical
                    )
                )
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
                Timber.e(e, "Failed to update content preference")
            }
        }
    }
}
