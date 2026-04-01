package org.sparcs.soap.App.Features.SignIn

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import javax.inject.Inject

interface SignInViewModelProtocol {
    var isLoading: Boolean
    suspend fun signIn(activity: Activity)
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
) : ViewModel(), SignInViewModelProtocol {

    override var isLoading by mutableStateOf(false)

    override suspend fun signIn(activity: Activity) {
        isLoading = true
        try {
            authUseCase.signIn(activity)
            withContext(Dispatchers.IO + NonCancellable) {
                userUseCase.fetchUsers()
            }
        } finally {
            isLoading = false
        }
    }
}