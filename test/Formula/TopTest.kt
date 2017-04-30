package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class TopTest {
    @Test
    fun print() {
        assertEquals("⊤", TRUE.print())
    }

}