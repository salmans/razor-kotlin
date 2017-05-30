package chase

import formula.*
import org.junit.Test
import kotlin.test.assertEquals

class BasicTest {
    @Test
    fun testEmptyBasic() {
        BasicModel().let {
            assertEquals(emptySet(), it.getDomain())
            assertEquals(emptySet(), it.getFacts())
            assertEquals(emptySet(), it.getWitnesses(e_0))
        }
    }

    @Test
    fun testAddObservations() {
        BasicModel().let {
            it.addObservations(setOf(_R()))
            assertEquals(emptySet(), it.getDomain())
            assertEquals(setOf(_R.fact()), it.getFacts())
            assertEquals(emptySet(), it.getWitnesses(e_0))
        }
        BasicModel().let {
            it.addObservations(setOf(_R(_c)))
            assertEquals(setOf(e_0), it.getDomain())
            assertEquals(setOf(_R.fact(e_0)), it.getFacts())
            assertEquals(setOf(_c), it.getWitnesses(e_0))
        }
        BasicModel().let {
            it.addObservations(setOf(_R(_c, _f(_d))))
            assertEquals(setOf(e_0, e_1), it.getDomain())
            assertEquals(setOf(_R.fact(e_0, e_1)), it.getFacts())
            assertEquals(setOf(_c), it.getWitnesses(e_0))
            assertEquals(setOf(_f(_d)), it.getWitnesses(e_1))
        }
        BasicModel().let {
            it.addObservations(setOf(_R(_c, _f(_d)), _S(_d, _g(_f(_a)))))
            assertEquals(setOf(e_0, e_1, e_2, e_3), it.getDomain())
            assertEquals(setOf(_R.fact(e_0, e_1), _S.fact(e_2, e_3)), it.getFacts())
            assertEquals(setOf(_c), it.getWitnesses(e_0))
            assertEquals(setOf(_f(_d)), it.getWitnesses(e_1))
            assertEquals(setOf(_d), it.getWitnesses(e_2))
            assertEquals(setOf(_g(_f(_a))), it.getWitnesses(e_3))
        }
        BasicModel().let {
            it.addObservations(setOf(_R(_a, _b), _S(_f(_a))))
            it.addObservations(setOf(_R(_f(_c), _d), _S(_b)))
            assertEquals(setOf(e_0, e_1, e_2, e_3, e_4, e_5), it.getDomain())
            assertEquals(setOf(_R.fact(e_0, e_1), _S.fact(e_2), _R.fact(e_3, e_4), _S.fact(e_5)), it.getFacts())
            assertEquals(setOf(_a), it.getWitnesses(e_0))
            assertEquals(setOf(_b), it.getWitnesses(e_1))
            assertEquals(setOf(_f(_a)), it.getWitnesses(e_2))
            assertEquals(setOf(_f(_c)), it.getWitnesses(e_3))
            assertEquals(setOf(_d), it.getWitnesses(e_4))
            assertEquals(setOf(_b), it.getWitnesses(e_5))
        }
    }
    
    @Test
    fun testLit() {
        assertEquals(Literal.Tru, Top.lit())
        assertEquals(Literal.Fls, Bottom.lit())
        assertEquals(Literal.Atm(P, listOf(x)), P(x).lit())
        assertEquals(Literal.Atm(P, listOf(x, f(y))), P(x, f(y)).lit())
        assertEquals(Literal.Eql(x, y), (x equals y).lit())
        assertEquals(Literal.Eql(g(x), f(y)), (g(x) equals f(y)).lit())
    }

    @Test
    fun testNeg() {
        assertEquals(Literal.Fls, Top.neg())
        assertEquals(Literal.Tru, Bottom.neg())
        assertEquals(Literal.Neg(P, listOf(x)), P(x).neg())
        assertEquals(Literal.Neg(P, listOf(x, f(y))), P(x, f(y)).neg())
        assertEquals(Literal.Neq(x, y), (x equals y).neg())
        assertEquals(Literal.Neq(g(x), f(y)), (g(x) equals f(y)).neg())
    }

