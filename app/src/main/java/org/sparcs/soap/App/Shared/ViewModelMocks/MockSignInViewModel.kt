package org.sparcs.soap.App.Shared.ViewModelMocks

import android.app.Activity
import org.sparcs.soap.App.Features.SignIn.SignInViewModelProtocol

class MockSignInViewModel : SignInViewModelProtocol {

    override var isLoading: Boolean = false
    override suspend fun signIn(activity: Activity) {}
}
