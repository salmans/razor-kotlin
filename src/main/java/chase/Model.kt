package chase

import formula.Const
import formula.Func
import formula.Pred

interface WitnessTerm

/**
 * Functions
 */
data class WitnessFunc(val name: String) {
    constructor(function: Func): this(function.name)
    override fun toString(): String = this.name
}

data class WitnessConst(val name: String) : WitnessTerm {
    constructor(constant: Const): this(constant.name)
    override fun toString(): String = "'${this.name}"
}

data class Element(val index: Int) : WitnessTerm {
    override fun toString(): String = "e#${this.index}"
}

data class WitnessApp(val function: WitnessFunc, val terms: List<WitnessTerm>) : WitnessTerm{
    override fun toString(): String = "${this.function}${this.terms.joinToString (prefix = "[", postfix = "]"){ it.toString() }}"
}

data class Rel(val name: String) {
    constructor(predicate: Pred) : this(predicate.name)
    override fun toString(): String = this.name
}

data class Fact(val relation: Rel, val elements: List<Element>) {
    override fun toString(): String = "<${this.relation}${if(elements.isEmpty()) "" else ":"}${this.elements.joinToString { it.toString() }}>"
}

interface Model {
    fun getDomain(): Set<Element>
    fun getFacts(): Set<Fact>
    fun getWitnesses(element: Element): Set<WitnessTerm>
}
