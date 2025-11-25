package com.sparcs.soap.Features.SignIn

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Usecases.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

interface SignInViewModelProtocol {
    var isLoading: Boolean
    var errorMessage: String?

    fun signIn(activity: Activity)
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel(), SignInViewModelProtocol {

    override var isLoading by mutableStateOf(false)

    override var errorMessage by mutableStateOf<String?>(null)

    override fun signIn(
        activity: Activity
    ) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                authUseCase.signIn(activity)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}