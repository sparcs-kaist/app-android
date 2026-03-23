package org.sparcs.soap.App.Domain.Models.OTL

data class Department(
    val id: Int,
    val name: String
) {
    fun toModel(): Department {
        return Department(
            id = id,
            name = name
        )
    }
}