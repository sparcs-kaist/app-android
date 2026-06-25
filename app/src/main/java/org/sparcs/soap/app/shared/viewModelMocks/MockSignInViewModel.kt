package org.sparcs.soap.app.shared.viewModelMocks

import android.app.Activity
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.features.signIn.SignInViewModelProtocol

class MockSignInViewModel : SignInViewModelProtocol {

    override var isLoading: Boolean = false

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override fun signIn(activity: Activity) {}
}
