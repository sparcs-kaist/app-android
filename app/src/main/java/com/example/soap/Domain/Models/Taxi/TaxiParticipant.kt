package com.example.soap.Domain.Models.Taxi

import java.net.URL
import java.util.Date

data class TaxiParticipant(
    val id: String,
    val name: String,
    val nickname: String,
    val profileImageURL: URL?,
    val withdraw: Boolean,
    val readAt: Date
)