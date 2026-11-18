package org.sparcs.soap.App.Domain.Enums.Map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class LocationCategory {
    Building,
    AcademicBuilding,
    Parking,
    Cafe,
    Gate,
    Restaurant,
    Cafeteria,
    Dormitory,
    Library,
    SportsField,
    Hospital,
    OlevStop,
    Store;

    val color: Color
        get() = when (this) {
            Building, AcademicBuilding, Library -> Color(0xFF644F29)
            Parking, OlevStop -> Color(0xFF5A86AD)
            Cafe, Restaurant, Cafeteria -> Color(0xFFE68A5C)
            Gate -> Color(0xFF8E9794)
            Dormitory -> Color(0xFF8675A9)
            SportsField -> Color(0xFF7DA67D)
            Hospital -> Color(0xFFD9534F)
            Store -> Color(0xFFEBC062)
        }

    val icon: ImageVector
        get() = when (this) {
            Building -> Icons.Default.Apartment
            AcademicBuilding -> Icons.Default.School
            Parking -> Icons.Default.LocalParking
            Cafe -> Icons.Default.LocalCafe
            Gate -> Icons.Default.DoorBack
            Restaurant, Cafeteria -> Icons.Default.Restaurant
            Dormitory -> Icons.Default.NightShelter
            Library -> Icons.AutoMirrored.Filled.MenuBook
            SportsField -> Icons.Default.SportsSoccer
            Hospital -> Icons.Default.LocalHospital
            OlevStop -> Icons.Default.DirectionsBus
            Store -> Icons.Default.ShoppingCart
        }
}