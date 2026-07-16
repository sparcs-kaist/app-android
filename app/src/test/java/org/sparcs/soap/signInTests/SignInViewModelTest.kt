package org.sparcs.soap.signInTests

import android.app.Activity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.sparcs.soap.app.features.signIn.SignInViewModel
import org.sparcs.soap.buddyTestSupport.useCase.MockAuthUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class SignInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockAuthUseCase: MockAuthUseCase
    private lateinit var mockUserUseCase: MockUserUseCase
    private lateinit var viewModel: SignInViewModel
    private lateinit var activity: Activity

    @Before
    fun setup() {
        mockAuthUseCase = MockAuthUseCase()
        mockUserUseCase = MockUserUseCase()
        viewModel = SignInViewModel(mockAuthUseCase, mockUserUseCase)
        activity = Robolectric.buildActivity(Activity::class.java).get()
    }

    @Test
    fun `initial state is not loading and no alert`() {
        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.isAlertPresented)
        assertNull(viewModel.alertState)
    }

    @Test
    fun `signIn failure presents alert and stops loading`() = runTest {
        mockAuthUseCase.signInResult = Result.failure(Exception("Test failure"))

        viewModel.signIn(activity)

        assertEquals(1, mockAuthUseCase.signInCallCount)
        assertTrue(viewModel.isAlertPresented)
        assertNotNull(viewModel.alertState)
        assertFalse(viewModel.isLoading)
    }
}
