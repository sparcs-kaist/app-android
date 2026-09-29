package org.sparcs.soap.app.features.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.helpers.FriendCode
import org.sparcs.soap.app.domain.models.otl.Friend
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.otl.FriendUseCaseProtocol
import org.sparcs.soap.app.features.friends.event.FriendsListViewEvent
import org.sparcs.soap.app.shared.extensions.toAlertState
import java.text.Collator
import javax.inject.Inject

interface FriendsListViewModelProtocol {
    val state: StateFlow<FriendsListViewModel.ViewState>

    /**
     * The user's own invite code, or `null` while loading / when the endpoint
     * is unavailable (it 404s on production for now).
     */
    val myCode: StateFlow<String?>
    val isMyCodeUnavailable: StateFlow<Boolean>

    /** A pull-to-refresh reload is in flight. */
    val isRefreshing: StateFlow<Boolean>

    /** An add-by-code request is in flight. */
    val isAddingFriend: StateFlow<Boolean>
    val alertState: StateFlow<AlertState?>

    fun load()
    fun refresh()
    fun loadMyCode()
    fun addFriend(code: String, onComplete: () -> Unit = {})
    fun delete(friend: Friend)
    fun toggleFavorite(friend: Friend)
    fun dismissAlert()
}

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val friendUseCase: FriendUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
) : ViewModel(), FriendsListViewModelProtocol {

    sealed interface ViewState {
        data object Loading : ViewState
        data class Loaded(val friends: List<Friend>) : ViewState
        data object Empty : ViewState
        data class Error(val error: Exception) : ViewState
    }

    // MARK: - State
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _myCode = MutableStateFlow<String?>(null)
    override val myCode: StateFlow<String?> = _myCode.asStateFlow()

    private val _isMyCodeUnavailable = MutableStateFlow(false)
    override val isMyCodeUnavailable: StateFlow<Boolean> = _isMyCodeUnavailable.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    override val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isAddingFriend = MutableStateFlow(false)
    override val isAddingFriend: StateFlow<Boolean> = _isAddingFriend.asStateFlow()

    private val _alertState = MutableStateFlow<AlertState?>(null)
    override val alertState: StateFlow<AlertState?> = _alertState.asStateFlow()

    // MARK: - Loading
    override fun load() {
        viewModelScope.launch { reload() }
    }

    override fun refresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                reload()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    override fun loadMyCode() {
        viewModelScope.launch {
            try {
                _myCode.value = friendUseCase.fetchMyCode()
                _isMyCodeUnavailable.value = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // The code endpoint is not yet available everywhere; fail quietly
                // and let the view fall back rather than surfacing an alert.
                crashlyticsService.recordException(e)
                _myCode.value = null
                _isMyCodeUnavailable.value = true
            }
        }
    }

    // MARK: - Mutations
    override fun addFriend(code: String, onComplete: () -> Unit) {
        val normalized = FriendCode.normalized(code) ?: return
        if (_isAddingFriend.value) return
        _isAddingFriend.value = true
        viewModelScope.launch {
            try {
                mutate(FriendsListViewEvent.FriendAdded) { friendUseCase.addFriend(normalized) }
            } finally {
                _isAddingFriend.value = false
            }
            // Success or failure (the alert shows on the list), but not on cancellation.
            onComplete()
        }
    }

    override fun delete(friend: Friend) {
        viewModelScope.launch {
            mutate(FriendsListViewEvent.FriendDeleted) { friendUseCase.deleteFriend(friend.id) }
        }
    }

    override fun toggleFavorite(friend: Friend) {
        viewModelScope.launch {
            mutate(FriendsListViewEvent.FavoriteToggled) {
                friendUseCase.setFavorite(friend.id, !friend.isFavorite)
            }
        }
    }

    override fun dismissAlert() {
        _alertState.value = null
    }

    // MARK: - Helpers
    private suspend fun reload() {
        try {
            val list = friendUseCase.fetchFriends()
            apply(list.friends)
            analyticsService.logEvent(FriendsListViewEvent.FriendsLoaded)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashlyticsService.recordException(e)
            _state.value = ViewState.Error(e)
        }
    }

    private suspend fun mutate(event: FriendsListViewEvent, action: suspend () -> Unit) {
        try {
            action()
            analyticsService.logEvent(event)
            reload()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashlyticsService.recordException(e)
            _alertState.value = e.toAlertState(R.string.error_unknown_try_again)
                .copy(titleResId = R.string.friends_error_title)
        }
    }

    /**
     * Favourites float to the top, then everyone is ordered by name so the
     * list stays stable across reloads.
     */
    private fun apply(friends: List<Friend>) {
        if (friends.isEmpty()) {
            _state.value = ViewState.Empty
            return
        }
        val collator = Collator.getInstance().apply { strength = Collator.SECONDARY }
        val sorted = friends.sortedWith(
            compareByDescending<Friend> { it.isFavorite }.thenComparator { lhs, rhs ->
                collator.compare(lhs.name, rhs.name)
            }
        )
        _state.value = ViewState.Loaded(sorted)
    }
}
