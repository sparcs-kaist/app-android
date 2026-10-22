package org.sparcs.soap.otlTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.Professor
import org.sparcs.soap.app.domain.models.otl.SectionLabel
import org.sparcs.soap.app.domain.models.otl.subtitleTopic

class SectionLabelTest {
    @Test
    fun `subtitle replaces placeholder professor`() {
        val label = SectionLabel.of("<리더십 강좌>", listOf(Professor(1, "Staff")))
        assertEquals("리더십 강좌", label.title)
        assertNull(label.professors)
    }

    @Test
    fun `subtitle with real professor keeps both`() {
        val label = SectionLabel.of("디자인", listOf(Professor(1, "Kim"), Professor(2, " staff ")))
        assertEquals("디자인", label.title)
        assertEquals("Kim", label.professors)
    }

    @Test
    fun `no subtitle falls back to professors`() {
        val label = SectionLabel.of("  ", listOf(Professor(1, "Kim"), Professor(2, "Lee")))
        assertEquals("Kim, Lee", label.title)
        assertNull(label.professors)
    }

    @Test
    fun `no subtitle and no professors yields no title`() {
        assertNull(SectionLabel.of("", listOf(Professor(1, ""))).title)
    }

    @Test
    fun `brackets only subtitle is empty`() {
        assertNull("< >".subtitleTopic)
        assertNull("<>".subtitleTopic)
        assertEquals("<", "<".subtitleTopic)
    }
}
