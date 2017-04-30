package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class RelTest {
    @Test
    fun print() {
        assertEquals("P", P.print())
        assertEquals("Q", Q.print())
    }
}