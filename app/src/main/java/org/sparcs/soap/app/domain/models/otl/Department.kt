package org.sparcs.soap.app.domain.models.otl

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