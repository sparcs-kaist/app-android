package com.example.soap.Domain.Services

import com.example.soap.Domain.Models.Taxi.TaxiChat
import kotlinx.coroutines.flow.Flow

interface TaxiChatServiceProtocol {

    val chatsPublisher: Flow<List<TaxiChat>>

    val isConnectedPublisher: Flow<Boolean>

    val roomUpdatePublisher: Flow<String>
}
