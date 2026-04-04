package org.sparcs.soap.App.Shared.ViewModelMocks

import android.app.Activity
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Features.SignIn.SignInViewModelProtocol

class MockSignInViewModel : SignInViewModelProtocol {

    override var isLoading: Boolean = false

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override fun signIn(activity: Activity) {}
}
