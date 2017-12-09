package chase

import formula.*
import org.junit.Test
import kotlin.test.assertEquals

class BasicTest {
    @Test
    fun testEmptyBasic() {
        BasicModel().let {
            assertEquals(emptySet(), it.getDomain())
            assertEquals(emptySet(), it.getObservations())
            assertEquals(emptySet(), it.getWitnesses(e_0))
        }
    }

    @Test
    fun testAddElement() {
        BasicModel().let {
            it.addElement(e_0)
            assertEquals(setOf(e_0), it.getDomain())
        }
        BasicModel().let {
            it.addElement(e_0)
            it.addElement(e_0)
            assertEquals(setOf(e_0), it.getDomain())
        }
        BasicModel().let {
            it.addElement(e_0)
            it.addElement(e_1)
            it.addElement(e_2)
            assertEquals(setOf(e_0, e_1, e_2), it.getDomain())
        }
    }

    @Test
    fun testAddObservations() {
        BasicModel().let {
            it.addObservation(_R())
            assertEquals(setOf(_R.observe()), it.getObservations())
        }
        BasicModel().let {
            it.addObservation(_R(e_0))
            assertEquals(setOf(_R.observe(e_0)), it.getObservations())
        }
        BasicModel().let {
            it.addObservation(_R(e_0, e_1))
            assertEquals(setOf(_R.observe(e_0, e_1)), it.getObservations())
        }
        BasicModel().let {
            it.addObservation(_R(e_0, e_1))
            it.addObservation(_S(e_2, e_3))
            assertEquals(setOf(_R.observe(e_0, e_1), _S.observe(e_2, e_3)), it.getObservations())
        }
        BasicModel().let {
            it.addObservation(_R(e_0, e_1))
            it.addObservation(_S(e_2))
            it.addObservation(_R(e_3, e_4))
            it.addObservation(_S(e_5))
            assertEquals(setOf(_R.observe(e_0, e_1), _S.observe(e_2), _R.observe(e_3, e_4), _S.observe(e_5)), it.getObservations())
        }
    }

    @Test
    fun testAddWitnesses() {
        BasicModel().let {
            it.addWitness(e_0, _a)
            assertEquals(setOf(_a), it.getWitnesses(e_0))
        }
        BasicModel().let {
            it.addWitness(e_0, _a)
            it.addWitness(e_0, _b)
            it.addWitness(e_0, _c)
            it.addWitness(e_0, _a)
            it.addWitness(e_0, _d)
            assertEquals(setOf(_a, _b, _c, _d), it.getWitnesses(e_0))
            it.addWitness(e_1, _f(_a))
            assertEquals(setOf(_a, _b, _c, _d), it.getWitnesses(e_0))
            assertEquals(setOf(_f(_a)), it.getWitnesses(e_1))
        }
    }

    @Test
    fun testLit() {
        assertEquals(Literal.Atm(P, listOf(x)), P(x).lit())
        assertEquals(Literal.Atm(P, listOf(x, f(y))), P(x, f(y)).lit())
        assertEquals(Literal.Eql(x, y), (x equals y).lit())
        assertEquals(Literal.Eql(g(x), f(y)), (g(x) equals f(y)).lit())
    }

    @Test
    fun testNeg() {
        assertEquals(Literal.Neg(P, listOf(x)), P(x).neg())
        assertEquals(Literal.Neg(P, listOf(x, f(y))), P(x, f(y)).neg())
        assertEquals(Literal.Neq(x, y), (x equals y).neg())
        assertEquals(Literal.Neq(g(x), f(y)), (g(x) equals f(y)).neg())
    }

    @Test
    fun printLit() {
        assertEquals("P(x)", P(x).lit().print())
        assertEquals("P(f(x), g(y))", P(f(x), g(y)).lit().print())
        assertEquals("x = y", (x equals  y).lit().print())
        assertEquals("f(x) = g(y)", (f(x) equals  g(y)).lit().print())
        assertEquals("¬P(x)", P(x).neg().print())
        assertEquals("¬P(f(x), g(y))", P(f(x), g(y)).neg().print())
        assertEquals("x ≠ y", (x equals  y).neg().print())
        assertEquals("f(x) ≠ g(y)", (f(x) equals  g(y)).neg().print())
    }

    @Test
    fun testBuildSequent() {
        BasicSequent(TRUE).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList()), it.head)
        }
        BasicSequent(FALSE).let {
            assertEquals(emptyList(), it.body)
            assertEquals(emptyList(), it.head)
        }
        BasicSequent(TRUE implies TRUE).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList()), it.head)
        }
        BasicSequent(TRUE implies !FALSE).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList()), it.head)
        }
        BasicSequent(TRUE implies (TRUE and TRUE)).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList()), it.head)
        }
        BasicSequent(TRUE implies (TRUE or TRUE)).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList(), emptyList()), it.head)
        }
        BasicSequent(TRUE implies FALSE).let {
            assertEquals(emptyList(), it.body)
            assertEquals(emptyList(), it.head)
        }
        BasicSequent(TRUE implies !TRUE).let {
            assertEquals(emptyList(), it.body)
            assertEquals(emptyList(), it.head)
        }
        BasicSequent(TRUE implies (TRUE and FALSE)).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList()), it.head)
        }
        BasicSequent(TRUE implies (TRUE or FALSE)).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList()), it.head)
        }
        BasicSequent(P(x) implies Q(x)).let {
            assertEquals(listOf(P(x).lit()), it.body)
            assertEquals(listOf(listOf(Q(x).lit())), it.head)
        }
        BasicSequent(!P(x) implies !Q(x)).let {
            assertEquals(listOf(P(x).neg()), it.body)
            assertEquals(listOf(listOf(Q(x).neg())), it.head)
        }
        BasicSequent((P(x) and Q(x)) implies Q(y)).let {
            assertEquals(listOf(P(x).lit(), Q(x).lit()), it.body)
            assertEquals(listOf(listOf(Q(y).lit())), it.head)
        }
        BasicSequent((P(x) and Q(x)) implies (Q(x) or (R(z) and S(z)))).let {
            assertEquals(listOf(P(x).lit(), Q(x).lit()), it.body)
            assertEquals(listOf(listOf(Q(x).lit()), listOf(R(z).lit(), S(z).lit())), it.head)
        }
        BasicSequent(TRUE implies ((P(x) and Q(x)) or (P(y) and Q(y)) or (P(z) and Q(z)))).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(
                    listOf(P(x).lit(), Q(x).lit())
                    , listOf(P(y).lit(), Q(y).lit())
                    , listOf(P(z).lit(), Q(z).lit())), it.head)
        }
        assertFailure(INVALID_SEQUENT_FALSE_BODY, { BasicSequent(FALSE implies TRUE) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent((P(x) or Q(x)) implies R(x)) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(P(x) implies R(x) and (Q(z) or R(z))) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(P(x) implies exists(x) { Q(x) }) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(exists(x) { Q(x) } implies P(x)) })
    }
}