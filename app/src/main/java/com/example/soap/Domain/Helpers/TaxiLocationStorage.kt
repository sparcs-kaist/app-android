package com.example.soap.Domain.Helpers

import com.example.soap.Domain.Models.Taxi.TaxiLocation

class TaxiLocationStorage: TaxiLocationStorageProtocol {
    override var taxiLocations: List<TaxiLocation> = emptyList()

    override fun setLocation(locations: List<TaxiLocation>) {
        taxiLocations = locations
    }

    override fun queryLocation(query: String): List<TaxiLocation> {
        return taxiLocations.filter { it.titleContains(query) }
    }
}