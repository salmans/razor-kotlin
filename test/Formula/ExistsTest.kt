package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class ExistsTest {
    @Test
    fun print() {
        assertEquals("∃ x. P(x)", (exists(x){P(x)}).print())
        assertEquals("∃ x, y. P(x, y)", (exists(x, y){P(x, y)}).print())
        assertEquals("∃ x. (x = y)", (exists(x){x equals y}).print())
        assertEquals("∃ x. (¬Q(x))", (exists(x){!Q(x)}).print())
        assertEquals("∃ x. (Q(x) ∧ R(x))", (exists(x){Q(x) and R(x)}).print())
        assertEquals("∃ x. (Q(x) ∨ R(x))", (exists(x){Q(x) or R(x)}).print())
        assertEquals("∃ x. (Q(x) → R(x))", (exists(x){Q(x) implies R(x)}).print())
    }
}