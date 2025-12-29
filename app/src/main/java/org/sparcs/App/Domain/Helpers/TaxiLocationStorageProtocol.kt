package org.sparcs.App.Domain.Helpers

import org.sparcs.App.Domain.Models.Taxi.TaxiLocation

interface TaxiLocationStorageProtocol {
    var taxiLocations: List<TaxiLocation>
    fun setLocation(locations: List<TaxiLocation>)
    fun queryLocation(query: String): List<TaxiLocation>
}