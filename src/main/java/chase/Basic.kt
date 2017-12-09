package chase

import formula.*

class BasicModel : Model {
    private var domain: HashSet<Element> = HashSet()
    private var observations: HashSet<Observation> = HashSet()
    private var witnesses: HashMap<Element, Set<WitnessTerm>> = HashMap()

    override fun addElement(element: Element) {
        domain.add(element)
    }

    override fun getDomain(): Set<Element> = domain

    override fun addWitness(element: Element, witness: WitnessTerm) {
        this.witnesses.put(element, this.witnesses[element].orEmpty().plus(witness))
    }

    override fun getWitnesses(element: Element): Set<WitnessTerm> = witnesses[element] ?: emptySet()

    override fun addObservation(observation: Observation) {
        this.observations.add(observation)
    }

    override fun getObservations(): Set<Observation> = observations
}

sealed class Literal {
    abstract fun print(): String

    data class Atm(private val pred: Pred, private val terms: Terms) : Literal() {
        override fun print(): String = "$pred(${this.terms.joinToString(", ")})"
    }

    data class Eql(private val left: Term, private val right: Term) : Literal() {
        override fun print(): String = "$left = $right"
    }

    data class Neg(private val pred: Pred, private val terms: Terms) : Literal() {
        override fun print(): String = "¬$pred(${this.terms.joinToString(", ")})"
    }

    data class Neq(private val left: Term, private val right: Term) : Literal() {
        override fun print(): String = "$left ≠ $right"
    }
}


fun Atom.lit() = Literal.Atm(this.pred, this.terms)
fun Atom.neg() = Literal.Neg(this.pred, this.terms)
fun Equals.lit() = Literal.Eql(this.left, this.right)
fun Equals.neg() = Literal.Neq(this.left, this.right)

private fun buildBody(formula: Formula): List<Literal> = when (formula) {
    is Top -> emptyList()
    is Atom -> listOf(formula.lit())
    is Equals -> listOf(formula.lit())
    is And -> buildBody(formula.left) + buildBody(formula.right)
    else -> throw EXPECTED_STANDARD_SEQUENT.internalError()
}

private fun buildHead(formula: Formula): List<List<Literal>> = when (formula) {
    is Top -> listOf(emptyList())
    is Bottom -> emptyList()
    is Atom -> listOf(listOf(formula.lit()))
    is Equals -> listOf(listOf(formula.lit()))
    is And -> {
        val left = buildHead(formula.left)
        val right = buildHead(formula.right)
        if (left.isEmpty()) {
            right
        } else if (right.isEmpty()) {
            left
        } else if (left.size == 1 || right.size == 1) {
            listOf((left.firstOrNull() ?: emptyList()) + (right.firstOrNull() ?: emptySet()))
        } else {
            throw EXPECTED_STANDARD_SEQUENT.internalError()
        }
    }
    is Or -> buildHead(formula.left) + buildHead(formula.right)
    else -> throw EXPECTED_STANDARD_SEQUENT.internalError()
}

class BasicSequent(val body: List<Literal>, val head: List<List<Literal>>) : Sequent<BasicModel> {
    constructor(formula: Formula) : this(
            if (formula is Implies) buildBody(formula.left) else listOf(),
            if (formula is Implies) buildHead(formula.right) else buildHead(formula)
    )

    override fun evaluate(model: BasicModel): Set<Observation> {
        return emptySet()
    }
}