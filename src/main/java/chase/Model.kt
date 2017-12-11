package chase

import formula.Const
import formula.Func
import formula.Pred

/**
 * Elements of a model
 */
data class Element(private val index: Int) {
    override fun toString(): String = "e#${this.index}"
}

/**
 * Witness Terms: witness terms are used to justify an element in the model.
 */
interface WitnessTerm {
    override fun toString(): String
}

/**
 * Witness functions are used to create complex witness terms.
 */
data class WitnessFunc(private val name: String) {
    constructor(function: Func) : this(function.name) // create a witness function for an existing function

    override fun toString(): String = this.name
}

/**
 * Witness constants
 */
data class WitnessConst(private val name: String) : WitnessTerm {
    constructor(constant: Const) : this(constant.name) // create a witness constant for an existing constant

    override fun toString(): String = "'${this.name}"
}

data class WitnessApp(private val function: WitnessFunc, private val terms: List<WitnessTerm> = emptyList()) : WitnessTerm {
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

    data class Fact(private val relation: Rel, private val elements: List<Element>) : Observation() {
        override fun toString(): String = "<${this.relation}${this.elements.joinToString(prefix = "(", postfix = ")") { it.toString() }}>"
    }

    data class Identity(private val left: Element, private val right: Element) : Observation() {
        override fun toString(): String = "<$left = $right>"
    }
}

/**
 * Models
 */
interface Model<out M> {
    /**
     * Returns a copy of this model (for branching purposed)
     */
    fun clone(): M

    /**
     * Add an element to the model.
     */
    fun addElement(element: Element)

    /**
     * Get the set of elements in the domain of a model.
     */
    fun getDomain(): Set<Element>

    /**
     * Add a witness term for a given element.
     */
    fun addWitness(element: Element, witness: WitnessTerm)

    /**
     * Get the set of witnesses for a given element.
     */
    fun getWitnesses(element: Element): Set<WitnessTerm>

    /**
     * Add an observation to the model.
     */
    fun addObservation(observation: Observation)

    /**
     * Return the set of observations in the model.
     */
    fun getObservations(): Set<Observation>
}
