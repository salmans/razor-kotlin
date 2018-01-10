package chase

import formula.*
import org.junit.Ignore
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
            assertEquals(e_0, it.element(_c))
            assertEquals(setOf(_c), it.witness(e_0))
        }
        BasicModel().let {
            it.observe(_a equals _b)
            assertEquals(setOf(e_0), it.getDomain())
            assertEquals(emptySet(), it.getFacts())
            assertEquals(e_0, it.element(_a))
            assertEquals(e_0, it.element(_b))
            assertEquals(setOf(_a, _b), it.witness(e_0))
        }
        BasicModel().let {
            it.observe(_a equals _a)
            assertEquals(setOf(e_0), it.getDomain())
            assertEquals(emptySet(), it.getFacts())
            assertEquals(e_0, it.element(_a))
            assertEquals(setOf(_a), it.witness(e_0))
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
            assertEquals(e_0, it.element(_a))
            assertEquals(e_0, it.element(_b))
            assertEquals(setOf(_a, _b), it.witness(e_0))
        }
        BasicModel().let {
            it.observe(_R(f(_c)))
            assertEquals(setOf(e_0, e_1), it.getDomain())
            assertEquals(setOf(_R(e_1)), it.getFacts())
            assertEquals(true, it.lookup(_R(e_1)))
            assertEquals(true, it.lookup(_R(f(_c))))
            assertEquals(e_0, it.element(_c))
            assertEquals(e_1, it.element(f(_c)))
            assertEquals(setOf(_c), it.witness(e_0))
            assertEquals(setOf(f(e_0)), it.witness(e_1))
        }
        BasicModel().let {
            it.observe(_R(_a, _b))
            assertEquals(setOf(e_0, e_1), it.getDomain())
            assertEquals(setOf(_R(e_0, e_1)), it.getFacts())
            assertEquals(true, it.lookup(_R(e_0, e_1)))
            assertEquals(false, it.lookup(_R(e_0, e_0)))
            assertEquals(e_0, it.element(_a))
            assertEquals(e_1, it.element(_b))
            assertEquals(setOf(_a), it.witness(e_0))
            assertEquals(setOf(_b), it.witness(e_1))
        }
        BasicModel().let {
            it.observe(_R(f(_c), g(f(_c))))
            assertEquals(setOf(e_0, e_1, e_2), it.getDomain())
            assertEquals(setOf(_R(e_1, e_2)), it.getFacts())
            assertEquals(true, it.lookup(_R(e_1, e_2)))
            assertEquals(true, it.lookup(_R(f(_c), g(f(_c)))))
            assertEquals(true, it.lookup(_R(f(_c), e_2)))
            assertEquals(e_0, it.element(_c))
            assertEquals(e_1, it.element(f(_c)))
            assertEquals(e_2, it.element(g(f(_c))))
            assertEquals(setOf(_c), it.witness(e_0))
            assertEquals(setOf(f(e_0)), it.witness(e_1))
            assertEquals(setOf(g(e_1)), it.witness(e_2))
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

    @Test
    fun testCore() {
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>\n" +
                "'a -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy0.raz").readText())))
        assertEquals("Domain: {e#0, e#1}\n" +
                "Facts: <P(e#0)>, <P(e#1)>\n" +
                "'a -> e#0\n" +
                "'b -> e#1", printModels(solveBasic(this.javaClass.getResource("/core/thy1.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>, <Q(e#0)>\n" +
                "'a -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy2.raz").readText())))
        assertEquals("Domain: {e#0, e#1}\n" +
                "Facts: <R(e#0, e#1)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1", printModels(solveBasic(this.javaClass.getResource("/core/thy3.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: \n" +
                "'a, 'b -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy4.raz").readText())))
        assertEquals("Domain: {e#0, e#1}\n" +
                "Facts: <P(e#0, e#1)>\n" +
                "'a -> e#0\n" +
                "'b -> e#1", printModels(solveBasic(this.javaClass.getResource("/core/thy5.raz").readText())))
        assertEquals("Domain: {e#0, e#1}\n" +
                "Facts: <P(e#1)>\n" +
                "'a -> e#0\n" +
                "f[e#0] -> e#1", printModels(solveBasic(this.javaClass.getResource("/core/thy6.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>, <Q(e#0)>, <R(e#0)>\n" +
                "'a -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy7.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>\n" +
                "'a -> e#0\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0}\n" +
                "Facts: <Q(e#0)>\n" +
                "'b -> e#0\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0}\n" +
                "Facts: <R(e#0)>\n" +
                "'c -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy8.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>, <Q(e#0)>\n" +
                "'a, 'b -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy9.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>, <R(e#0)>\n" +
                "'a -> e#0\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0}\n" +
                "Facts: <Q(e#0)>, <S(e#0)>\n" +
                "'b -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy10.raz").readText())))
        assertEquals("Domain: {}\n" +
                "Facts: \n", printModels(solveBasic(this.javaClass.getResource("/core/thy11.raz").readText())))
        assertEquals("Domain: {}\n" +
                "Facts: \n", printModels(solveBasic(this.javaClass.getResource("/core/thy12.raz").readText())))
        assertEquals("", printModels(solveBasic(this.javaClass.getResource("/core/thy13.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <Q(e#0)>\n" +
                "'b -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy14.raz").readText())))
        assertEquals("", printModels(solveBasic(this.javaClass.getResource("/core/thy15.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0, e#0)>, <Q(e#0)>\n" +
                "'c -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy16.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2}\n" +
                "Facts: <P(e#0, e#0)>, <P(e#1, e#2)>, <Q(e#0)>\n" +
                "'c -> e#0\n" +
                "'a -> e#1\n" +
                "'b -> e#2", printModels(solveBasic(this.javaClass.getResource("/core/thy17.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2}\n" +
                "Facts: <P(e#0, e#1)>, <P(e#2, e#2)>, <Q(e#2)>\n" +
                "'a -> e#0\n" +
                "'b -> e#1\n" +
                "'c -> e#2", printModels(solveBasic(this.javaClass.getResource("/core/thy18.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9, e#10}\n" +
                "Facts: \n" +
                "'a -> e#0\n" +
                "f[e#0] -> e#1\n" +
                "f[e#1] -> e#2\n" +
                "f[e#2] -> e#3\n" +
                "f[e#3] -> e#4\n" +
                "f[e#4] -> e#5\n" +
                "f[e#5] -> e#6\n" +
                "f[e#6] -> e#7\n" +
                "f[e#7] -> e#8\n" +
                "f[e#8] -> e#9\n" +
                "f[e#9], 'b -> e#10", printModels(solveBasic(this.javaClass.getResource("/core/thy19.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9, e#10}\n" +
                "Facts: <P(e#0)>, <P(e#1)>, <P(e#2)>, <P(e#3)>, <P(e#4)>, <P(e#5)>, <P(e#6)>, <P(e#7)>, <P(e#8)>, <P(e#9)>\n" +
                "'a -> e#0\n" +
                "f[e#0] -> e#1\n" +
                "f[e#1] -> e#2\n" +
                "f[e#2] -> e#3\n" +
                "f[e#3] -> e#4\n" +
                "f[e#4] -> e#5\n" +
                "f[e#5] -> e#6\n" +
                "f[e#6] -> e#7\n" +
                "f[e#7] -> e#8\n" +
                "f[e#8] -> e#9\n" +
                "f[e#9], 'b -> e#10", printModels(solveBasic(this.javaClass.getResource("/core/thy20.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9, e#10}\n" +
                "Facts: <P(e#0)>, <P(e#1)>, <P(e#2)>, <P(e#3)>, <P(e#4)>, <P(e#5)>, <P(e#6)>, <P(e#7)>, <P(e#8)>\n" +
                "'a -> e#0\n" +
                "f[e#0] -> e#1\n" +
                "f[e#1] -> e#2\n" +
                "f[e#2] -> e#3\n" +
                "f[e#3] -> e#4\n" +
                "f[e#4] -> e#5\n" +
                "f[e#5] -> e#6\n" +
                "f[e#6] -> e#7\n" +
                "f[e#7] -> e#8\n" +
                "f[e#8] -> e#9\n" +
                "f[e#9], 'b -> e#10", printModels(solveBasic(this.javaClass.getResource("/core/thy21.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>, <Q(e#0)>, <R(e#0)>\n" +
                "'a -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy22.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>, <Q(e#0)>, <R(e#0)>, <S(e#0)>\n" +
                "'sk#0, 'sk#1, 'sk#2 -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy23.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>, <Q(e#0)>, <R(e#0)>, <S(e#0)>, <T(e#0)>\n" +
                "'sk#0, 'sk#1, 'sk#2, 'sk#3 -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy24.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3}\n" +
                "Facts: <P(e#0)>, <Q(e#1)>, <R(e#2)>, <S(e#3)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3", printModels(solveBasic(this.javaClass.getResource("/core/thy25.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <P(e#0)>\n" +
                "'sk#0 -> e#0\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0}\n" +
                "Facts: <P(e#0)>\n" +
                "'sk#1 -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy26.raz").readText())))
        assertEquals("", printModels(solveBasic(this.javaClass.getResource("/core/thy27.raz").readText())))
        assertEquals("Domain: {}\n" +
                "Facts: <T()>, <V()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <U()>, <V()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <T()>, <U()>, <V()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <U()>, <T()>, <V()>\n", printModels(solveBasic(this.javaClass.getResource("/core/thy28.raz").readText())))
        assertEquals("Domain: {}\n" +
                "Facts: <P()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <Q()>, <S()>, <W()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <Q()>, <S()>, <X()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <Q()>, <S()>, <Y()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <Q()>, <R()>, <T()>, <V()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <Q()>, <R()>, <U()>, <V()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <Q()>, <R()>, <T()>, <U()>, <V()>\n" +
                "\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {}\n" +
                "Facts: <Q()>, <R()>, <U()>, <T()>, <V()>\n", printModels(solveBasic(this.javaClass.getResource("/core/thy29.raz").readText())))
        assertEquals("", printModels(solveBasic(this.javaClass.getResource("/core/thy30.raz").readText())))
        assertEquals("Domain: {e#0}\n" +
                "Facts: <R(e#0)>, <U(e#0)>, <Q(e#0, e#0)>\n" +
                "'sk#0 -> e#0", printModels(solveBasic(this.javaClass.getResource("/core/thy31.raz").readText())))
        assertEquals("Domain: {e#0, e#1}\n" +
                "Facts: <R(e#0)>, <Q(e#0, e#1)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1", printModels(solveBasic(this.javaClass.getResource("/core/thy32.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2}\n" +
                "Facts: <Q(e#0)>, <R(e#0, e#0)>, <S(e#1)>, <Q(e#2)>, <R(e#2, e#2)>, <R(e#2, e#0)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2}\n" +
                "Facts: <Q(e#0)>, <R(e#0, e#0)>, <S(e#1)>, <Q(e#2)>, <S(e#2)>, <R(e#2, e#2)>, <R(e#2, e#0)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2}\n" +
                "Facts: <Q(e#0)>, <S(e#0)>, <R(e#0, e#0)>, <S(e#1)>, <Q(e#2)>, <R(e#2, e#2)>, <R(e#2, e#0)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2}\n" +
                "Facts: <Q(e#0)>, <S(e#0)>, <R(e#0, e#0)>, <S(e#1)>, <Q(e#2)>, <S(e#2)>, <R(e#2, e#2)>, <R(e#2, e#0)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2", printModels(solveBasic(this.javaClass.getResource("/core/thy33.raz").readText())))
        assertEquals("Domain: {e#0, e#1}\n" +
                "Facts: <P(e#0)>, <P(e#1)>\n" +
                "'a -> e#0\n" +
                "'sk#0 -> e#1", printModels(solveBasic(this.javaClass.getResource("/core/thy34.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P11(e#2)>, <P111(e#3)>, <P1111(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#7 -> e#3\n" +
                "'sk#15 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P11(e#2)>, <P111(e#3)>, <P1112(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#7 -> e#3\n" +
                "'sk#15 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P11(e#2)>, <P112(e#3)>, <P1121(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#7 -> e#3\n" +
                "'sk#17 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P11(e#2)>, <P112(e#3)>, <P1122(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#7 -> e#3\n" +
                "'sk#17 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P12(e#2)>, <P121(e#3)>, <P1211(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#9 -> e#3\n" +
                "'sk#19 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P12(e#2)>, <P121(e#3)>, <P1212(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#9 -> e#3\n" +
                "'sk#19 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P12(e#2)>, <P122(e#3)>, <P1221(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#9 -> e#3\n" +
                "'sk#21 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P1(e#1)>, <P12(e#2)>, <P122(e#3)>, <P1222(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#3 -> e#2\n" +
                "'sk#9 -> e#3\n" +
                "'sk#21 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P21(e#2)>, <P211(e#3)>, <P2111(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#11 -> e#3\n" +
                "'sk#23 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P21(e#2)>, <P211(e#3)>, <P2112(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#11 -> e#3\n" +
                "'sk#23 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P21(e#2)>, <P212(e#3)>, <P2121(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#11 -> e#3\n" +
                "'sk#25 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P21(e#2)>, <P212(e#3)>, <P2122(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#11 -> e#3\n" +
                "'sk#25 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P22(e#2)>, <P221(e#3)>, <P2211(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#13 -> e#3\n" +
                "'sk#27 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P22(e#2)>, <P221(e#3)>, <P2212(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#13 -> e#3\n" +
                "'sk#27 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P22(e#2)>, <P222(e#3)>, <P2221(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#13 -> e#3\n" +
                "'sk#29 -> e#4\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P2(e#1)>, <P22(e#2)>, <P222(e#3)>, <P2222(e#4)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#5 -> e#2\n" +
                "'sk#13 -> e#3\n" +
                "'sk#29 -> e#4", printModels(solveBasic(this.javaClass.getResource("/core/thy35.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q11(e#4, e#5)>, <Q111(e#6, e#7)>, <Q1111(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#14 -> e#6\n" +
                "'sk#15 -> e#7\n" +
                "'sk#30 -> e#8\n" +
                "'sk#31 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q11(e#4, e#5)>, <Q111(e#6, e#7)>, <Q1112(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#14 -> e#6\n" +
                "'sk#15 -> e#7\n" +
                "'sk#30 -> e#8\n" +
                "'sk#31 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q11(e#4, e#5)>, <Q112(e#6, e#7)>, <Q1121(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#14 -> e#6\n" +
                "'sk#15 -> e#7\n" +
                "'sk#34 -> e#8\n" +
                "'sk#35 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q11(e#4, e#5)>, <Q112(e#6, e#7)>, <Q1122(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#14 -> e#6\n" +
                "'sk#15 -> e#7\n" +
                "'sk#34 -> e#8\n" +
                "'sk#35 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q12(e#4, e#5)>, <Q121(e#6, e#7)>, <Q1211(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#18 -> e#6\n" +
                "'sk#19 -> e#7\n" +
                "'sk#38 -> e#8\n" +
                "'sk#39 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q12(e#4, e#5)>, <Q121(e#6, e#7)>, <Q1212(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#18 -> e#6\n" +
                "'sk#19 -> e#7\n" +
                "'sk#38 -> e#8\n" +
                "'sk#39 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q12(e#4, e#5)>, <Q122(e#6, e#7)>, <Q1221(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#18 -> e#6\n" +
                "'sk#19 -> e#7\n" +
                "'sk#42 -> e#8\n" +
                "'sk#43 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q1(e#2, e#3)>, <Q12(e#4, e#5)>, <Q122(e#6, e#7)>, <Q1222(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#6 -> e#4\n" +
                "'sk#7 -> e#5\n" +
                "'sk#18 -> e#6\n" +
                "'sk#19 -> e#7\n" +
                "'sk#42 -> e#8\n" +
                "'sk#43 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q21(e#4, e#5)>, <Q211(e#6, e#7)>, <Q2111(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#22 -> e#6\n" +
                "'sk#23 -> e#7\n" +
                "'sk#46 -> e#8\n" +
                "'sk#47 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q21(e#4, e#5)>, <Q211(e#6, e#7)>, <Q2112(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#22 -> e#6\n" +
                "'sk#23 -> e#7\n" +
                "'sk#46 -> e#8\n" +
                "'sk#47 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q21(e#4, e#5)>, <Q212(e#6, e#7)>, <Q2121(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#22 -> e#6\n" +
                "'sk#23 -> e#7\n" +
                "'sk#50 -> e#8\n" +
                "'sk#51 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q21(e#4, e#5)>, <Q212(e#6, e#7)>, <Q2122(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#22 -> e#6\n" +
                "'sk#23 -> e#7\n" +
                "'sk#50 -> e#8\n" +
                "'sk#51 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q22(e#4, e#5)>, <Q221(e#6, e#7)>, <Q2211(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#26 -> e#6\n" +
                "'sk#27 -> e#7\n" +
                "'sk#54 -> e#8\n" +
                "'sk#55 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q22(e#4, e#5)>, <Q221(e#6, e#7)>, <Q2212(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#26 -> e#6\n" +
                "'sk#27 -> e#7\n" +
                "'sk#54 -> e#8\n" +
                "'sk#55 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q22(e#4, e#5)>, <Q222(e#6, e#7)>, <Q2221(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#26 -> e#6\n" +
                "'sk#27 -> e#7\n" +
                "'sk#58 -> e#8\n" +
                "'sk#59 -> e#9\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9}\n" +
                "Facts: <Q(e#0, e#1)>, <Q2(e#2, e#3)>, <Q22(e#4, e#5)>, <Q222(e#6, e#7)>, <Q2222(e#8, e#9)>\n" +
                "'sk#0 -> e#0\n" +
                "'sk#1 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "'sk#3 -> e#3\n" +
                "'sk#10 -> e#4\n" +
                "'sk#11 -> e#5\n" +
                "'sk#26 -> e#6\n" +
                "'sk#27 -> e#7\n" +
                "'sk#58 -> e#8\n" +
                "'sk#59 -> e#9", printModels(solveBasic(this.javaClass.getResource("/core/thy36.raz").readText())))
        assertEquals("", printModels(solveBasic(this.javaClass.getResource("/core/thy37.raz").readText())))
        assertEquals("Domain: {e#2}\n" +
                "Facts: <R(e#2, e#2, e#2)>\n" +
                "'sk#0, 'sk#1, 'sk#2 -> e#2", printModels(solveBasic(this.javaClass.getResource("/core/thy38.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6}\n" +
                "Facts: <Q(e#1)>, <R(e#1, e#6)>\n" +
                "'sk#0 -> e#0\n" +
                "f[e#0] -> e#1\n" +
                "f[e#1] -> e#2\n" +
                "f[e#2] -> e#3\n" +
                "f[e#3] -> e#4\n" +
                "f[e#4] -> e#5\n" +
                "f[e#5] -> e#6", printModels(solveBasic(this.javaClass.getResource("/core/thy39.raz").readText())))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <Q(e#1)>, <R(e#0, e#1)>, <R(e#1, e#3)>, <S(e#4)>, <P(e#1)>\n" +
                "'sk#0 -> e#0\n" +
                "f[e#0] -> e#1\n" +
                "f[e#1] -> e#2\n" +
                "f[e#2] -> e#3\n" +
                "'sk#1 -> e#4", printModels(solveBasic(this.javaClass.getResource("/core/thy40.raz").readText())))
        assertEquals("Domain: {}\n" +
                "Facts: \n", printModels(solveBasic(this.javaClass.getResource("/core/thy41.raz").readText())))
    }

    @Test
    fun testExamples() {
        assertEquals("Domain: {e#0}\n" +
                "Facts: <Man(e#0)>, <MustDie(e#0)>\n" +
                "'gregor -> e#0", printModels(solveBasic(this.javaClass.getResource("/examples/valar-morghulis.raz").readText())))
        assertEquals("", printModels(solveBasic(this.javaClass.getResource("/examples/lannisters.raz").readText())))
        assertEquals("Domain: {e#3, e#1, e#2}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3 -> e#3\n" +
                "'sk#0 -> e#1\n" +
                "'sk#1 -> e#2\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#3, e#1, e#2}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3 -> e#3\n" +
                "'sk#0 -> e#1\n" +
                "'sk#2 -> e#2\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#3, e#1}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3, father[e#3], 'sk#1 -> e#3\n" +
                "'sk#0 -> e#1\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#3, e#1}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3, father[e#3], mother[e#3], 'sk#2 -> e#3\n" +
                "'sk#0 -> e#1\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#3, e#1}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3, father[e#3], 'sk#1 -> e#3\n" +
                "'sk#0 -> e#1\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#3, e#1}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3, father[e#3], mother[e#3], 'sk#2 -> e#3\n" +
                "'sk#0 -> e#1\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#3, e#1}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3, father[e#3], mother[e#3], 'sk#1 -> e#3\n" +
                "'sk#0 -> e#1\n" +
                "-- -- -- -- -- -- -- -- -- --\n" +
                "Domain: {e#3, e#1}\n" +
                "Facts: <Grandpas(e#3, e#3)>, <Parent(e#1, e#3)>, <Grandpas(e#1, e#3)>, <Grandpas(e#3, e#1)>, <Grandpas(e#1, e#1)>, <Parent(e#1, e#1)>, <Parent(e#3, e#1)>, <Man(e#3)>, <Person(e#3)>, <Man(e#1)>, <Person(e#1)>, <Parent(e#3, e#3)>\n" +
                "'sk#3, father[e#3], mother[e#3], 'sk#2 -> e#3\n" +
                "'sk#0 -> e#1", printModels(solveDomainBoundedBasic(this.javaClass.getResource("/examples/grandpa.raz").readText(), 4)))
        assertEquals("", printModels(solveDomainBoundedBasic(this.javaClass.getResource("/examples/grandpa.raz").readText(), 5)))
        assertEquals("", printModels(solveDomainBoundedBasic(this.javaClass.getResource("/examples/grandpa.raz").readText(), 6)))
        assertEquals("", printModels(solveBasic(this.javaClass.getResource("/examples/grandpa.raz").readText())))
    }

    @Test
    fun testBounded() {
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4}\n" +
                "Facts: <P(e#0)>, <P(e#1)>, <P(e#2)>, <P(e#3)>, <P(e#4)>\n" +
                "'a -> e#0\n" +
                "f[e#0] -> e#1\n" +
                "f[e#1] -> e#2\n" +
                "f[e#2] -> e#3\n" +
                "f[e#3] -> e#4", printModels(solveDomainBoundedBasic(this.javaClass.getResource("/bounded/thy0.raz").readText(), 5)))
        assertEquals("Domain: {e#0, e#1, e#2, e#3, e#4, e#5, e#6, e#7, e#8, e#9, e#10, e#11, e#12, e#13, e#14, e#15, e#16, e#17, e#18, e#19}\n" +
                "Facts: <P(e#0)>, <P(e#1)>, <P(e#2)>, <P(e#3)>, <P(e#4)>, <P(e#5)>, <P(e#6)>, <P(e#7)>, <P(e#8)>, <P(e#9)>, <P(e#10)>, <P(e#11)>, <P(e#12)>, <P(e#13)>, <P(e#14)>, <P(e#15)>, <P(e#16)>, <P(e#17)>, <P(e#18)>, <P(e#19)>\n" +
                "'a -> e#0\n" +
                "f[e#0] -> e#1\n" +
                "f[e#1] -> e#2\n" +
                "f[e#2] -> e#3\n" +
                "f[e#3] -> e#4\n" +
                "f[e#4] -> e#5\n" +
                "f[e#5] -> e#6\n" +
                "f[e#6] -> e#7\n" +
                "f[e#7] -> e#8\n" +
                "f[e#8] -> e#9\n" +
                "f[e#9] -> e#10\n" +
                "f[e#10] -> e#11\n" +
                "f[e#11] -> e#12\n" +
                "f[e#12] -> e#13\n" +
                "f[e#13] -> e#14\n" +
                "f[e#14] -> e#15\n" +
                "f[e#15] -> e#16\n" +
                "f[e#16] -> e#17\n" +
                "f[e#17] -> e#18\n" +
                "f[e#18] -> e#19", printModels(solveDomainBoundedBasic(this.javaClass.getResource("/bounded/thy0.raz").readText(), 20)))
        assertEquals("Domain: {e#4, e#5, e#7, e#9}\n" +
                "Facts: \n" +
                "'sk#0, e[], f[e#4, e#4], f[e#4, e#5] -> e#4\n" +
                "i[e#4], f[e#5, e#4] -> e#5\n" +
                "f[e#5, e#5], f[e#7, e#4] -> e#7\n" +
                "f[e#7, e#5] -> e#9", printModels(solveDomainBoundedBasic(this.javaClass.getResource("/bounded/thy1.raz").readText(), 5)))
    }
}