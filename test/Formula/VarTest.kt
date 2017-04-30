package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class VarTest {
    @Test
    fun print() {
        assertEquals("x", x.print())
        assertEquals("y", y.print())
    }
}