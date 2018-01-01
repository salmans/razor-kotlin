package chase

import formula.*

/**
 * Witness Terms: witness terms are used to justify an element in the model.
 */
sealed class WitnessTerm {
    abstract override fun toString(): String
}

/**
 * Witness constants
 */
data class WitnessConst(private val name: String) : WitnessTerm() {
    constructor(constant: Const) : this(constant.name) // create a witness constant for an existing constant

    override fun toString(): String = "'${this.name}"
}

/**
 * Elements of a model
 */
open class Element(private var index: Int) : WitnessTerm() {
    override fun toString(): String = "e#${this.index}"

    override fun equals(other: Any?): Boolean = when (other) {
        is Element -> this.index == other.index
        else -> false
    }

    override fun hashCode(): Int {
        return index
    }

    /**
     * Collapse this element with `element`
     */
    fun collapse(element: Element) {
        index = element.index
    }
}

/**
 * Witness function application
 */
data class WitnessApp(val function: Func, val terms: List<WitnessTerm> = emptyList()) : WitnessTerm() {
    override fun toString(): String = "${this.function}${this.terms.joinToString(prefix = "[", postfix = "]") { it.toString() }}"
}

/**
 * Relations are used to construct observations. Relations are semantic counterparts of predicates.
 */
data class Rel(private val name: String) {
    constructor(predicate: Pred) : this(predicate.name)

    override fun toString(): String = this.name
}

/**
 * Observations: observations are *positive* facts that are true in the model.
 */
sealed class Observation {
    data class Fact(private val relation: Rel, val terms: List<WitnessTerm>) : Observation() {
        override fun toString(): String = "<${this.relation}${this.terms.joinToString(prefix = "(", postfix = ")") { it.toString() }}>"
    }

    data class Identity(val left: WitnessTerm, val right: WitnessTerm) : Observation() {
        override fun toString(): String = "<$left = $right>"
    }
}

/**
 * Models
 */
abstract class Model<out M> {
    /**
     * A collection of elements in this model
     */
    abstract val elements: Collection<Element>

    /**
     * A collection of facts in this model
     */
    abstract val facts: Collection<Observation.Fact>

    /**
     * Returns a copy of this model (for branching purposed)
     */
    abstract fun duplicate(): M

    /**
     * Add an observation to the model.
     */
    abstract fun observe(observation: Observation)

    /**
     * Lookup an observatin in the model.
     */
    abstract fun lookup(observation: Observation): Boolean
}


interface Sequent<out M : Model<M>>

abstract class Strategy<M : Model<M>> : Iterable<M> {
    abstract override fun iterator(): Iterator<M>
    abstract fun add(model: M): Boolean
    abstract fun remove(model: M): Boolean
}

interface Evaluator<M : Model<M>, S : Sequent<M>> {
    fun evaluate(model: M): List<M>?
}

abstract class Chase<M : Model<M>, S : Sequent<M>, out SL : Evaluator<M, S>, out ST : Strategy<M>> {
    abstract fun emptyModel(): M
    abstract fun newSequent(formula: Formula): S
    abstract fun newEvaluator(sequents: List<S>): SL
    abstract fun newStrategy(): ST
}

fun <M : Model<M>, S : Sequent<M>, SL : Evaluator<M, S>, ST : Strategy<M>> solve(theory: Theory, chase: Chase<M, S, SL, ST>) {
    val geometricTheory = theory.geometric()
    val sequents = geometricTheory.formulas.map { chase.newSequent(it) }
    val evaluator = chase.newEvaluator(sequents)
    val strategy = chase.newStrategy().apply { add(chase.emptyModel()) }


    while (strategy.iterator().hasNext()) {
        val model = strategy.iterator().next()
        strategy.remove(model)
        val models = evaluator.evaluate(model)

        if (models != null) {
            if (!models.isEmpty()) {
                models.forEach {
                    strategy.add(it)
                }
            } else {
                print(model.elements)
                print(model.facts)
            }
        }
    }
}