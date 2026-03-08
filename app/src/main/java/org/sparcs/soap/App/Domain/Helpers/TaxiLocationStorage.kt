package org.sparcs.soap.App.Domain.Helpers

import org.sparcs.soap.App.Domain.Models.Taxi.TaxiLocation

interface TaxiLocationStorageProtocol {
    var taxiLocations: List<TaxiLocation>
    fun setLocation(locations: List<TaxiLocation>)
    fun queryLocation(query: String): List<TaxiLocation>
}

class TaxiLocationStorage : TaxiLocationStorageProtocol {
    override var taxiLocations: List<TaxiLocation> = emptyList()

    override fun setLocation(locations: List<TaxiLocation>) {
        taxiLocations = locations
    }

    override fun queryLocation(query: String): List<TaxiLocation> {
        return taxiLocations.filter { it.titleContains(query) }
    }
}