package org.sparcs.soap.App.Domain.Services

import kotlinx.coroutines.flow.Flow
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat

interface TaxiChatServiceProtocol {

    val chatsPublisher: Flow<List<TaxiChat>>

    val isConnectedPublisher: Flow<Boolean>

    val roomUpdatePublisher: Flow<String>
}
