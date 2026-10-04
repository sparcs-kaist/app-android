package org.sparcs.soap.networkingTests

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.networking.responseDTO.otl.OTLUserDTO

class OTLUserDTOTest {
    private fun parse(json: String) = Gson().fromJson(json, OTLUserDTO::class.java).toModel()

    @Test fun `null degree from users info still maps to a user`() {
        val user = parse(
            """{"id":1,"name":"홍길동","mail":"a@kaist.ac.kr","studentNumber":20230001,"degree":null,""" +
                """"majorDepartments":[{"id":3,"name":"전산학부"}],"interestedDepartments":[]}"""
        )
        assertEquals("홍길동", user.name)
        assertNull(user.degree)
        assertEquals(listOf("전산학부"), user.majorDepartments.map { it.name })
    }

    @Test fun `missing department lists map to empty`() {
        val user = parse("""{"id":1,"name":"홍길동","mail":"a@kaist.ac.kr","studentNumber":20230001,"degree":"학사"}""")
        assertEquals("학사", user.degree)
        assertTrue(user.majorDepartments.isEmpty())
        assertTrue(user.interestedDepartments.isEmpty())
    }
}
