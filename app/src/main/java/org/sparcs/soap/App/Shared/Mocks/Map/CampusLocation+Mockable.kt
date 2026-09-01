package org.sparcs.soap.App.Shared.Mocks.Map

import org.sparcs.soap.App.Domain.Enums.Map.LocationCategory
import org.sparcs.soap.App.Domain.Models.Map.CampusLocation


fun CampusLocation.Companion.mock(): CampusLocation {
    return mockList().first()
}

fun CampusLocation.Companion.mockList(): List<CampusLocation> {
    return listOf(
        CampusLocation("(E1) Main Gate", 36.365596, 127.363944, LocationCategory.Gate),
        CampusLocation(
            "(E2) Industrial Engineering & Management Building",
            36.367364,
            127.364392,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E2-2) Department of Industrial & Systems Engineering",
            36.367423,
            127.364022,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E3) Information & Electronics Building",
            36.368002,
            127.365033,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E3-1) School of Computing",
            36.368075,
            127.365819,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E3-2) School of Electrical Engineering",
            36.368814,
            127.365768,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E3-3) Device Innovation Facility",
            36.368954,
            127.366583,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E3-4) Saeneul Dong",
            36.369295,
            127.366283,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E3-5) KRAFTON Building",
            36.367704,
            127.36521,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E4) KAIST Institutes Building",
            36.368218,
            127.363864,
            LocationCategory.Building
        ),
        CampusLocation("(E5) Facility Club", 36.369263, 127.363563, LocationCategory.Cafeteria),
        CampusLocation(
            "(E6) Natural Science Building",
            36.36993,
            127.364599,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E6-1) Department of Mathematical Sciences",
            36.369475,
            127.36458,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E6-2) Department of Physics",
            36.369865,
            127.364177,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E6-3) Department of Biological Sciences",
            36.369997,
            127.365073,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E6-4) Department of Chemistry",
            36.370451,
            127.36443,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E6-5) GoongNi Laboratory Building",
            36.370174,
            127.363719,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E6-6) Basic Science Building",
            36.370818,
            127.365054,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E6-7) Educational Research Center of Biological Science",
            36.369984,
            127.365687,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E7) Biomedical Research Center",
            36.370382,
            127.365494,
            LocationCategory.Building
        ),
        CampusLocation("(E8) Sejong Hall", 36.37123, 127.367149, LocationCategory.Dormitory),
        CampusLocation(
            "(E9) Academic Cultural Complex",
            36.369367,
            127.362721,
            LocationCategory.Library
        ),
        CampusLocation("(E9-1) KAIST Art Museum", 36.369801, 127.362804, LocationCategory.Building),
        CampusLocation("(E10) Storehouse", 36.37136, 127.365409, LocationCategory.Building),
        CampusLocation(
            "(E10-1) Applied Animal Research Center",
            36.371643,
            127.365446,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E11) Creative Learning Building",
            36.370344,
            127.362555,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation("(E12) Energy Plant", 36.371276, 127.364513, LocationCategory.Building),
        CampusLocation(
            "(E13) Chung Mong-Hun Uribyeol Research Building",
            36.372548,
            127.366267,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E14) Main Administration Building",
            36.370518,
            127.361264,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E15) Auditorium",
            36.372062,
            127.362911,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E16) ChungMoonSoul Building",
            36.371541,
            127.361841,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation(
            "(E16-1) YANG Bun Soon Building",
            36.371148,
            127.362343,
            LocationCategory.AcademicBuilding
        ),
        CampusLocation("(E17) Stadium", 36.369583, 127.368515, LocationCategory.SportsField),
        CampusLocation(
            "(E18) Daejeon Disease-model Animal Center",
            36.3683,
            127.368126,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E18-1) Bio Model System Park",
            36.368548,
            127.368107,
            LocationCategory.Building
        ),
        CampusLocation(
            "(E19) National Nano Fab Center",
            36.368256,
            127.366819,
            LocationCategory.Building
        ),
        CampusLocation("(E20) Kyeryong Hall", 36.372561, 127.367066, LocationCategory.Building),
        CampusLocation(
            "(E21) KAIST Clinic-Papalardo Center",
            36.369386,
            127.369829,
            LocationCategory.Hospital
        ),
        CampusLocation(
            "(E22) Institute for Basic Science KAIST Campus",
            36.369587,
            127.367141,
            LocationCategory.Building
        ),
        CampusLocation("KAIST Brand Shop", 36.36974, 127.362238, LocationCategory.Store),
        CampusLocation("Cafe Ogada", 36.36909, 127.362745, LocationCategory.Cafe),
        CampusLocation("Caffè Pascucci", 36.368621, 127.364585, LocationCategory.Cafe),
        CampusLocation("Grazie", 36.3682, 127.363571, LocationCategory.Cafe),
        CampusLocation("SUBWAY", 36.371271, 127.362037, LocationCategory.Restaurant),
        CampusLocation("California bakery&cafe", 36.370077, 127.363687, LocationCategory.Cafe),
        CampusLocation("Convenience store", 36.369205, 127.36377, LocationCategory.Store),
        CampusLocation("Convenience store", 36.371427, 127.366656, LocationCategory.Store),
        CampusLocation("Parking", 36.367056, 127.366111, LocationCategory.Parking),
        CampusLocation("Parking", 36.36671, 127.364346, LocationCategory.Parking),
        CampusLocation("Parking", 36.366909, 127.365076, LocationCategory.Parking),
        CampusLocation("Parking", 36.367509, 127.365918, LocationCategory.Parking),
        CampusLocation("Parking", 36.367729, 127.36631, LocationCategory.Parking),
        CampusLocation("Parking", 36.367691, 127.364604, LocationCategory.Parking),
        CampusLocation("Parking", 36.368658, 127.366626, LocationCategory.Parking),
        CampusLocation("Parking", 36.369894, 127.36919, LocationCategory.Parking),
        CampusLocation("Parking", 36.369341, 127.365924, LocationCategory.Parking),
        CampusLocation("Parking", 36.369483, 127.365505, LocationCategory.Parking),
        CampusLocation("Parking", 36.370563, 127.367254, LocationCategory.Parking),
        CampusLocation("Parking", 36.369721, 127.365451, LocationCategory.Parking),
        CampusLocation("Parking", 36.370127, 127.365897, LocationCategory.Parking),
        CampusLocation("Parking", 36.370563, 127.366079, LocationCategory.Parking),
        CampusLocation("Parking", 36.370541, 127.365339, LocationCategory.Parking),
        CampusLocation("Parking", 36.370978, 127.364271, LocationCategory.Parking),
        CampusLocation("Parking", 36.370874, 127.363762, LocationCategory.Parking),
        CampusLocation("Parking", 36.369958, 127.363166, LocationCategory.Parking),
        CampusLocation("Parking", 36.369604, 127.363542, LocationCategory.Parking),
        CampusLocation("Parking", 36.372306, 127.363724, LocationCategory.Parking),
        CampusLocation("Parking", 36.3709, 127.360919, LocationCategory.Parking),
        CampusLocation("East-Campus Cafeteria", 36.369021, 127.363794, LocationCategory.Cafeteria)
    )
}