    @Test
    fun printLit() {
        assertEquals("⊤", Top.lit().print())
        assertEquals("⟘", Bottom.lit().print())
        assertEquals("P(x)", P(x).lit().print())
        assertEquals("P(f(x), g(y))", P(f(x), g(y)).lit().print())
        assertEquals("x = y", (x equals  y).lit().print())
        assertEquals("f(x) = g(y)", (f(x) equals  g(y)).lit().print())
        assertEquals("⟘", Top.neg().print())
        assertEquals("⊤", Bottom.neg().print())
        assertEquals("¬P(x)", P(x).neg().print())
        assertEquals("¬P(f(x), g(y))", P(f(x), g(y)).neg().print())
        assertEquals("x ≠ y", (x equals  y).neg().print())
        assertEquals("f(x) ≠ g(y)", (f(x) equals  g(y)).neg().print())
    }

    @Test
    fun testBuildSequent() {
        BasicSequent(TRUE).let {
            assertEquals(emptySet(), it.body)
            assertEquals(emptySet(), it.head)
        }
        BasicSequent(FALSE).let {
            assertEquals(emptySet(), it.body)
            assertEquals(setOf(emptySet()), it.head)
        }
        BasicSequent(TRUE implies TRUE).let {
            assertEquals(emptySet(), it.body)
            assertEquals(emptySet(), it.head)
        }
        BasicSequent(TRUE implies (TRUE and TRUE)).let {
            assertEquals(emptySet(), it.body)
            assertEquals(emptySet(), it.head)
        }
        BasicSequent(TRUE implies (TRUE or TRUE)).let {
            assertEquals(emptySet(), it.body)
            assertEquals(emptySet(), it.head)
        }
        BasicSequent(TRUE implies FALSE).let {
            assertEquals(emptySet(), it.body)
            assertEquals(setOf(emptySet()), it.head)
        }
        BasicSequent(TRUE implies (TRUE and FALSE)).let {
            assertEquals(emptySet(), it.body)
            assertEquals(setOf(emptySet()), it.head)
        }
        BasicSequent(TRUE implies (TRUE or FALSE)).let {
            assertEquals(emptySet(), it.body)
            assertEquals(setOf(emptySet()), it.head)
        }
        BasicSequent(P(x) implies Q(x)).let {
            assertEquals(setOf(P(x).lit()), it.body)
            assertEquals(setOf(setOf(Q(x).lit())), it.head)
        }
        BasicSequent(!P(x) implies !Q(x)).let {
            assertEquals(setOf(P(x).neg()), it.body)
            assertEquals(setOf(setOf(Q(x).neg())), it.head)
        }
        BasicSequent((P(x) and Q(x)) implies Q(y)).let {
            assertEquals(setOf(P(x).lit(), Q(x).lit()), it.body)
            assertEquals(setOf(setOf(Q(y).lit())), it.head)
        }
        BasicSequent((P(x) and Q(x)) implies (Q(x) or (R(z) and S(z)))).let {
            assertEquals(setOf(P(x).lit(), Q(x).lit()), it.body)
            assertEquals(setOf(setOf(Q(x).lit()), setOf(R(z).lit(), S(z).lit())), it.head)
        }
        BasicSequent(TRUE implies ((P(x) and Q(x)) or (P(y) and Q(y)) or (P(z) and Q(z)))).let {
            assertEquals(emptySet(), it.body)
            assertEquals(setOf(
                    setOf(P(x).lit(), Q(x).lit())
                    , setOf(P(y).lit(), Q(y).lit())
                    , setOf(P(z).lit(), Q(z).lit())), it.head)
        }
        assertFailure(INVALID_SEQUENT_FALSE_BODY, { BasicSequent(FALSE implies TRUE) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent((P(x) or Q(x)) implies R(x)) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(P(x) implies R(x) and (Q(z) or R(z))) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(P(x) implies exists(x) { Q(x) }) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(exists(x) { Q(x) } implies P(x)) })
    }
}