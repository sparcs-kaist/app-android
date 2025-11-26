package com.sparcs.soap.Features.SignIn

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sparcs.soap.Domain.Enums.Auth.AuthUseCaseError
import com.sparcs.soap.Domain.Usecases.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

interface SignInViewModelProtocol {
    var isLoading: Boolean
    var errorMessage: String?

    suspend fun signIn(activity: Activity)
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel(), SignInViewModelProtocol {

    override var isLoading by mutableStateOf(false)
    override var errorMessage by mutableStateOf<String?>(null)

    override suspend fun signIn(activity: Activity) {
        isLoading = true
        errorMessage = null
        try {
            authUseCase.signIn(activity)
        } catch (e: CancellationException) {
            errorMessage = "User cancelled"
            Log.e("SignInViewModel", "Sign in cancelled: $e")
            throw e
        } catch (e: AuthUseCaseError) {
            errorMessage = e.message
            Log.e("SignInViewModel", "Failed to sign in: $e")
            throw e
        } catch (e: Exception) {
            errorMessage = e.message
            Log.e("SignInViewModel", "Unexpected error: $e")
            throw e
        } finally {
            isLoading = false
        }
    }
}