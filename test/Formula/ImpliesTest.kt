package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class ImpliesTest {
    @Test
    fun print() {
        assertEquals("P() → Q()", (P() implies Q()).print())
        assertEquals("P() → (x = y)", (P() implies (x equals y)).print())
        assertEquals("P() → (¬Q())", (P() implies !Q()).print())
        assertEquals("P() → (Q() ∧ R())", (P() implies (Q() and R())).print())
        assertEquals("P() → (Q() ∨ R())", (P() implies (Q() or R())).print())
        assertEquals("P() → (Q() → R())", (P() implies (Q() implies R())).print())
    }
}