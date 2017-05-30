package formula

interface Syntax {
    /**
     * Print the element in a human readable form.
     */
    fun print(): String
}

// print a list of syntactic elements
fun <T : Syntax> List<T>.print(separator: String = ", "): String = joinToString(separator = separator, transform = { it.print() })

// Formulas ------------------------------------------------------------------------

/**
 * Terms
 */
sealed class Term : Syntax {
    /**
     * Returns a set of variables in this term.
     */
    abstract val freeVars: Set<Var>
}

/**
 * A list of {@code Term}.
 */
typealias Terms = List<Term>

/**
 * Functions
 */
data class Func(val name: String) : Syntax {
    override fun print(): String = name
}

/**
 * Variable
 * e.g., x
 */
data class Var(val name: String) : Term() {
    override val freeVars by lazy { setOf(this) }

    override fun print(): String = name
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

    override fun print(): String = "'$name"
}

/**
 * Function Application
 * e.g., f(x, y)
 *
 * Note: although constants are technically zero arity functions, we distinguish constants and functions syntactically.
 */
data class App(val function: Func, val terms: Terms = emptyList()) : Term() {
    override val freeVars by lazy { this.terms.flatMap(Term::freeVars).toSet() }

    override fun print(): String = "${function.print()}(${terms.print()})"
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
    is Atom -> print()
    else -> "(${print()})"
}

/**
 * Predicate
 */
data class Pred(val name: String) : Syntax {
    override fun print(): String = name
}

// Theory ------------------------------------------------------------------------
/**
 * Theory
 */
data class Theory(val formulas: List<Formula>) : Syntax {
    override fun print() = formulas.print("\n")
}

// Formulas ------------------------------------------------------------------------
/**
 * Top (Truth)
 */
object Top : Formula() {
    override val freeVars = emptySet<Var>()

    override fun print() = "⊤"
}

/**
 * Bottom (Falsehood)
 */
object Bottom : Formula() {
    override val freeVars = emptySet<Var>()

    override fun print() = "⟘"
}

/**
 * Atom
 * e.g. R(x, f(x))
 */
data class Atom(val pred: Pred, val terms: Terms = emptyList()) : Formula() {
    override val freeVars by lazy { terms.flatMap(Term::freeVars).toSet() }

    override fun print(): String = "${pred.print()}(${this.terms.print()})"
}

/**
 * Equality
 * e.g. f(x) = y
 */
data class Equals(val left: Term, val right: Term) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun print(): String = "${left.print()} = ${right.print()}"
}

/**
 * Negation
 * e.g. ¬R(x)
 */
data class Not(val formula: Formula) : Formula() {
    override val freeVars by lazy { this.formula.freeVars }

    override fun print(): String = "¬${formula.printParens()}"
}

/**
 * Conjunction
 * e.g. R(x) ∧ Q(y)
 */
data class And(val left: Formula, val right: Formula) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun print(): String = "${left.printParens()} ∧ ${right.printParens()}"
}

/**
 * Disjunction
 * e.g. R(x) ∨ Q(y)
 */
data class Or(val left: Formula, val right: Formula) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun print(): String = "${left.printParens()} ∨ ${right.printParens()}"
}

/**
 * Implication
 * e.g. P(x) → Q(x)
 */
data class Implies(val left: Formula, val right: Formula) : Formula() {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun print(): String = "${left.printParens()} → ${right.printParens()}"
}

/**
 * Exists
 * e.g. ∃ x.P(x)
 */
data class Exists(val variables: Vars, val formula: Formula) : Formula() {
    override val freeVars by lazy { this.formula.freeVars - variables }

    override fun print(): String = "∃ ${variables.print()}. ${formula.printParens()}"
}

/**
 * Forall
 * e.g. ∀ x.P(x)
 */
data class Forall(val variables: Vars, val formula: Formula) : Formula() {
    override val freeVars by lazy { this.formula.freeVars - variables }

    override fun print(): String = "∀ ${variables.print()}. ${formula.printParens()}"
}

