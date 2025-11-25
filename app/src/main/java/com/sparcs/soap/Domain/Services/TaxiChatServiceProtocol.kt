package com.sparcs.soap.Domain.Services

import com.sparcs.soap.Domain.Models.Taxi.TaxiChat
import kotlinx.coroutines.flow.Flow

interface TaxiChatServiceProtocol {

    val chatsPublisher: Flow<List<TaxiChat>>

    val isConnectedPublisher: Flow<Boolean>

    val roomUpdatePublisher: Flow<String>
}
