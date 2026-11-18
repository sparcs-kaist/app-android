package org.sparcs.soap.App.Domain.Enums.Map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class MapTab(val title: String) {
    Nearby("Nearby"),
    Classrooms("Classrooms"),
    Olev("OLEV"),

    Search("Search");

    val icon: ImageVector
        get() = when (this) {
            Nearby -> Icons.Default.LocationSearching
            Classrooms -> Icons.Default.MenuBook
            Olev -> Icons.Default.DirectionsBus
            Search -> Icons.Default.Search
        }
}