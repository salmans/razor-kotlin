package chase

import formula.*
import org.junit.Test
import kotlin.test.assertEquals

class BasicTest {
    @Test
    fun emptyModel() {
        BasicModel().let {
            assertEquals(emptySet(), it.getDomain())
            assertEquals(emptySet(), it.getFacts())
        }
    }

    @Test
    fun observe() {
        BasicModel().let {
            it.observe(_R())
            assertEquals(setOf(_R()), it.getFacts())
            assertEquals(true, it.lookup(_R()))
        }
        BasicModel().let {
            it.observe(_R(_c))
            assertEquals(setOf(e_0), it.getDomain())
            assertEquals(setOf(_R(e_0)), it.getFacts())
            assertEquals(true, it.lookup(_R(_c)))
            assertEquals(true, it.lookup(_R(e_0)))
            assertEquals(false, it.lookup(_R(e_1)))
        }
        BasicModel().let {
            it.observe(_a equals _b)
            assertEquals(setOf(e_0), it.getDomain())
            assertEquals(emptySet(), it.getFacts())
        }
        BasicModel().let {
            it.observe(_a equals _a)
            assertEquals(setOf(e_0), it.getDomain())
            assertEquals(emptySet(), it.getFacts())
        }
        BasicModel().let {
            it.observe(_P(_a))
            it.observe(_Q(_b))
            it.observe(_a equals _b)
            assertEquals(setOf(e_0), it.getDomain())
            assertEquals(setOf(_P(e_0), _Q(e_0)), it.getFacts())
            assertEquals(true, it.lookup(_P(e_0)))
            assertEquals(true, it.lookup(_Q(e_0)))
            assertEquals(true, it.lookup(_P(_a)))
            assertEquals(true, it.lookup(_Q(_b)))
        }
        BasicModel().let {
            it.observe(_R(f(_c)))
            assertEquals(setOf(e_0, e_1), it.getDomain())
            assertEquals(setOf(_R(e_1)), it.getFacts())
            assertEquals(true, it.lookup(_R(e_1)))
            assertEquals(true, it.lookup(_R(f(_c))))
        }
        BasicModel().let {
            it.observe(_R(_a, _b))
            assertEquals(setOf(e_0, e_1), it.getDomain())
            assertEquals(setOf(_R(e_0, e_1)), it.getFacts())
            assertEquals(true, it.lookup(_R(e_0, e_1)))
            assertEquals(false, it.lookup(_R(e_0, e_0)))
        }
        BasicModel().let {
            it.observe(_R(f(_c), g(f(_c))))
            assertEquals(setOf(e_0, e_1, e_2), it.getDomain())
            assertEquals(setOf(_R(e_1, e_2)), it.getFacts())
            assertEquals(true, it.lookup(_R(e_1, e_2)))
            assertEquals(true, it.lookup(_R(f(_c), g(f(_c)))))
            assertEquals(true, it.lookup(_R(f(_c), e_2)))
        }
        BasicModel().let {
            it.observe(_R(_a, _b))
            it.observe(_S(_c, _d))
            assertEquals(setOf(e_0, e_1, e_2, e_3), it.getDomain())
            assertEquals(setOf(_R(e_0, e_1), _S(e_2, e_3)), it.getFacts())
        }
        BasicModel().let {
            it.observe(_R(_a, f(_a)))
            it.observe(_S(_b))
            it.observe(_R(g(f(_a)), _b))
            it.observe(_S(_c))
            assertEquals(setOf(e_0, e_1, e_2, e_3, e_4), it.getDomain())
            assertEquals(setOf(_R(e_0, e_1), _S(e_4), _S(e_2), _R(e_3, e_2)), it.getFacts())
            assertEquals(true, it.lookup(_R(e_0, e_1)))
            assertEquals(true, it.lookup(_S(e_2)))
            assertEquals(true, it.lookup(_R(e_3, e_2)))
            assertEquals(true, it.lookup(_S(e_4)))
            assertEquals(true, it.lookup(_R(_a, f(_a))))
            assertEquals(true, it.lookup(_S(_b)))
            assertEquals(true, it.lookup(_R(g(f(_a)), _b)))
            assertEquals(true, it.lookup(_S(_c)))
        }
        BasicModel().let {
            assertFailure("The element is not in the domain of this model.", { it.observe(_R(e_0)) })
        }
    }

    @Test
    fun testDuplicateModel() {
        run {
            val original = BasicModel()
            original.duplicate().let {
                assertEquals(it, original)
            }
        }
        run {
            val original = BasicModel().apply {
                observe(_R(_a, _b))
                observe(_S(_c))
                observe(_S(_d))
            }
            original.duplicate().let {
                assertEquals(it, original)
            }
        }
    }

    @Test
    fun lit() {
        assertEquals("P(x)", P(x).lit().toString())
        assertEquals("P(f(x), g(y))", P(f(x), g(y)).lit().toString())
        assertEquals("x = y", (x equals y).lit().toString())
        assertEquals("f(x) = g(y)", (f(x) equals g(y)).lit().toString())
    }

    @Test
    fun testBuildSequent() {
        BasicSequent(TRUE implies TRUE).let {
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
        BasicSequent(TRUE implies (FALSE and TRUE)).let {
            assertEquals(emptyList(), it.body)
            assertEquals(listOf(emptyList()), it.head)
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
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(TRUE) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(FALSE) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(FALSE implies TRUE) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent((P(x) or Q(x)) implies R(x)) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(P(x) implies R(x) and (Q(z) or R(z))) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(P(x) implies exists(x) { Q(x) }) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(exists(x) { Q(x) } implies P(x)) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(TRUE implies !FALSE) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(TRUE implies !TRUE) })
        assertFailure(EXPECTED_STANDARD_SEQUENT, { BasicSequent(!P(x) implies !Q(x)) })
    }
}