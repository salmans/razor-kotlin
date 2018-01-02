package chase

import formula.Const
import formula.Func
import formula.Pred
import java.util.*

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
data class Element(private var index: Int) : WitnessTerm() {
    override fun toString(): String = "e#${this.index}"

    override fun equals(other: Any?): Boolean = when (other) {
        is Element -> this.index == other.index
        else -> false
    }

    override fun hashCode(): Int {
        return index
    }

    /**
     * Collapse this element with `element`, so they would be equal.
     * This method is used to identify elements for equality reasoning.
     */
    fun collapse(element: Element) {
        index = element.index
    }
}

/**
 * Witness function application
 */
data class WitnessApp(private val function: Func, val terms: List<WitnessTerm> = emptyList()) : WitnessTerm() {
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
     * Returns the domain of this model.
     */
    abstract fun getDomain(): Set<Element>

    /**
     * Returns the set of facts in this model.
     */
    abstract fun getFacts(): Set<Observation.Fact>

    /**
     * Returns a copy of this model (for branching purposed)
     */
    abstract fun duplicate(): M

    /**
     * Add an observation to the model.
     */
    abstract fun observe(observation: Observation)

    /**
     * Lookup an observation in the model.
     */
    abstract fun lookup(observation: Observation): Boolean
}

interface Sequent

interface Strategy<M : Model<M>> : Iterable<M> {
    override fun iterator(): Iterator<M>
    fun add(model: M): Boolean
    fun remove(model: M): Boolean
}

interface Evaluator<M : Model<M>, S : Sequent> {
    fun evaluate(model: M): List<M>?
}

fun <M : Model<M>, S : Sequent> solveAll(strategy: Strategy<M>, evaluator: Evaluator<M, S>): List<M> {
    val result = LinkedList<M>()
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
                result.add(model)
            }
        }
    }

    return result
}