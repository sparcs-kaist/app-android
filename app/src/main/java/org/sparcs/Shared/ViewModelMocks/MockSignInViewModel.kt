package org.sparcs.Shared.ViewModelMocks

import android.app.Activity
import org.sparcs.Features.SignIn.SignInViewModelProtocol

class MockSignInViewModel : SignInViewModelProtocol {

    override var isLoading: Boolean = false
    override var errorMessage: String? = ""

    override suspend fun signIn(activity: Activity) {}
}
