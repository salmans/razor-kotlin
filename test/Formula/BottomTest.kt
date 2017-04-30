package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class BottomTest {
    @Test
    fun print() {
        assertEquals("⟘", FALSE.print())
    }
}