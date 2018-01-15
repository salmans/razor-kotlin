package chase

import formula.geometric
import formula.parseTheory
import org.junit.Test
import kotlin.test.assertEquals

class SelectorsTest {
    @Test
    fun testFairBasic() {
        fun runTest(source: String): List<Model> {
            val geometricTheory = source.parseTheory()!!.geometric()
            val sequents = geometricTheory.formulas.map { BasicSequent(it) }
            val evaluator = BasicEvaluator()
            val selector = FairSelector(sequents.toTypedArray())
            val strategy = FIFOStrategy<BasicSequent>().apply { add(StrategyNode(BasicModel(), selector)) }
            return solveAll(strategy, evaluator, null)
        }

        (0 .. CORE_TEST_COUNT)
                .map { this.javaClass.getResource("/core/thy$it.raz").readText() }
                .forEach { assertEquals(solveBasic(it).toSet(), runTest(it).toSet()) }
    }

    @Test
    fun testOptimalBasic() {
        fun runTest(source: String): List<Model> {
            val geometricTheory = source.parseTheory()!!.geometric()
            val sequents = geometricTheory.formulas.map { BasicSequent(it) }
            val evaluator = BasicEvaluator()
            val selector = OptimalSelector(sequents, { ss -> FairSelector(ss.toTypedArray()) })
            val strategy = FIFOStrategy<BasicSequent>().apply { add(StrategyNode(BasicModel(), selector)) }
            return solveAll(strategy, evaluator, null)
        }

        (0 .. CORE_TEST_COUNT)
                .map { this.javaClass.getResource("/core/thy$it.raz").readText() }
                .forEach { assertEquals(solveBasic(it).toSet(), runTest(it).toSet()) }
    }
}