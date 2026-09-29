package org.sparcs.soap.buddyPreviewSupport.friends

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.Friend
import org.sparcs.soap.app.features.friends.FriendsListViewModel
import org.sparcs.soap.app.features.friends.FriendsListViewModelProtocol

class PreviewFriendsListViewModel(
    initialState: FriendsListViewModel.ViewState,
    myCode: String? = "ACD347",
    isMyCodeUnavailable: Boolean = false,
) : FriendsListViewModelProtocol {
    override val state: StateFlow<FriendsListViewModel.ViewState> = MutableStateFlow(initialState)
    override val myCode: StateFlow<String?> = MutableStateFlow(myCode)
    override val isMyCodeUnavailable: StateFlow<Boolean> = MutableStateFlow(isMyCodeUnavailable)
    override val isRefreshing: StateFlow<Boolean> = MutableStateFlow(false)
    override val isAddingFriend: StateFlow<Boolean> = MutableStateFlow(false)
    override val alertState: StateFlow<AlertState?> = MutableStateFlow(null)

    override fun load() {}
    override fun refresh() {}
    override fun loadMyCode() {}
    override fun addFriend(code: String, onComplete: () -> Unit) = onComplete()
    override fun delete(friend: Friend) {}
    override fun toggleFavorite(friend: Friend) {}
    override fun dismissAlert() {}
}
