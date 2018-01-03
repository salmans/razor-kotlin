package chase

import formula.*
import sun.awt.util.IdentityLinkedList
import tools.pow
import java.util.*


val EXPECTED_STANDARD_SEQUENT = "Internal Error: Expecting a geometric sequent in standard form."
val ELEMENT_NOT_IN_DOMAIN = "The element is not in the domain of this model."

class BasicModel() : Model<BasicModel>() {
    private var elementIndex = 0

    private val rewrites: MutableMap<WitnessTerm, Element> = mutableMapOf()

    // TODO make this a HashSet
    private var facts: LinkedList<Observation.Fact> = LinkedList()

    override fun getDomain(): Set<Element> = rewrites.values.toSet()

    override fun getFacts(): Set<Observation.Fact> = facts.toSet()

    private fun record(term: WitnessTerm): Element = when (term) {
        is Element -> if (term in getDomain()) term else throw throw RuntimeException(ELEMENT_NOT_IN_DOMAIN)
        is WitnessConst -> rewrites[term] ?: { Element(elementIndex++).apply { rewrites[term] = this } }.invoke()
        is WitnessApp -> term.copy(terms = term.terms.map { record(it) }).let {
            rewrites[it] ?: { Element(elementIndex++).apply { rewrites[it] = this } }.invoke()
        }
    }

    private fun reduce(term: WitnessTerm): Element? = when (term) {
        is Element -> if (term in getDomain()) term else null
        is WitnessConst -> rewrites[term]
        is WitnessApp -> term.terms.map { reduce(it) }.let {
            if (it.any { it == null }) null else rewrites[term.copy(terms = it.filterNotNull())]
        }
    }

    constructor(model: BasicModel) : this() {
        this.elementIndex = model.elementIndex
        this.rewrites.putAll(model.rewrites)
        this.facts.addAll(model.facts)
    }

    override fun observe(observation: Observation) {
        when (observation) {
            is Observation.Fact -> this.facts.add(observation.copy(terms = observation.terms.map { record(it) }))
            is Observation.Identity -> {
                val l = record(observation.left)
                val r = record(observation.right)
                this.rewrites.replaceAll({ _, v -> if (v == r) r.apply { collapse(l) } else v })
            }
        }
    }

    override fun lookup(observation: Observation): Boolean = when (observation) {
        is Observation.Fact -> observation.terms.map { reduce(it) }.let {
            if (it.any { it == null }) false else facts.contains(observation.copy(terms = it.filterNotNull()))
        }
        is Observation.Identity -> reduce(observation.left).let { it != null && it == reduce(observation.right) }
    }

    override fun duplicate(): BasicModel = BasicModel(this)

    override fun equals(other: Any?): Boolean = when (other) {
        is BasicModel -> this.elementIndex == other.elementIndex && this.rewrites == other.rewrites && this.facts == other.facts
        else -> false
    }

    override fun hashCode(): Int {
        var result = elementIndex
        result = 31 * result + rewrites.hashCode()
        result = 31 * result + facts.hashCode()
        return result
    }
}


sealed class Literal {
    data class Atm(val pred: Pred, val terms: Terms) : Literal() {
        override fun toString(): String = "$pred(${this.terms.joinToString(", ")})"
    }

    data class Eql(val left: Term, val right: Term) : Literal() {
        override fun toString(): String = "$left = $right"
    }
}

fun Atom.lit() = Literal.Atm(this.pred, this.terms)
fun Equals.lit() = Literal.Eql(this.left, this.right)

private fun buildBody(formula: Formula): List<Literal> = when (formula) {
    is Top -> emptyList()
    is Atom -> listOf(formula.lit())
    is Equals -> listOf(formula.lit())
    is And -> buildBody(formula.left) + buildBody(formula.right)
    else -> throw RuntimeException(EXPECTED_STANDARD_SEQUENT)
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
            throw RuntimeException(EXPECTED_STANDARD_SEQUENT)
        }
    }
    is Or -> buildHead(formula.left) + buildHead(formula.right)
    else -> throw RuntimeException(EXPECTED_STANDARD_SEQUENT)
}

class BasicSequent(formula: Formula) : Sequent {
    val freeVars = formula.freeVars.toList()
    val body: List<Literal> = if (formula is Implies) buildBody(formula.left) else throw RuntimeException(EXPECTED_STANDARD_SEQUENT)
    val head: List<List<Literal>> = if (formula is Implies) buildHead(formula.right) else throw RuntimeException(EXPECTED_STANDARD_SEQUENT)
}

typealias Witness = (Var) -> Element

fun Term.witness(witness: Witness): WitnessTerm = when (this) {
    is Const -> WitnessConst(this.name)
    is Var -> witness(this)
    is App -> WitnessApp(this.function, this.terms.map { it.witness(witness) })
}

class BasicEvaluator(private val sequents: List<BasicSequent>) : Evaluator<BasicModel, BasicSequent> {
    override fun evaluate(model: BasicModel): List<BasicModel>? {
        val domain = model.getDomain().toList()

        for (sequent in sequents) {
            val domainSize = domain.size
            val sequentsSize = sequent.freeVars.size
            for (i in 0 until pow(domainSize, sequentsSize)) {
                val witMap = (1 .. sequentsSize).associate {
                    sequent.freeVars[it - 1] to domain[(i / pow(domainSize, (it - 1))) % pow(domainSize, it)]
                }
                val witness = { v: Var -> witMap[v]!! }

                val convert = { lit: Literal ->
                    when (lit) {
                        is Literal.Atm -> Observation.Fact(Rel(lit.pred.name), lit.terms.map { it.witness(witness) })
                        is Literal.Eql -> Observation.Identity(lit.left.witness(witness), lit.right.witness(witness))
                    }
                }

                val bodies: List<Observation> = sequent.body.map(convert)
                val heads: List<List<Observation>> = sequent.head.map { it.map(convert) }

                if (bodies.all { model.lookup(it) } && !heads.any { it.all { model.lookup(it) } }) {
                    return if (heads.isEmpty()) {
                        null // failure
                    } else {
                        heads.map { model.duplicate().apply { it.forEach { observe(it) } } }
                        // this evaluator returns the models from first successful sequent
                    }
                }
            }
        }

        return emptyList() // not failed but no progress
    }
}

class BasicStrategy : Strategy<BasicModel> {
    private val queue = IdentityLinkedList<BasicModel>()

    override fun iterator(): Iterator<BasicModel> {
        return queue.iterator()
    }

    override fun add(model: BasicModel): Boolean {
        return queue.add(model)
    }

    override fun remove(model: BasicModel): Boolean {
        return queue.remove(model)
    }
}