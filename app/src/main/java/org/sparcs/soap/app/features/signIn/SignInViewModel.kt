package org.sparcs.soap.app.features.signIn

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.ChannelManager
import org.sparcs.soap.app.domain.error.auth.AuthUseCaseError
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.shared.extensions.toAlertState
import javax.inject.Inject

interface SignInViewModelProtocol {
    var isLoading: Boolean

    val alertState: AlertState?
    var isAlertPresented: Boolean

    fun signIn(activity: Activity)
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
) : ViewModel(), SignInViewModelProtocol {

    override var isLoading by mutableStateOf(false)

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun signIn(activity: Activity) {
        viewModelScope.launch {
            isLoading = true
            try {
                authUseCase.signIn(activity)
                userUseCase.fetchUsers()
                ChannelManager.updateProfile(
                    name = userUseCase.taxiUser?.name,
                    email = userUseCase.taxiUser?.email,
                    mobile = userUseCase.taxiUser?.phoneNumber,
                    memberId = userUseCase.feedUser?.id
                )
            } catch (e: Exception) {
                val authError = e as? AuthUseCaseError
                alertState = e.toAlertState(
                    authError?.messageRes ?: R.string.unexpected_error
                )
                isAlertPresented = true
            } finally {
                isLoading = false
            }
        }
    }
}