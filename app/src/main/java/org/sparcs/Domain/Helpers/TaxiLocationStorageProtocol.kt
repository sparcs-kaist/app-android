package org.sparcs.Domain.Helpers

import org.sparcs.Domain.Models.Taxi.TaxiLocation

interface TaxiLocationStorageProtocol {
    var taxiLocations: List<TaxiLocation>
    fun setLocation(locations: List<TaxiLocation>)
    fun queryLocation(query: String): List<TaxiLocation>
}