package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class TheoryTest {
    @Test
    fun print() {
        assertEquals(
                "∀ x. (x = x)\n" +
                "∀ x, y. ((x = y) → (y = x))\n" +
                "∀ x, y, z. (((x = y) ∧ (y = z)) → (x = z))", Theory(listOf(
                forall(x) { x equals x},
                forall(x, y) { (x equals y) implies (y equals x) },
                forall(x, y, z) { (x equals y) and (y equals z) implies (x equals z)})).print()
        )
    }
}