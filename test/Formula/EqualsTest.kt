package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class EqualsTest {
    @Test
    fun print() {
        assertEquals("x = y", (x equals y).print())
    }
}