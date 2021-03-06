package chase

import formula.geometric
import formula.parseTheory
import kotlin.test.assertEquals
import kotlin.test.fail

// Elements
val e_0 = Element(0)
val e_1 = Element(1)
val e_2 = Element(2)
val e_3 = Element(3)
val e_4 = Element(4)
val e_5 = Element(5)
val e_6 = Element(6)
val e_7 = Element(7)
val e_8 = Element(8)
val e_9 = Element(9)

// Witness Constants
val _a = WitnessConst("a")
val _a1 = WitnessConst("a1")
val _a2 = WitnessConst("a2")
val _a3 = WitnessConst("a3")
val _a4 = WitnessConst("a4")
val _b = WitnessConst("b")
val _b1 = WitnessConst("b1")
val _b2 = WitnessConst("b2")
val _b3 = WitnessConst("b3")
val _b4 = WitnessConst("b4")
val _c = WitnessConst("c")
val _c1 = WitnessConst("c1")
val _c2 = WitnessConst("c2")
val _c3 = WitnessConst("c3")
val _c4 = WitnessConst("c4")
val _d = WitnessConst("d")
val _d1 = WitnessConst("d1")
val _d2 = WitnessConst("d2")
val _d3 = WitnessConst("d3")
val _d4 = WitnessConst("d4")

// Relations
val _E = Rel("E")
val _P = Rel("P")
val _Q = Rel("Q")
val _R = Rel("R")
val _S = Rel("S")

const val CORE_TEST_COUNT = 42
const val BOUNDED_TEST_COUNT = 2
const val STRESS_TEST_COUNT = 0

fun assertFailure(errorMessage: String, func: () -> Unit) {
    try {
        func()
        fail("error expected!")
    } catch (e: Exception) {
        assertEquals(errorMessage, e.message)
    }
}

fun solveBasic(source: String): List<Model> {
    val geometricTheory = source.parseTheory().geometric()
    val sequents = geometricTheory.formulas.map { BasicSequent(it) }
    val evaluator = BasicEvaluator()
    val selector = TopDownSelector(sequents)
    val strategy = FIFOStrategy<BasicSequent>().apply { add(StrategyNode(BasicModel(), selector)) }
    return solveAll(strategy, evaluator, null)
}

fun solveDomainBoundedBasic(source: String, bound: Int): List<Model> {
    val geometricTheory = source.parseTheory().geometric()
    val sequents = geometricTheory.formulas.map { BasicSequent(it) }
    val bounder = DomainSizeBounder(bound)
    val evaluator = BasicEvaluator()
    val selector = TopDownSelector(sequents)
    val strategy = FIFOStrategy<BasicSequent>().apply { add(StrategyNode(BasicModel(), selector)) }
    return solveAll(strategy, evaluator, bounder)
}

fun printModels(models: List<Model>): String {
    return models.joinToString(separator = "\n-- -- -- -- -- -- -- -- -- --\n") { it ->
        it.toString() + it.getDomain().joinToString(prefix = "\n", separator = "\n") { e -> "${it.witness(e).joinToString()} -> $e" }
    }
}