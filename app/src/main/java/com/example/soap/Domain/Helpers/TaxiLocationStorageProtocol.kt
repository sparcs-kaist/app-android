package com.example.soap.Domain.Helpers

import com.example.soap.Domain.Models.Taxi.TaxiLocation

interface TaxiLocationStorageProtocol {
    var taxiLocations: List<TaxiLocation>
    fun setLocation(locations: List<TaxiLocation>)
    fun queryLocation(query: String): List<TaxiLocation>
}