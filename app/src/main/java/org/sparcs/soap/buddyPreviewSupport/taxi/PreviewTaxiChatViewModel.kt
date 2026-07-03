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
import org.sparcs.soap.app.domain.models.taxi.ChatBubblePosition
import org.sparcs.soap.app.domain.models.taxi.ChatRenderItem
import org.sparcs.soap.app.domain.models.taxi.MetadataVisibility
import org.sparcs.soap.app.domain.models.taxi.SenderInfo
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.taxiChat.TaxiChatViewModel
import org.sparcs.soap.app.features.taxiChat.TaxiChatViewModelProtocol
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
        private val mockRenderItems: List<ChatRenderItem> by lazy {
            val chats = TaxiChat.mockList()

            fun sender(chat: TaxiChat, isMine: Boolean) = SenderInfo(
                id = chat.authorID,
                name = chat.authorName,
                avatarURL = chat.authorProfileURL,
                isMine = isMine,
                isWithdrew = chat.authorIsWithdrew ?: false
            )

            fun message(
                index: Int,
                position: ChatBubblePosition,
                showName: Boolean,
                showAvatar: Boolean,
                showTime: Boolean,
                isMine: Boolean,
            ): ChatRenderItem {
                val chat = chats[index]
                return ChatRenderItem.Message(
                    id = chat.id.toString(),
                    chat = chat,
                    kind = chat.type,
                    sender = sender(chat, isMine),
                    position = position,
                    metadata = MetadataVisibility(
                        showName = showName,
                        showAvatar = showAvatar,
                        showTime = showTime
                    )
                )
            }

            listOf(
                ChatRenderItem.DaySeparator(chats[0].time),
                ChatRenderItem.SystemEvent(id = chats[0].id.toString(), chat = chats[0]),
                message(
                    1, ChatBubblePosition.TOP, false,
                    showAvatar = false,
                    showTime = false,
                    isMine = true
                ),
                message(
                    2, ChatBubblePosition.MIDDLE, false,
                    showAvatar = false,
                    showTime = false,
                    isMine = true
                ),
                message(
                    3, ChatBubblePosition.BOTTOM, false,
                    showAvatar = false,
                    showTime = true,
                    isMine = true
                ),
                ChatRenderItem.SystemEvent(id = chats[4].id.toString(), chat = chats[4]),
                message(
                    5, ChatBubblePosition.SINGLE, true,
                    showAvatar = true,
                    showTime = true,
                    isMine = false
                ),
                message(
                    6, ChatBubblePosition.SINGLE, false,
                    showAvatar = true,
                    showTime = true,
                    isMine = true
                ),
                message(
                    7, ChatBubblePosition.SINGLE, true,
                    showAvatar = true,
                    showTime = true,
                    isMine = false
                ),
                ChatRenderItem.SystemEvent(id = chats[8].id.toString(), chat = chats[8]),
                message(
                    9, ChatBubblePosition.SINGLE, true,
                    showAvatar = true,
                    showTime = true,
                    isMine = false
                ),
                ChatRenderItem.SystemEvent(id = chats[10].id.toString(), chat = chats[10]),
                message(
                    11, ChatBubblePosition.SINGLE, false,
                    showAvatar = true,
                    showTime = true,
                    isMine = true
                ),
                message(
                    12, ChatBubblePosition.SINGLE, false,
                    showAvatar = false,
                    showTime = true,
                    isMine = false
                ),
                message(
                    13, ChatBubblePosition.SINGLE, false,
                    showAvatar = true,
                    showTime = true,
                    isMine = true
                ),
                message(
                    14, ChatBubblePosition.SINGLE, true,
                    showAvatar = true,
                    showTime = true,
                    isMine = false
                ),
                message(
                    15, ChatBubblePosition.SINGLE, false,
                    showAvatar = false,
                    showTime = true,
                    isMine = false
                ),
                message(
                    16, ChatBubblePosition.SINGLE, false,
                    showAvatar = false,
                    showTime = true,
                    isMine = false
                ),
                message(
                    17, ChatBubblePosition.SINGLE, false,
                    showAvatar = false,
                    showTime = true,
                    isMine = true
                ),
                message(
                    18, ChatBubblePosition.SINGLE, true,
                    showAvatar = true,
                    showTime = true,
                    isMine = false
                )
            )
        }
    }
}