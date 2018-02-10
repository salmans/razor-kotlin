package formula

interface Syntax

// Formulas ------------------------------------------------------------------------

/**
 * Terms
 */
sealed class Term : Syntax {
    /**
     * Returns a set of variables in this term.
     */
    abstract val freeVars: Set<Var>

    abstract override fun toString(): String
}

/**
 * A list of {@code Term}.
 */
typealias Terms = List<Term>

/**
 * Functions
 */
data class Func(private val name: String) : Syntax {
    override fun toString(): String = name
}

/**
 * Variable
 * e.g., x
 */
data class Var(val name: String) : Term() {
    override val freeVars by lazy { setOf(this) }

    override fun toString(): String = name
}

/**
 * A list of {@code Var}.
 */
typealias Vars = List<Var>

/**
 * Constant
 * e.g., 'c
 */
data class Const(val name: String) : Term() {
    override val freeVars: Set<Var> = emptySet()

    override fun toString(): String = "'$name"
}

/**
 * Function Application
 * e.g., f(x, y)
 *
 * Note: although constants are technically zero arity functions, we distinguish constants and functions syntactically.
 */
data class App(val function: Func, val terms: Terms = emptyList()) : Term() {
    override val freeVars by lazy { this.terms.flatMap(Term::freeVars).toSet() }

    override fun toString(): String = "$function(${terms.joinToString(", ")})"
}

/**
 * Formulas
 */
sealed class Formula : Syntax {
    /**
     * Returns a set of free variables in this formula.
     */
    abstract val freeVars: Set<Var>
}

/**
 * Prints parenthesis around non atomic formulas.
 */
fun Formula.printParens() = when (this) {
    is Atom -> toString()
    else -> "($this)"
}

/**
 * Predicate
 */
data class Pred(val name: String) : Syntax {
    override fun toString(): String = name
}

// Theory ------------------------------------------------------------------------
/**
 * Theory
 */
data class Theory(val formulas: List<Formula>) : Syntax {
    override fun toString() = formulas.joinToString("\n")
}

// Formulas ------------------------------------------------------------------------
/**
 * Top (Truth)
 */
object Top : Formula() {
    override val freeVars = emptySet<Var>()

    override fun toString() = "⊤"
}

/**
 * Bottom (Falsehood)
 */
object Bottom : Formula() {
    override val freeVars = emptySet<Var>()

    override fun toString() = "⟘"
}

/**
 * Atom
 * e.g. R(x, f(x))
 */
data class Atom(val pred: Pred, val terms: Terms = emptyList()) : Formula() {
    override val freeVars by lazy { terms.flatMap(Term::freeVars).toSet() }

    override fun toString(): String = "$pred(${this.terms.joinToString(", ")})"
}

/**
 * Equality
 * e.g. f(x) = y
 */
data class Equals(val left: Term, val right: Term) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun toString(): String = "$left = $right"
}

/**
 * Negation
 * e.g. ¬R(x)
 */
data class Not(val formula: Formula) : Formula() {
    override val freeVars by lazy { this.formula.freeVars }

    override fun toString(): String = "¬${formula.printParens()}"
}

/**
 * Conjunction
 * e.g. R(x) ∧ Q(y)
 */
data class And(val left: Formula, val right: Formula) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun toString(): String = "${left.printParens()} ∧ ${right.printParens()}"
}

/**
 * Disjunction
 * e.g. R(x) ∨ Q(y)
 */
data class Or(val left: Formula, val right: Formula) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun toString(): String = "${left.printParens()} ∨ ${right.printParens()}"
}

/**
 * Implication
 * e.g. P(x) → Q(x)
 */
data class Implies(val left: Formula, val right: Formula) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun toString(): String = "${left.printParens()} → ${right.printParens()}"
}

/**
 * Bi-implication
 * e.g. P(x) ⇔ Q(x)
 */
data class Iff(val left: Formula, val right: Formula) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun toString(): String = "${left.printParens()} ⇔ ${right.printParens()}"
}

/**
 * Exists
 * e.g. ∃ x.P(x)
 */
data class Exists(val variables: Vars, val formula: Formula) : Formula() {
    override val freeVars by lazy { this.formula.freeVars - variables }

    override fun toString(): String = "∃ ${variables.joinToString(", ")}. ${formula.printParens()}"
}

/**
 * Forall
 * e.g. ∀ x.P(x)
 */
data class Forall(val variables: Vars, val formula: Formula) : Formula() {
    override val freeVars by lazy { this.formula.freeVars - variables }

    override fun toString(): String = "∀ ${variables.joinToString(", ")}. ${formula.printParens()}"
}

