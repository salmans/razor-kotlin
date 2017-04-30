package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class NotTest {
    @Test
    fun print() {
        assertEquals("¬R()",(!R()).print())
        assertEquals("¬(¬R())",(!!R()).print())
        assertEquals("¬(x = y)", (!(x equals y)).print())
        assertEquals("¬R(x, y)",(!R(x, y)).print())
        assertEquals("¬(R(x, y) ∧ Q(z))",(!(R(x, y) and Q(z))).print())
        assertEquals("¬(R(x, y) ∨ Q(z))",(!(R(x, y) or Q(z))).print())
        assertEquals("¬(R(x, y) ∧ (¬Q(z)))",(!(R(x, y) and !Q(z))).print())
        assertEquals("¬(R(x, y) → Q(z))",(!(R(x, y) implies Q(z))).print())
    }
}