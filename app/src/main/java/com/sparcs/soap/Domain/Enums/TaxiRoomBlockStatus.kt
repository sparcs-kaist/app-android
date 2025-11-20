package com.sparcs.soap.Domain.Enums

sealed class TaxiRoomBlockStatus {
    data object Allow : TaxiRoomBlockStatus()
    data object NotPaid : TaxiRoomBlockStatus()
    data object TooManyRooms : TaxiRoomBlockStatus()
    data class Error(val errorMessage: String) : TaxiRoomBlockStatus()
}
