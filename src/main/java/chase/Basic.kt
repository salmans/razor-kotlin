package chase

import formula.*

class BasicModel() : Model<BasicModel> {
    private var elementIndex = 0

    override fun nextElement() = Element(elementIndex++)

    private var domain: HashSet<Element> = HashSet()
    private var facts: HashSet<Observation.Fact> = HashSet()
    private var witnesses: HashMap<Element, Set<WitnessTerm>> = HashMap()

    constructor(model: BasicModel) : this() {
        this.domain.addAll(model.domain)
        this.facts.addAll(model.facts)
        this.witnesses.putAll(model.witnesses)
    }

    override fun getElement(witness: WitnessTerm): Element? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDomain(): Set<Element> = domain

    override fun addWitness(element: Element, witness: WitnessTerm) {
        this.witnesses.put(element, this.witnesses[element].orEmpty().plus(witness))
    }

    override fun getWitnesses(element: Element): Set<WitnessTerm> = witnesses[element] ?: emptySet()

    override fun addObservation(observation: Observation) {
        when (observation) {
            is Observation.Fact -> this.facts.add(observation)
            is Observation.Identity -> TODO()
        }
    }

    override fun getObservations(): Set<Observation> = facts

    override fun duplicate(): BasicModel = BasicModel(this)
}

sealed class Literal {
    abstract fun print(): String

    data class Atm(private val pred: Pred, val terms: Terms) : Literal() {
        override fun print(): String = "$pred(${this.terms.joinToString(", ")})"
    }

    data class Eql(val left: Term, val right: Term) : Literal() {
        override fun print(): String = "$left = $right"
    }
}

fun Atom.lit() = Literal.Atm(this.pred, this.terms)
fun Equals.lit() = Literal.Eql(this.left, this.right)


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

class BasicSequent(formula: Formula) : Sequent<BasicModel> {
    val freeVars = formula.freeVars
    val body: List<Literal> = if (formula is Implies) buildBody(formula.left) else throw EXPECTED_STANDARD_SEQUENT.internalError()
    val head: List<List<Literal>> = if (formula is Implies) buildHead(formula.right) else throw EXPECTED_STANDARD_SEQUENT.internalError()

    private fun totalSubs(domainSize: Int, varSize: Int): Int = when (varSize){
        0 -> 1
        else -> domainSize * totalSubs(domainSize, varSize - 1)
    }

    override fun evaluate(model: BasicModel, substitution: Substitution): List<List<Observation>> {
        val domain = mutableListOf<Element>().apply { addAll(model.getDomain()) }

        for (i in 0..(totalSubs(domain.size, freeVars.size) - 1)) {

        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}