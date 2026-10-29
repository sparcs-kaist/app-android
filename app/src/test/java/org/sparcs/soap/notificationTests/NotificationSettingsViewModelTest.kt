package org.sparcs.soap.notificationTests

import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.NotificationCategory
import org.sparcs.soap.app.domain.usecases.DeviceNotRegisteredException
import org.sparcs.soap.app.domain.usecases.MockFCMUseCase
import org.sparcs.soap.app.features.settings.notification.NotificationSettingsViewModel
import org.sparcs.soap.app.features.settings.notification.NotificationSettingsViewModel.ViewState
import org.sparcs.soap.testSupport.MainDispatcherRule

class NotificationSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = MockFCMUseCase()

    @Test
    fun `load shows this device's preferences`() {
        useCase.preferences[NotificationCategory.TAXI_EVENT] = false
        val viewModel = NotificationSettingsViewModel(useCase)

        viewModel.load()

        assertEquals(ViewState.Loaded, viewModel.state.value)
        assertEquals(false, viewModel.preferences[NotificationCategory.TAXI_EVENT])
        assertEquals(true, viewModel.preferences[NotificationCategory.TAXI_CHAT])
    }

    @Test
    fun `a device not registered for notifications shows an error`() {
        val viewModel = NotificationSettingsViewModel(object : org.sparcs.soap.app.domain.usecases.FCMUseCaseProtocol {
            override suspend fun register(fcmToken: String) {}
            override suspend fun unregister() {}
            override suspend fun notificationPreferences(): Map<NotificationCategory, Boolean> =
                throw DeviceNotRegisteredException()
            override suspend fun setNotifications(category: NotificationCategory, enabled: Boolean) {}
        })

        viewModel.load()

        assertTrue((viewModel.state.value as ViewState.Error).error is DeviceNotRegisteredException)
    }

    @Test
    fun `turning a kind off saves it`() {
        val viewModel = NotificationSettingsViewModel(useCase)
        viewModel.load()

        viewModel.setNotifications(NotificationCategory.FEED_REPLY, false)

        assertEquals(false, viewModel.preferences[NotificationCategory.FEED_REPLY])
        assertEquals(false, useCase.preferences[NotificationCategory.FEED_REPLY])
        assertFalse(viewModel.isAlertPresented)
    }

    @Test
    fun `a change that can't be saved moves the switch back and says so`() {
        val viewModel = NotificationSettingsViewModel(useCase)
        viewModel.load()
        useCase.setNotificationsError = Exception("offline")

        viewModel.setNotifications(NotificationCategory.TAXI_CHAT, false)

        assertEquals(true, viewModel.preferences[NotificationCategory.TAXI_CHAT])
        assertTrue(viewModel.isAlertPresented)
    }

    @Test
    fun `flipping a switch back while the first change fails keeps the latest`() {
        val firstSave = CompletableDeferred<Unit>()
        val saved = mutableListOf<Boolean>()
        val viewModel = NotificationSettingsViewModel(object : org.sparcs.soap.app.domain.usecases.FCMUseCaseProtocol {
            override suspend fun register(fcmToken: String) {}
            override suspend fun unregister() {}
            override suspend fun notificationPreferences(): Map<NotificationCategory, Boolean> =
                mapOf(NotificationCategory.TAXI_CHAT to true)
            override suspend fun setNotifications(category: NotificationCategory, enabled: Boolean) {
                if (saved.isEmpty() && !firstSave.isCompleted) {
                    firstSave.await()
                    throw Exception("offline")
                }
                saved += enabled
            }
        })
        viewModel.load()

        viewModel.setNotifications(NotificationCategory.TAXI_CHAT, false)
        viewModel.setNotifications(NotificationCategory.TAXI_CHAT, true)
        viewModel.setNotifications(NotificationCategory.TAXI_CHAT, false)
        firstSave.complete(Unit)

        assertEquals(false, viewModel.preferences[NotificationCategory.TAXI_CHAT])
        assertEquals(listOf(false), saved)
        assertFalse(viewModel.isAlertPresented)
    }
}
