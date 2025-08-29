package com.example.soap.Domain.Usecases

import android.graphics.Bitmap
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Domain.Models.Taxi.TaxiChatGroup
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TaxiChatUseCaseProtocol {
    val groupedChatsFlow: Flow<List<TaxiChatGroup>>
    val roomUpdateFlow: Flow<TaxiRoom>

    suspend fun fetchInitialChats()
    suspend fun fetchChats(before: Date)
    suspend fun sendChat(content: String?, type: TaxiChat.ChatType)
    suspend fun sendImage(content: Bitmap)
}