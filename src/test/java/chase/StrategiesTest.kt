package chase

import formula.geometric
import formula.parseTheory
import org.junit.Test
import kotlin.test.assertEquals

class StrategiesTest {
    @Test
    fun testLIFOBasic() {
        fun runTest(source: String): List<Model> {
            val geometricTheory = source.parseTheory()!!.geometric()
            val sequents = geometricTheory.formulas.map { BasicSequent(it) }
            val evaluator = BasicEvaluator()
            val selector = FairSelector(sequents.toTypedArray())
            val strategy = LIFOStrategy<BasicSequent>().apply { add(StrategyNode(BasicModel(), selector)) }
            return solveAll(strategy, evaluator, null)
        }

        (0 .. CORE_TEST_COUNT)
                .map { this.javaClass.getResource("/core/thy$it.raz").readText() }
                .forEach { assertEquals(solveBasic(it).toSet(), runTest(it).toSet()) }
    }
}