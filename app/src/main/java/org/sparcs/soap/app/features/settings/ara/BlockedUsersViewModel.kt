package org.sparcs.soap.app.features.settings.ara

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.Event
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.ara.AraBlockedUser
import org.sparcs.soap.app.domain.repositories.ara.AraUserRepositoryProtocol
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.shared.extensions.toAlertState
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import javax.inject.Inject

interface BlockedUsersViewModelProtocol {
    val state: BlockedUsersViewModel.ViewState
    val isRefreshing: Boolean
    var alertState: AlertState?
    var isAlertPresented: Boolean
    fun load()
    fun refresh()
    fun unblock(user: AraBlockedUser)
}

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val araUserRepository: AraUserRepositoryProtocol,
    private val analyticsService: AnalyticsServiceProtocol = MockAnalyticsService(),
) : ViewModel(), BlockedUsersViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data class Loaded(val users: List<AraBlockedUser>) : ViewState
        data class Error(val error: Exception) : ViewState
    }

    override var state: ViewState by mutableStateOf(ViewState.Loading)
        private set
    override var isRefreshing: Boolean by mutableStateOf(false)
        private set
    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun load() {
        if (state is ViewState.Error) state = ViewState.Loading
        viewModelScope.launch { fetch() }
    }

    override fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            fetch()
            isRefreshing = false
        }
    }

    private suspend fun fetch() {
        try {
            state = ViewState.Loaded(araUserRepository.fetchBlockedUsers())
        } catch (e: Exception) {
            if (state !is ViewState.Loaded) state = ViewState.Error(e)
        }
    }

    override fun unblock(user: AraBlockedUser) {
        val before = (state as? ViewState.Loaded)?.users ?: return
        state = ViewState.Loaded(before.filterNot { it.id == user.id })
        viewModelScope.launch {
            try {
                araUserRepository.unblockUser(user.id)
                analyticsService.logEvent(BlockedUsersViewEvent.UserUnblocked)
            } catch (e: Exception) {
                restore(user, before)
                alertState = e.toAlertState(R.string.error_unblock_user)
                isAlertPresented = true
            }
        }
    }

    private fun restore(user: AraBlockedUser, before: List<AraBlockedUser>) {
        val current = (state as? ViewState.Loaded)?.users ?: return
        if (current.any { it.id == user.id }) return
        val currentIds = current.map { it.id }.toSet()
        val restored = before.filter { it.id == user.id || it.id in currentIds }
        val added = current.filterNot { shown -> restored.any { it.id == shown.id } }
        state = ViewState.Loaded(restored + added)
    }
}

sealed class BlockedUsersViewEvent : Event {
    data object UserUnblocked : BlockedUsersViewEvent()

    override val source: String = "BlockedUsersView"

    override val name: String
        get() = when (this) {
            UserUnblocked -> "blocked_user_unblocked"
        }

    override val parameters: Map<String, Any>
        get() = mapOf("source" to source)
}
