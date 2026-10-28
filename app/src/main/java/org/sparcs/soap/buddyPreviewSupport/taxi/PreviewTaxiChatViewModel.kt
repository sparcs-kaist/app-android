package org.sparcs.soap.buddyPreviewSupport.taxi

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.taxi.ChatRenderItem
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.taxiChat.TaxiChatViewModel
import org.sparcs.soap.app.features.taxiChat.TaxiChatViewModelProtocol
import org.sparcs.soap.app.features.taxiChat.components.ChatBubblePositionResolver
import org.sparcs.soap.app.features.taxiChat.components.ChatRenderItemBuilder
import org.sparcs.soap.app.features.taxiChat.components.DefaultMessagePresentationPolicy
import org.sparcs.soap.app.features.taxiChat.components.TaxiGroupingPolicy
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList

class PreviewTaxiChatViewModel(
    initialState: TaxiChatViewModel.ViewState = TaxiChatViewModel.ViewState.Loaded,
) : TaxiChatViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiChatViewModel.ViewState> = _state.asStateFlow()

    private val _renderItems = MutableStateFlow(mockRenderItems)
    override val renderItems: StateFlow<List<ChatRenderItem>> = _renderItems.asStateFlow()

    private val _taxiUser = MutableStateFlow<TaxiUser?>(TaxiUser.mock())
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser.asStateFlow()

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    private val _room = MutableStateFlow(TaxiRoom.mock())
    override val room: StateFlow<TaxiRoom> = _room.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    override val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    override val isLeaveRoomAvailable: Boolean = true
    override val isCommitSettlementAvailable: Boolean = true
    override val isCommitPaymentAvailable: Boolean = true
    override val account: String = TaxiUser.mock().account
    override val topChatID: String? = null

    override var scrollToBottomTrigger by mutableIntStateOf(0)

    override suspend fun setup() {}
    override suspend fun loadMoreChats() {}
    override suspend fun fetchInitialChats() {}
    override fun sendChat(message: String, type: TaxiChat.ChatType) {}
    override suspend fun leaveRoom(): Boolean {
        return false
    }

    override fun commitSettlement(amount: Int) {}
    override suspend fun commitPayment() {}
    override suspend fun sendImage(image: Bitmap) {}
    override fun switchRoom(newRoom: TaxiRoom) {
        _room.value = newRoom
    }

    override fun toggleCarrier(hasCarrier: Boolean) {}
    override fun updateArrival(isArrived: Boolean) {}

    companion object {
        // Built the way the app builds them, as Alice ("user1"), so sides and grouping match the app.
        private val mockRenderItems: List<ChatRenderItem> by lazy {
            ChatRenderItemBuilder(
                policy = TaxiGroupingPolicy(),
                positionResolver = ChatBubblePositionResolver(),
                presentationPolicy = DefaultMessagePresentationPolicy()
            ).build(chats = TaxiChat.mockList(), myUserID = "user1")
        }
    }
}