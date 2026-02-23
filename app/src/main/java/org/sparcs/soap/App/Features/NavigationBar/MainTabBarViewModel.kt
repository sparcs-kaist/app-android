package org.sparcs.soap.App.Features.NavigationBar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.DeepLink
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Repositories.Ara.AraBoardRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.R
import javax.inject.Inject

@HiltViewModel
class MainTabBarViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    private val araBoardUseCase: AraBoardRepositoryProtocol
) : ViewModel() {

    var alertState by mutableStateOf<AlertState?>(null)
    var isAlertPresented by mutableStateOf(false)

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var pendingDeepLink: DeepLink? = null

    fun handleDeepLink(deepLink: DeepLink, isAuthenticated: Boolean) {
        if (isAuthenticated) {
            executeDeepLink(deepLink)
        } else {
            pendingDeepLink = deepLink
        }
    }

    fun checkPendingDeepLink(isAuthenticated: Boolean) {
        if (isAuthenticated && pendingDeepLink != null) {
            executeDeepLink(pendingDeepLink!!)
            pendingDeepLink = null
        }
    }

    private fun executeDeepLink(deepLink: DeepLink) {
        when (deepLink) {
            is DeepLink.TaxiInvite -> resolveInvite(deepLink.code)
            is DeepLink.AraPost -> resolvePost(deepLink.id)
        }
    }

    fun resolveInvite(code: String) {
        viewModelScope.launch {
            try {
                val room = taxiRoomRepository.getPublicRoom(code)
                _navigationEvent.emit(Channel.Taxi.name + "?roomId=${room.id}")
            } catch (e: Exception) {
                showError(R.string.invalid_invitation_title, R.string.invalid_invitation_message)
            }
        }
    }

    fun resolvePost(id: Int) {
        viewModelScope.launch {
            try {
                val post = araBoardUseCase.fetchPost(origin = null, postID = id)
                _navigationEvent.emit(Channel.PostView.name + "?postId=${post.id}")
            } catch (e: Exception) {
                showError(R.string.post_not_found_title, R.string.post_not_found_message)
            }
        }
    }

    private fun showError(title: Int, message: Int) {
        alertState = AlertState(titleResId = title, messageResId = message)
        isAlertPresented = true
    }
}