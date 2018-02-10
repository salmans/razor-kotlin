package formula

import org.junit.Assert.assertEquals
import org.junit.Test

internal class SyntaxTest {
    @Test
    fun printFunc() {
        assertEquals("f", f.toString())
        assertEquals("g", g.toString())
    }

    @Test
    fun printVar() {
        assertEquals("x", x.toString())
        assertEquals("y", y.toString())
    }

    @Test
    fun freeVarsVar() {
        assertEquals(setOf(x), x.freeVars)
    }

    @Test
    fun printConst() {
        assertEquals("'a", a.toString())
        assertEquals("'b", b.toString())
    }

    @Test
    fun freeVarConst() {
        assertEquals(emptySet<Var>(), a.freeVars)
    }

    @Test
    fun printApp() {
        assertEquals("f()", f().toString())
        assertEquals("f(x, y)", f(x, y).toString())
        assertEquals("f(g(x, y))", f(g(x, y)).toString())
        assertEquals("f(f(f(f(f(f(f(x)))))))", f(f(f(f(f(f(f(x))))))).toString())
    }

    @Test
    fun freeVarsApp() {
        assertEquals(emptySet<Var>(), f().freeVars)
        assertEquals(emptySet<Var>(), f(g(h(), g())).freeVars)
        assertEquals(setOf(x), f(x).freeVars)
        assertEquals(setOf(x, y, z), f(x, y, z).freeVars)
        assertEquals(setOf(x, y), f(x, y, x).freeVars)
        assertEquals(setOf(x, y, z), f(g(x), h(y, f(g(z)))).freeVars)
    }

    @Test
    fun printPred() {
        assertEquals("P", P.toString())
        assertEquals("Q", Q.toString())
    }

    @Test
    fun printTop() {
        assertEquals("⊤", TRUE.toString())
    }

    @Test
    fun freeVarsTop() {
        assertEquals(emptySet<Var>(), TRUE.freeVars)
    }

    @Test
    fun printBottom() {
        assertEquals("⟘", FALSE.toString())
    }

    @Test
    fun freeVarsBottom() {
        assertEquals(emptySet<Var>(), FALSE.freeVars)
    }

    @Test
    fun printAtom() {
        assertEquals("R()", R().toString())
        assertEquals("R(x, y)", R(x, y).toString())
        assertEquals("R(g(x, y))", R(g(x, y)).toString())
        assertEquals("R(f(f(f(f(f(f(x)))))))", R(f(f(f(f(f(f(x))))))).toString())
    }

    @Test
    fun freeVarsAtom() {
        assertEquals(emptySet<Var>(), R().freeVars)
        assertEquals(setOf(x, y), R(x, y).freeVars)
        assertEquals(setOf(x, y, z), R(y, g(x, z)).freeVars)
        assertEquals(setOf(x, z), R(z, f(f(f(f(f(f(x))))))).freeVars)
    }

    @Test
    fun printEquals() {
        assertEquals("x = y", (x equals y).toString())
    }

    @Test
    fun freeVarsEquals() {
        assertEquals(setOf(x, y), (x equals y).freeVars)
        assertEquals(setOf(x), (x equals g()).freeVars)
    }

    @Test
    fun printNot() {
        assertEquals("¬R()", (!R()).toString())
        assertEquals("¬(¬R())", (!!R()).toString())
        assertEquals("¬(x = y)", (!(x equals y)).toString())
        assertEquals("¬R(x, y)", (!R(x, y)).toString())
        assertEquals("¬(R(x, y) ∧ Q(z))", (!(R(x, y) and Q(z))).toString())
        assertEquals("¬(R(x, y) ∨ Q(z))", (!(R(x, y) or Q(z))).toString())
        assertEquals("¬(R(x, y) ∧ (¬Q(z)))", (!(R(x, y) and !Q(z))).toString())
        assertEquals("¬(R(x, y) → Q(z))", (!(R(x, y) implies Q(z))).toString())
        assertEquals("¬(R(x, y) ⇔ Q(z))", (!(R(x, y) iff Q(z))).toString())
    }

    @Test
    fun freeVarsNot() {
        assertEquals(emptySet<Var>(), (!R()).freeVars)
        assertEquals(emptySet<Var>(), (!!R()).freeVars)
        assertEquals(setOf(x, y), (!(x equals y)).freeVars)
        assertEquals(setOf(x, y), (!R(x, y)).freeVars)
    }

    @Test
    fun printAnd() {
        assertEquals("P() ∧ Q()", (P() and Q()).toString())
        assertEquals("P() ∧ (x = y)", (P() and (x equals y)).toString())
        assertEquals("P() ∧ (¬Q())", (P() and !Q()).toString())
        assertEquals("P() ∧ (Q() ∧ R())", (P() and (Q() and R())).toString())
        assertEquals("P() ∧ (Q() ∨ R())", (P() and (Q() or R())).toString())
        assertEquals("P() ∧ (Q() → R())", (P() and (Q() implies R())).toString())
        assertEquals("P() ∧ (Q() ⇔ R())", (P() and (Q() iff R())).toString())
    }

    @Test
    fun freeVarsAnd() {
        assertEquals(emptySet<Var>(), (P() and Q()).freeVars)
        assertEquals(setOf(x, y, z), (P(z, y) and (x equals y)).freeVars)
    }

    @Test
    fun printOr() {
        assertEquals("P() ∨ Q()", (P() or Q()).toString())
        assertEquals("P() ∨ (¬Q())", (P() or !Q()).toString())
        assertEquals("P() ∨ (x = y)", (P() or (x equals y)).toString())
        assertEquals("P() ∨ (Q() ∧ R())", (P() or (Q() and R())).toString())
        assertEquals("P() ∨ (Q() ∨ R())", (P() or (Q() or R())).toString())
        assertEquals("P() ∨ (Q() → R())", (P() or (Q() implies R())).toString())
        assertEquals("P() ∨ (Q() ⇔ R())", (P() or (Q() iff R())).toString())
    }

