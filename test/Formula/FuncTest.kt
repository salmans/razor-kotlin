package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class FuncTest {
    @Test
    fun print() {
        assertEquals("f", f.print())
        assertEquals("g", g.print())
    }
}