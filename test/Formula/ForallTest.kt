package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class ForallTest {
    @Test
    fun print() {
        assertEquals("∀ x. P(x)", (forall(x){P(x)}).print())
        assertEquals("∀ x, y. P(x, y)", (forall(x, y){P(x, y)}).print())
        assertEquals("∀ x. (x = y)", (forall(x){x equals y}).print())
        assertEquals("∀ x. (¬Q(x))", (forall(x){!Q(x)}).print())
        assertEquals("∀ x. (Q(x) ∧ R(x))", (forall(x){Q(x) and R(x)}).print())
        assertEquals("∀ x. (Q(x) ∨ R(x))", (forall(x){Q(x) or R(x)}).print())
        assertEquals("∀ x. (Q(x) → R(x))", (forall(x){Q(x) implies R(x)}).print())
    }
}