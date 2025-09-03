package com.example.soap.Shared.Mocks

import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Domain.Models.Taxi.TaxiChatGroup
import com.example.soap.Shared.Extensions.toISO8601
import java.net.URL
import java.util.Calendar
import java.util.Date

fun TaxiChat.Companion.mock(): TaxiChat {
    return TaxiChat.mockList()[0]
}

fun TaxiChat.Companion.mockList(): List<TaxiChat>{
    val baseDate = Date()
    val roomID = "mock-room-id"

    return listOf(
        // Alice enters first
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.ENTRANCE,
            authorID = "user1",
            authorName = "Alice",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "",
            time = Date(baseDate.time - 3600_000), // 1 hour ago
            isValid = true,
            inOutNames = listOf("Alice")
        ),
        // Alice's first message
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user1",
            authorName = "Alice",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "Hello! Anyone want to share a taxi?",
            time = Date(baseDate.time - 3500_000),
            isValid = true,
            inOutNames = emptyList()
        ),
        // Bob enters
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.ENTRANCE,
            authorID = "user2",
            authorName = "Bob",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
            authorIsWithdrew = false,
            content = "",
            time = Date(baseDate.time - 3400_000),
            isValid = true,
            inOutNames = listOf("Bob")
        ),
        // Bob's response
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user2",
            authorName = "Bob",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
            authorIsWithdrew = false,
            content = "Yes! I'd like to join too",
            time = Date(baseDate.time - 3350_000),
            isValid = true,
            inOutNames = emptyList()
        ),
        // Alice's follow-up
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user1",
            authorName = "Alice",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "Great! What time should we leave?",
            time = Date(baseDate.time - 3200_000),
            isValid = true,
            inOutNames = emptyList()
        ),
        // Bob's suggestion
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user2",
            authorName = "Bob",
            authorProfileURL =URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
            authorIsWithdrew = false,
            content = "How about 2 PM?",
            time = Date(baseDate.time - 3100_000),
            isValid = true,
            inOutNames = emptyList()
        ),
        // Charlie enters
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.ENTRANCE,
            authorID = "user3",
            authorName = "Charlie",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "",
            time = Date(baseDate.time - 2800_000),
            isValid = true,
            inOutNames = listOf("Charlie")
        ),
        // Charlie's message
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user3",
            authorName = "Charlie",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "Mind if I join? I'm going to the same destination",
            time = Date(baseDate.time - 2750_000),
            isValid = true,
            inOutNames = emptyList()
        ),
        // Account sharing
        // TaxiChat(
        //     roomID = roomID,
        //     type = TaxiChat.ChatType.ACCOUNT,
        //     authorID = "user1",
        //     authorName = "Alice",
        //     authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
        //     authorIsWithdrew = false,
        //     content = "Shared payment account info",
        //     time = Date(baseDate.time - 2600_000),
        //     isValid = true,
        //     inOutNames = emptyList()
        // ),

        // Someone leaves
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.EXIT,
            authorID = "user3",
            authorName = "Charlie",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "",
            time = Date(baseDate.time - 2400_000),
            isValid = true,
            inOutNames = listOf("Charlie")
        ),

        // Final conversation before departure
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user1",
            authorName = "Alice",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "Alright, let's meet at the pickup location",
            time = Date(baseDate.time - 2000_000),
            isValid = true,
            inOutNames = emptyList()
        ),

        // Departure message
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.DEPARTURE,
            authorID = null,
            authorName = null,
            authorProfileURL = null,
            authorIsWithdrew = null,
            content = "",
            time = Date(baseDate.time - 1800_000),
            isValid = true,
            inOutNames = emptyList()
        ),

        // During ride
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user1",
            authorName = "Alice",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "We've departed!",
            time = Date(baseDate.time - 1700_000),
            isValid = true,
            inOutNames = emptyList()
        ),

        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user2",
            authorName = "Bob",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
            authorIsWithdrew = false,
            content = "Traffic is lighter than expected, we should arrive early",
            time = Date(baseDate.time - 1200_000),
            isValid = true,
            inOutNames = emptyList()
        ),

        // Arrival message
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.ARRIVAL,
            authorID = null,
            authorName = null,
            authorProfileURL = null,
            authorIsWithdrew = null,
            content = "",
            time = Date(baseDate.time - 600_000),
            isValid = true,
            inOutNames = emptyList()
        ),

        // Settlement message
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.SETTLEMENT,
            authorID = "user2",
            authorName = "Bob",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
            authorIsWithdrew = false,
            content = "",
            time = Date(baseDate.time - 300_000),
            isValid = true,
            inOutNames = emptyList()
        ),

        // Payment message
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.PAYMENT,
            authorID = "user1",
            authorName = "Alice",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/NupjukOTL.png"),
            authorIsWithdrew = false,
            content = "",
            time = Date(baseDate.time - 180_000),
            isValid = true,
            inOutNames = emptyList()
        ),

        // Final thank you message
        TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.TEXT,
            authorID = "user2",
            authorName = "Bob",
            authorProfileURL = URL("https://sparcs-taxi-dev.s3.ap-northeast-2.amazonaws.com/profile-img/default/GooseOTL.png"),
            authorIsWithdrew = false,
            content = "Arrived safely! Thank you",
            time = Date(baseDate.time - 60_000),
            isValid = true,
            inOutNames = emptyList()
        )
    )
}

