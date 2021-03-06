package chase

import formula.*
import tools.Either
import helper.pow
import java.util.*
import kotlin.collections.HashMap


const val EXPECTED_STANDARD_SEQUENT = "Internal Error: Expecting a geometric sequent in standard form."
const val ELEMENT_NOT_IN_DOMAIN = "The element is not in the domain of this model."

class BasicModel() : Model() {
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

    constructor(model: BasicModel) : this() {
        this.elementIndex = model.elementIndex
        this.rewrites.putAll(model.rewrites)
        this.facts.addAll(model.facts)
    }

    override fun observe(observation: Observation) {
        when (observation) {
            is Observation.Fact -> this.facts.add(observation.copy(terms = observation.terms.map { record(it) }))
            is Observation.Identity -> {
                val (l, r) = observation.let { record(it.left) to record(it.right) }
                val (source, dest) = if (l > r) (r to l.duplicate()) else (l to r.duplicate())
                this.rewrites.replaceAll{ _, v -> if (v == dest) v.apply { collapse(source) } else v }
            }
        }
    }

    override fun lookup(observation: Observation): Boolean = when (observation) {
        is Observation.Fact -> observation.terms.map { element(it) }.let {
            if (it.any { it == null }) false else facts.contains(observation.copy(terms = it.filterNotNull()))
        }
        is Observation.Identity -> element(observation.left).let { it != null && it == element(observation.right) }
    }

    override fun element(term: WitnessTerm): Element? = when (term) {
        is Element -> if (term in getDomain()) term else null
        is WitnessConst -> rewrites[term]
        is WitnessApp -> term.terms.map { element(it) }.let {
            if (it.any { it == null }) null else rewrites[term.copy(terms = it.filterNotNull())]
        }
    }

    override fun witness(element: Element): Set<WitnessTerm> = rewrites.keys.filter { rewrites[it] == element }.toSet()

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

class BasicSequent(formula: Formula) : Sequent() {
    val freeVars = formula.freeVars.toList()
    override val body = (formula as? Implies)?.left ?: throw RuntimeException(EXPECTED_STANDARD_SEQUENT)
    override val head = (formula as? Implies)?.right ?: throw RuntimeException(EXPECTED_STANDARD_SEQUENT)
    val bodyLiterals: List<Literal> = buildBody(body)
    val headLiterals: List<List<Literal>> = buildHead(head)

    override fun toString(): String = "$bodyLiterals -> $headLiterals"
}

class BasicEvaluator : Evaluator<BasicSequent> {
    private fun Term.witness(witness: (Var) -> Element): WitnessTerm = when (this) {
        is Const -> WitnessConst(this.name)
        is Var -> witness(this)
        is App -> WitnessApp(this.function, this.terms.map { it.witness(witness) })
    }

    override fun evaluate(model: Model, selector: Selector<BasicSequent>, bounder: Bounder?): List<Either<Model, Model>>? {
        val domain = model.getDomain().toList()
        val iterator = selector.iterator()

        while (iterator.hasNext()) {
            val sequent = iterator.next()
            val domainSize = domain.size
            val sequentsSize = sequent.freeVars.size
            for (i in 0 until pow(domainSize, sequentsSize)) {
                val witMap = HashMap<Var, Element>()
                var j = 0
                var total = i
                while (j < sequentsSize) {
                    witMap[sequent.freeVars[j++]] = domain[total % domainSize]
                    total /= domainSize
                }
                val witness = { v: Var -> witMap[v]!! }

                val convert = { lit: Literal ->
                    when (lit) {
                        is Literal.Atm -> Observation.Fact(Rel(lit.pred.name), lit.terms.map { it.witness(witness) })
                        is Literal.Eql -> Observation.Identity(lit.left.witness(witness), lit.right.witness(witness))
                    }
                }

                val bodies: List<Observation> = sequent.bodyLiterals.map(convert)
                val heads: List<List<Observation>> = sequent.headLiterals.map { it.map(convert) }

                if (bodies.all { model.lookup(it) } && !heads.any { it.all { model.lookup(it) } }) {
                    return if (heads.isEmpty()) {
                        null // failure
                    } else {
                        heads.map {
                            if (bounder != null && it.any { bounder.bound(model, it) })
                                Either.right(model.duplicate().apply { it.forEach { observe(it) } })
                            else
                                Either.left(model.duplicate().apply { it.forEach { observe(it) } })
                        }
                        // this evaluator returns the models from first successful sequent
                    }
                }
            }
        }

        return emptyList() // not failed but no progress
    }
}