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

    object Tru : Literal() {
        override fun print(): String = "⊤"
    }

    object Fls : Literal() {
        override fun print(): String = "⟘"
    }

    data class Atm(val pred: Pred, val terms: Terms) : Literal() {
        override fun print(): String = "$pred(${this.terms.joinToString(", ")})"
    }

    data class Eql(val left: Term, val right: Term) : Literal() {
        override fun print(): String = "$left = $right"
    }

    data class Neg(val pred: Pred, val terms: Terms) : Literal() {
        override fun print(): String = "¬$pred(${this.terms.joinToString(", ")})"
    }

    data class Neq(val left: Term, val right: Term) : Literal() {
        override fun print(): String = "$left ≠ $right"
    }
}

fun Top.lit() = Literal.Tru
fun Top.neg() = Literal.Fls
fun Bottom.lit() = Literal.Fls
fun Bottom.neg() = Literal.Tru
fun Atom.lit() = Literal.Atm(this.pred, this.terms)
fun Atom.neg() = Literal.Neg(this.pred, this.terms)
fun Equals.lit() = Literal.Eql(this.left, this.right)
fun Equals.neg() = Literal.Neq(this.left, this.right)


class BasicSequent(formula: Formula) : Sequent<BasicModel> {
    val body: Set<Literal>
    val head: Set<Set<Literal>>

    init {
        fun buildBody(formula: Formula): Set<Literal> = when (formula) {
            is Top -> emptySet()
            is Bottom -> throw INVALID_SEQUENT_FALSE_BODY.internalError()
            is Atom -> setOf(formula.lit())
            is Equals -> setOf(formula.lit())
            is Not -> when (formula.formula) {
                is Top -> throw INVALID_SEQUENT_FALSE_BODY.internalError()
                is Bottom -> emptySet()
                is Atom -> setOf(formula.formula.neg())
                is Equals -> setOf(formula.formula.neg())
                else -> throw EXPECTED_STANDARD_SEQUENT.internalError()
            }
            is And -> buildBody(formula.left) + buildBody(formula.right)
            else -> throw EXPECTED_STANDARD_SEQUENT.internalError()
        }

        fun buildHead(formula: Formula): Set<Set<Literal>> = when (formula) {
            is Top -> emptySet()
            is Bottom -> setOf(emptySet())
            is Atom -> setOf(setOf(formula.lit()))
            is Equals -> setOf(setOf(formula.lit()))
            is Not -> when (formula.formula) {
                is Top -> setOf(emptySet())
                is Bottom -> emptySet()
                is Atom -> setOf(setOf(formula.formula.neg()))
                is Equals -> setOf(setOf(formula.formula.neg()))
                else -> throw EXPECTED_STANDARD_SEQUENT.internalError()
            }
            is And -> {
                val left = buildHead(formula.left)
                val right = buildHead(formula.right)
                if (left.isEmpty()) {
                    right
                } else if (right.isEmpty()) {
                    left
                } else if (left.size == 1 || right.size == 1) {
                    setOf((left.firstOrNull() ?: emptySet()) + (right.firstOrNull() ?: emptySet()))
                } else {
                    throw EXPECTED_STANDARD_SEQUENT.internalError()
                }
            }
            is Or -> buildHead(formula.left) + buildHead(formula.right)
            else -> throw EXPECTED_STANDARD_SEQUENT.internalError()
        }

        body = if (formula is Implies) buildBody(formula.left) else emptySet()
        head = if (formula is Implies) buildHead(formula.right) else buildHead(formula)
    }

    override fun evaluate(model: BasicModel): Set<Observation> {
        return emptySet()
    }
}