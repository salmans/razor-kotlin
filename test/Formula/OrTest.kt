package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class OrTest {
    @Test
    fun print() {
        assertEquals("P() ∨ Q()", (P() or Q()).print())
        assertEquals("P() ∨ (¬Q())", (P() or !Q()).print())
        assertEquals("P() ∨ (x = y)", (P() or (x equals y)).print())
        assertEquals("P() ∨ (Q() ∧ R())", (P() or (Q() and R())).print())
        assertEquals("P() ∨ (Q() ∨ R())", (P() or (Q() or R())).print())
        assertEquals("P() ∨ (Q() → R())", (P() or (Q() implies R())).print())
    }

}