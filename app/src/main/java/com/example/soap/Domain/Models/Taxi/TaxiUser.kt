package com.example.soap.Domain.Models.Taxi

import java.net.URL
import java.util.Date

data class TaxiUser(
    val id: String,
    val oid: String,
    val name: String,
    val nickname: String,
    val phoneNumber: String?,
    val email: String,
    val withdraw: Boolean,
    val ban: Boolean,
    val agreeOnTermsOfService: Boolean,
    val joinedAt: Date,
    val profileImageURL: URL?,
    val account: String
){
    companion object {}
}