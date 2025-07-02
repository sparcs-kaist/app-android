package com.example.soap.Models

import java.util.Date

data class TaxiLocation(
    val id: String,
    val title: String,
    val priority: Double,
    val latitude: Double,
    val longitude: Double
)

data class RoomInfo(
//    val origin: TaxiLocation,
//    val destination: TaxiLocation,
    val origin: String,
    val destination: String,
    val name: String,
    val occupancy: Int,
    val capacity: Int,
    val departureTime: Date
){
    companion object { }
}