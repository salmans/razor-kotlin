package Formula

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class SyntaxTest {
    @Test
    fun printFunc() {
        assertEquals("f", f.print())
        assertEquals("g", g.print())
    }

    @Test
    fun printVar() {
        assertEquals("x", x.print())
        assertEquals("y", y.print())
    }

    @Test
    fun printApp() {
        assertEquals("f()", f().print())
        assertEquals("f(x, y)", f(x,y).print())
        assertEquals("f(g(x, y))", f(g(x,y)).print())
        assertEquals("f(f(f(f(f(f(f(x)))))))", f(f(f(f(f(f(f(x))))))).print())
    }

    @Test
    fun printRel() {
        assertEquals("P", P.print())
        assertEquals("Q", Q.print())
    }

    @Test
    fun printTop() {
        assertEquals("⊤", TRUE.print())
    }

    @Test
    fun printBottom() {
        assertEquals("⟘", FALSE.print())
    }

    @Test
    fun printAtom() {
        assertEquals("R()", R().print())
        assertEquals("R(x, y)", R(x,y).print())
        assertEquals("R(g(x, y))", R(g(x,y)).print())
        assertEquals("R(f(f(f(f(f(f(x)))))))", R(f(f(f(f(f(f(x))))))).print())
    }

    @Test
    fun printEquals() {
        assertEquals("x = y", (x equals y).print())
    }

    @Test
    fun printNot() {
        assertEquals("¬R()",(!R()).print())
        assertEquals("¬(¬R())",(!!R()).print())
        assertEquals("¬(x = y)", (!(x equals y)).print())
        assertEquals("¬R(x, y)",(!R(x, y)).print())
        assertEquals("¬(R(x, y) ∧ Q(z))",(!(R(x, y) and Q(z))).print())
        assertEquals("¬(R(x, y) ∨ Q(z))",(!(R(x, y) or Q(z))).print())
        assertEquals("¬(R(x, y) ∧ (¬Q(z)))",(!(R(x, y) and !Q(z))).print())
        assertEquals("¬(R(x, y) → Q(z))",(!(R(x, y) implies Q(z))).print())
    }

    @Test
    fun printAnd() {
        assertEquals("P() ∧ Q()", (P() and Q()).print())
        assertEquals("P() ∧ (x = y)", (P() and (x equals y)).print())
        assertEquals("P() ∧ (¬Q())", (P() and !Q()).print())
        assertEquals("P() ∧ (Q() ∧ R())", (P() and (Q() and R())).print())
        assertEquals("P() ∧ (Q() ∨ R())", (P() and (Q() or R())).print())
        assertEquals("P() ∧ (Q() → R())", (P() and (Q() implies R())).print())
    }

    @Test
    fun printOr() {
        assertEquals("P() ∨ Q()", (P() or Q()).print())
        assertEquals("P() ∨ (¬Q())", (P() or !Q()).print())
        assertEquals("P() ∨ (x = y)", (P() or (x equals y)).print())
        assertEquals("P() ∨ (Q() ∧ R())", (P() or (Q() and R())).print())
        assertEquals("P() ∨ (Q() ∨ R())", (P() or (Q() or R())).print())
        assertEquals("P() ∨ (Q() → R())", (P() or (Q() implies R())).print())
    }

    @Test
    fun printImplies() {
        assertEquals("P() → Q()", (P() implies Q()).print())
        assertEquals("P() → (x = y)", (P() implies (x equals y)).print())
        assertEquals("P() → (¬Q())", (P() implies !Q()).print())
        assertEquals("P() → (Q() ∧ R())", (P() implies (Q() and R())).print())
        assertEquals("P() → (Q() ∨ R())", (P() implies (Q() or R())).print())
        assertEquals("P() → (Q() → R())", (P() implies (Q() implies R())).print())
    }

    @Test
    fun printExists() {
        assertEquals("∃ x. P(x)", (exists(x){P(x)}).print())
        assertEquals("∃ x, y. P(x, y)", (exists(x, y){P(x, y)}).print())
        assertEquals("∃ x. (x = y)", (exists(x){x equals y}).print())
        assertEquals("∃ x. (¬Q(x))", (exists(x){!Q(x)}).print())
        assertEquals("∃ x. (Q(x) ∧ R(x))", (exists(x){Q(x) and R(x)}).print())
        assertEquals("∃ x. (Q(x) ∨ R(x))", (exists(x){Q(x) or R(x)}).print())
        assertEquals("∃ x. (Q(x) → R(x))", (exists(x){Q(x) implies R(x)}).print())
    }

    @Test
    fun printForall() {
        assertEquals("∀ x. P(x)", (forall(x){P(x)}).print())
        assertEquals("∀ x, y. P(x, y)", (forall(x, y){P(x, y)}).print())
        assertEquals("∀ x. (x = y)", (forall(x){x equals y}).print())
        assertEquals("∀ x. (¬Q(x))", (forall(x){!Q(x)}).print())
        assertEquals("∀ x. (Q(x) ∧ R(x))", (forall(x){Q(x) and R(x)}).print())
        assertEquals("∀ x. (Q(x) ∨ R(x))", (forall(x){Q(x) or R(x)}).print())
        assertEquals("∀ x. (Q(x) → R(x))", (forall(x){Q(x) implies R(x)}).print())
    }

    @Test
    fun printTheories() {
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