package org.sparcs.soap.postListTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.domain.enums.ara.AraPostNicknameType
import org.sparcs.soap.app.domain.models.ara.AraPost

class AraCodeMappingTest {

    @Test
    fun mapsReadStatusCodes() {
        assertEquals(AraPost.ReadStatus.UNREAD, AraPost.ReadStatus.fromCode("N"))
        assertEquals(AraPost.ReadStatus.READ, AraPost.ReadStatus.fromCode("-"))
        assertEquals(AraPost.ReadStatus.UPDATED, AraPost.ReadStatus.fromCode("U"))
    }

    @Test
    fun ignoresUnknownReadStatusCodes() {
        assertNull(AraPost.ReadStatus.fromCode(""))
        assertNull(AraPost.ReadStatus.fromCode("R"))
        assertNull(AraPost.ReadStatus.fromCode(null))
    }

    @Test
    fun mapsNameTypeCodes() {
        assertEquals(AraPostNicknameType.REGULAR, AraPostNicknameType.fromCode(1))
        assertEquals(AraPostNicknameType.ANONYMOUS, AraPostNicknameType.fromCode(2))
        assertEquals(AraPostNicknameType.REALNAME, AraPostNicknameType.fromCode(4))
    }

    @Test
    fun ignoresUnknownNameTypeCodes() {
        assertNull(AraPostNicknameType.fromCode(0))
        assertNull(AraPostNicknameType.fromCode(3))
    }

    @Test
    fun readsTheNameTypesABoardAllows() {
        assertEquals(listOf(AraPostNicknameType.REGULAR), AraPostNicknameType.allowed(1))
        assertEquals(listOf(AraPostNicknameType.REGULAR, AraPostNicknameType.ANONYMOUS), AraPostNicknameType.allowed(3))
        assertEquals(listOf(AraPostNicknameType.REALNAME), AraPostNicknameType.allowed(4))
        assertEquals(emptyList<AraPostNicknameType>(), AraPostNicknameType.allowed(0))
    }
}