    @Test
    fun freeVarsOr() {
        assertEquals(emptySet<Var>(), (P() or Q()).freeVars)
        assertEquals(setOf(x, y, z), (P(z, y) or (x equals y)).freeVars)
    }

    @Test
    fun printImplies() {
        assertEquals("P() → Q()", (P() implies Q()).toString())
        assertEquals("P() → (x = y)", (P() implies (x equals y)).toString())
        assertEquals("P() → (¬Q())", (P() implies !Q()).toString())
        assertEquals("P() → (Q() ∧ R())", (P() implies (Q() and R())).toString())
        assertEquals("P() → (Q() ∨ R())", (P() implies (Q() or R())).toString())
        assertEquals("P() → (Q() → R())", (P() implies (Q() implies R())).toString())
        assertEquals("P() → (Q() ⇔ R())", (P() implies (Q() iff R())).toString())
    }

    @Test
    fun freeVarsImplies() {
        assertEquals(emptySet<Var>(), (P() implies Q()).freeVars)
        assertEquals(setOf(x, y, z), (P(z, y) implies (x equals y)).freeVars)
    }

    @Test
    fun printIff() {
        assertEquals("P() ⇔ Q()", (P() iff Q()).toString())
        assertEquals("P() ⇔ (x = y)", (P() iff (x equals y)).toString())
        assertEquals("P() ⇔ (¬Q())", (P() iff !Q()).toString())
        assertEquals("P() ⇔ (Q() ∧ R())", (P() iff (Q() and R())).toString())
        assertEquals("P() ⇔ (Q() ∨ R())", (P() iff (Q() or R())).toString())
        assertEquals("P() ⇔ (Q() → R())", (P() iff (Q() implies R())).toString())
        assertEquals("P() ⇔ (Q() ⇔ R())", (P() iff (Q() iff R())).toString())
    }

    @Test
    fun freeVarsIff() {
        assertEquals(emptySet<Var>(), (P() iff Q()).freeVars)
        assertEquals(setOf(x, y, z), (P(z, y) iff (x equals y)).freeVars)
    }

    @Test
    fun printExists() {
        assertEquals("∃ x. P(x)", (exists(x) { P(x) }).toString())
        assertEquals("∃ x, y. P(x, y)", (exists(x, y) { P(x, y) }).toString())
        assertEquals("∃ x. (x = y)", (exists(x) { x equals y }).toString())
        assertEquals("∃ x. (¬Q(x))", (exists(x) { !Q(x) }).toString())
        assertEquals("∃ x. (Q(x) ∧ R(x))", (exists(x) { Q(x) and R(x) }).toString())
        assertEquals("∃ x. (Q(x) ∨ R(x))", (exists(x) { Q(x) or R(x) }).toString())
        assertEquals("∃ x. (Q(x) → R(x))", (exists(x) { Q(x) implies R(x) }).toString())
        assertEquals("∃ x. (Q(x) ⇔ R(x))", (exists(x) { Q(x) iff R(x) }).toString())
    }

    @Test
    fun freeVarsExists() {
        assertEquals(emptySet<Var>(), (exists(x) { P(x) }).freeVars)
        assertEquals(emptySet<Var>(), (exists(x, y) { P(x, y) }).freeVars)
        assertEquals(setOf(y), (exists(x) { x equals y }).freeVars)
        assertEquals(setOf(y), (exists(x) { Q(x) and R(y) }).freeVars)
        assertEquals(setOf(y, z), (exists(x) { Q(x, z) or R(x, y) }).freeVars)
    }

    @Test
    fun printForall() {
        assertEquals("∀ x. P(x)", (forall(x) { P(x) }).toString())
        assertEquals("∀ x, y. P(x, y)", (forall(x, y) { P(x, y) }).toString())
        assertEquals("∀ x. (x = y)", (forall(x) { x equals y }).toString())
        assertEquals("∀ x. (¬Q(x))", (forall(x) { !Q(x) }).toString())
        assertEquals("∀ x. (Q(x) ∧ R(x))", (forall(x) { Q(x) and R(x) }).toString())
        assertEquals("∀ x. (Q(x) ∨ R(x))", (forall(x) { Q(x) or R(x) }).toString())
        assertEquals("∀ x. (Q(x) → R(x))", (forall(x) { Q(x) implies R(x) }).toString())
        assertEquals("∀ x. (Q(x) ⇔ R(x))", (forall(x) { Q(x) iff R(x) }).toString())
    }

    @Test
    fun freeVarsForall() {
        assertEquals(emptySet<Var>(), (forall(x) { P(x) }).freeVars)
        assertEquals(emptySet<Var>(), (forall(x, y) { P(x, y) }).freeVars)
        assertEquals(setOf(y), (forall(x) { x equals y }).freeVars)
        assertEquals(setOf(y), (forall(x) { Q(x) and R(y) }).freeVars)
        assertEquals(setOf(y, z), (forall(x) { Q(x, z) or R(x, y) }).freeVars)
    }    

    @Test
    fun printTheories() {
        assertEquals(
                "∀ x. (x = x)\n" +
                        "∀ x, y. ((x = y) → (y = x))\n" +
                        "∀ x, y, z. (((x = y) ∧ (y = z)) → (x = z))", Theory(listOf(
                forall(x) { x equals x },
                forall(x, y) { (x equals y) implies (y equals x) },
                forall(x, y, z) { (x equals y) and (y equals z) implies (x equals z) })).toString()
        )
    }
}