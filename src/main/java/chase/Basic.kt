package chase

import formula.*
import sun.awt.util.IdentityLinkedList
import tools.pow


class BasicModel() : Model<BasicModel>() {
    private val rewrites: MutableMap<WitnessTerm, Element> = mutableMapOf()

    override val elements: Collection<Element>
        get() = rewrites.values
    override var facts: HashSet<Observation.Fact> = HashSet()

    private fun record(term: WitnessTerm): Element = reduce(term) ?: Element(elements.size + 1).apply { rewrites[term] = this }


    private fun reduce(term: WitnessTerm): Element? = when (term) {
        is Element -> term
        is WitnessConst -> rewrites[term]
        is WitnessApp -> term.terms.map { reduce(it) }.let {
            if (it.any { it == null }) null else rewrites[term.copy(terms = it.filterNotNull())]
        }
    }

    constructor(model: BasicModel) : this() {
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
}


sealed class Literal {
    abstract fun print(): String

    data class Atm(val pred: Pred, val terms: Terms) : Literal() {
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
    val freeVars = formula.freeVars.toList()
    val body: List<Literal> = if (formula is Implies) buildBody(formula.left) else throw EXPECTED_STANDARD_SEQUENT.internalError()
    val head: List<List<Literal>> = if (formula is Implies) buildHead(formula.right) else throw EXPECTED_STANDARD_SEQUENT.internalError()
}

typealias Witness = (Var) -> Element

fun Term.witness(witness: Witness): WitnessTerm = when (this) {
    is Const -> WitnessConst(this.name)
    is Var -> witness(this)
    is App -> WitnessApp(this.function, this.terms.map { it.witness(witness) })
}

class BasicEvaluator(private val sequents: List<BasicSequent>) : Evaluator<BasicModel, BasicSequent> {
    override fun evaluate(model: BasicModel): List<BasicModel>? {
        val domain = model.elements.toList()

        for (sequent in sequents) {
            for (i in 0..(pow(domain.size, sequent.freeVars.size) - 1)) {
                val witMap = (0 until sequent.freeVars.size).associate {
                    sequent.freeVars[it] to domain[i % pow(domain.size, it)]
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

class BasicStrategy : Strategy<BasicModel>() {
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

class BasicChase : Chase<BasicModel, BasicSequent, BasicEvaluator, BasicStrategy>() {
    override fun emptyModel(): BasicModel = BasicModel()

    override fun newSequent(formula: Formula): BasicSequent = BasicSequent(formula)

    override fun newEvaluator(sequents: List<BasicSequent>): BasicEvaluator = BasicEvaluator(sequents)

    override fun newStrategy(): BasicStrategy = BasicStrategy()
}

fun main(args: Array<String>) {
    // solve("P('a)\nP(x) implies Q(x)".parseTheory()!!.geometric(), BasicChase())
    // solve("P('a)\nP(x) implies Q(x)\nQ(x) implies R(x)".parseTheory()!!.geometric(), BasicChase())
    // solve("P('a)".parseTheory()!!.geometric(), BasicChase())
    // solve("exists x. P(x)".parseTheory()!!.geometric(), BasicChase())
    // solve("P(f('a))".parseTheory()!!.geometric(), BasicChase())
    // solve("P('a) or Q('b) or R('c)".parseTheory()!!.geometric(), BasicChase())
    // solve("TRUE implies P('a, 'b)".parseTheory()!!.geometric(), BasicChase())
    // solve("'a = 'b".parseTheory()!!.geometric(), BasicChase())
    solve("P('a)\nQ('b)\nP(x) and Q(y) implies x = y".parseTheory()!!.geometric(), BasicChase())
}