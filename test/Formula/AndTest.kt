package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class AndTest {
    @Test
    fun print() {
        assertEquals("P() ∧ Q()", (P() and Q()).print())
        assertEquals("P() ∧ (x = y)", (P() and (x equals y)).print())
        assertEquals("P() ∧ (¬Q())", (P() and !Q()).print())
        assertEquals("P() ∧ (Q() ∧ R())", (P() and (Q() and R())).print())
        assertEquals("P() ∧ (Q() ∨ R())", (P() and (Q() or R())).print())
        assertEquals("P() ∧ (Q() → R())", (P() and (Q() implies R())).print())
    }
}