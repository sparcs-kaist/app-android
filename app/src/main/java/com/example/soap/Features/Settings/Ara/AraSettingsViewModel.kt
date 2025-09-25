//package com.example.soap.Features.Settings.Ara
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.soap.Domain.Usecases.UserUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import java.util.Calendar
//import java.util.Date
//import javax.inject.Inject
//
//
//@HiltViewModel
//class AraSettingsViewModel @Inject constructor(
//    private val userUseCase: UserUseCase
//) : ViewModel() {
//    sealed class ViewState {
//        data object Loading : ViewState()
//        data object Loaded : ViewState()
//        data class Error(val message: String) : ViewState()
//    }
//
//    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
//    val state: StateFlow<ViewState> = _state
//
//    private val _user = MutableStateFlow<AraUser?>(null)
//    val user: StateFlow<AraUser?> = _user
//
//    var allowNSFW: Boolean = false
//    var allowPolitical: Boolean = false
//    var nickname: String = ""
//
//    val nicknameUpdatable: Boolean
//        get() = nicknameUpdatableFrom?.let { it <= Date() } ?: false
//
//    val nicknameUpdatableFrom: Date?
//        get() = _user.value?.nicknameUpdatedAt?.let {
//            Calendar.getInstance().apply { time = it; add(Calendar.MONTH, 3) }.time
//        }
//
//    fun fetchUser() {
//        _state.value = ViewState.Loading
//        viewModelScope.launch {
//            val fetchedUser = userUseCase.getAraUser()
//            if (fetchedUser == null) {
//                _state.value = ViewState.Error("Ara User Information Not Found.")
//                return@launch
//            }
//            _user.value = fetchedUser
//            allowNSFW = fetchedUser.allowNSFW
//            allowPolitical = fetchedUser.allowPolitical
//            nickname = fetchedUser.nickname
//            _state.value = ViewState.Loaded
//        }
//    }
//
//    fun updateNickname() {
//        viewModelScope.launch {
//            try {
//                userUseCase.updateAraUser(mapOf("nickname" to nickname))
//            } catch (e: Exception) {
//                _state.value = ViewState.Error("Failed to update nickname: ${e.message}")
//            }
//        }
//    }
//
//    fun updateContentPreference() {
//        viewModelScope.launch {
//            try {
//                userUseCase.updateAraUser(
//                    mapOf(
//                        "see_sexual" to allowNSFW,
//                        "see_social" to allowPolitical
//                    )
//                )
//            } catch (e: Exception) {
//                _state.value = ViewState.Error("Failed to update content preference: ${e.message}")
//            }
//        }
//    }
//}
