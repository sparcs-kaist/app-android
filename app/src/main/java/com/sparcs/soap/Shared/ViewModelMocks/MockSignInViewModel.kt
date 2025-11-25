package com.sparcs.soap.Shared.ViewModelMocks

import android.app.Activity
import com.sparcs.soap.Features.SignIn.SignInViewModelProtocol

class MockSignInViewModel : SignInViewModelProtocol {

    override var isLoading: Boolean = false
    override var errorMessage: String? = ""

    override fun signIn(activity: Activity) {}
}
