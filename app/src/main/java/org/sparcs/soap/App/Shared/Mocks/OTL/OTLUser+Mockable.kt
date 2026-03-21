package org.sparcs.soap.App.Shared.Mocks.OTL

import org.sparcs.soap.App.Domain.Models.OTL.Department
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser


fun OTLUser.Companion.mock(): OTLUser {
    return OTLUser(
        id = 12878,
        email = "master@kaist.ac.kr",
        studentNumber = 20230045,
        name = "Kwon Soongyu",
        majorDepartments = listOf(Department(id = 709, name = "건설및환경공학과")),
        interestedDepartments = listOf(Department(id = 709, name = "건설및환경공학과")),
        degree = ""
        )
}