fun TaxiChatGroup.Companion.mock(): TaxiChatGroup{
    return TaxiChatGroup.mockList()[0]
}

fun TaxiChatGroup.Companion.mockList(): List<TaxiChatGroup>{
    val chats = TaxiChat.mockList()
    val currentUserID = "user1" // Alice is current user

    // Group chats by time and author (simplified grouping logic)
    val calendar = Calendar.getInstance()
    val groups = mutableListOf<TaxiChatGroup>()
    var currentGroup = mutableListOf<TaxiChat>()

    for (chat in chats) {
        if (chat.type == TaxiChat.ChatType.ENTRANCE || chat.type == TaxiChat.ChatType.EXIT) {
            // Flush current group if any
            if (currentGroup.isNotEmpty()) {
                val first = currentGroup.first()
                val time = first.time
                val isMe = first.authorID == currentUserID
                groups.add(
                    TaxiChatGroup(
                        id = time.toISO8601(),
                        chats = currentGroup.toList(),
                        lastChatID = currentGroup.lastOrNull()?.id,
                        authorID = first.authorID,
                        authorName = first.authorName,
                        authorProfileURL = first.authorProfileURL,
                        authorIsWithdrew = first.authorIsWithdrew,
                        time = time,
                        isMe = isMe,
                        isGeneral = false
                    )
                )
                currentGroup.clear()
            }

            // Add general message group
            groups.add(
                TaxiChatGroup(
                    id = chat.time.toISO8601(),
                    chats = listOf(chat),
                    lastChatID = null,
                    authorID = chat.authorID,
                    authorName = chat.authorName,
                    authorProfileURL = chat.authorProfileURL,
                    authorIsWithdrew = chat.authorIsWithdrew,
                    time = chat.time,
                    isMe = chat.authorID == currentUserID,
                    isGeneral = true
                )
            )
            continue
        }

        if (currentGroup.isEmpty()) {
            currentGroup.add(chat)
            continue
        }

        val lastChat = currentGroup.last()
        val isSameAuthor = chat.authorID == lastChat.authorID
        val isSameMinute = calendar.apply { time = chat.time }.get(Calendar.MINUTE) ==
                calendar.apply { time = lastChat.time }.get(Calendar.MINUTE)

        if (isSameAuthor && isSameMinute) {
            currentGroup.add(chat)
        } else {
            // Flush current group
            val first = currentGroup.first()
            val time = first.time
            val isMe = first.authorID == currentUserID
            groups.add(
                TaxiChatGroup(
                    id = time.toISO8601(),
                    chats = currentGroup.toList(),
                    lastChatID = currentGroup.lastOrNull()?.id,
                    authorID = first.authorID,
                    authorName = first.authorName,
                    authorProfileURL = first.authorProfileURL,
                    authorIsWithdrew = first.authorIsWithdrew,
                    time = time,
                    isMe = isMe,
                    isGeneral = false
                )
            )
            currentGroup = mutableListOf(chat)
        }
    }

    // Flush remaining group
    if (currentGroup.isNotEmpty()) {
        val first = currentGroup.first()
        val time = first.time
        val isMe = first.authorID == currentUserID
        groups.add(
            TaxiChatGroup(
                id = time.toISO8601(),
                chats = currentGroup.toList(),
                lastChatID = currentGroup.lastOrNull()?.id,
                authorID = first.authorID,
                authorName = first.authorName,
                authorProfileURL = first.authorProfileURL,
                authorIsWithdrew = first.authorIsWithdrew,
                time = time,
                isMe = isMe,
                isGeneral = false
            )
        )
    }

    return groups
}