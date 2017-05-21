package formula

interface Syntax {
    /**
     * Print the element in a human readable form.
     */
    fun print(): String
}

// print a list of syntactic elements
private fun <T : Syntax> List<T>.print(separator: String = ", "): String = joinToString(separator = separator, transform = { it.print() })

// Formulas ------------------------------------------------------------------------

/**
 * Terms
 */
abstract class Term : Syntax {
    /**
     * Returns a set of variables in this term.
     */
    abstract val freeVars: Set<Var>
}

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
data class App(val function: Func, val terms: List<Term> = emptyList()) : Term() {
    override val freeVars by lazy { this.terms.flatMap(Term::freeVars).toSet() }

    override fun print(): String = "${function.print()}(${terms.print()})"
}

/**
 * Formulas
 */
abstract class Formula : Syntax {
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
 * Relations
 */
data class Rel(val name: String) : Syntax {
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
data class Atom(val relation: Rel, val terms: List<Term> = emptyList()) : Formula() {
    override val freeVars by lazy { terms.flatMap(Term::freeVars).toSet() }

    override fun print(): String = "${relation.print()}(${this.terms.print()})"
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
 * Superclass for binary formulas: {@code And}, {@code Or}, {@code Implies}
 */
abstract class BinaryFormula(open val left: Formula, open val right: Formula) : Formula()

/**
 * Conjunction
 * e.g. R(x) ∧ Q(y)
 */
data class And(override val left: Formula, override val right: Formula) : BinaryFormula(left, right) {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun print(): String = "${left.printParens()} ∧ ${right.printParens()}"
}

/**
 * Disjunction
 * e.g. R(x) ∨ Q(y)
 */
data class Or(override val left: Formula, override val right: Formula) : BinaryFormula(left, right) {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun print(): String = "${left.printParens()} ∨ ${right.printParens()}"
}

/**
 * Implication
 * e.g. P(x) → Q(x)
 */
data class Implies(override val left: Formula, override val right: Formula) : BinaryFormula(left, right) {
    override val freeVars by lazy { this.left.freeVars + this.right.freeVars }

    override fun print(): String = "${left.printParens()} → ${right.printParens()}"
}

/**
 * Superclass for quantified formulas: {@code Forall} and {@code Exists}
 */
abstract class QuantifiedFormula(open val variables: List<Var>, open val formula: Formula) : Formula()

/**
 * Exists
 * e.g. ∃ x.P(x)
 */
data class Exists(override val variables: List<Var>, override val formula: Formula) : QuantifiedFormula(variables, formula) {
    override val freeVars by lazy { this.formula.freeVars - variables }

    override fun print(): String = "∃ ${variables.print()}. ${formula.printParens()}"
}

/**
 * Forall
 * e.g. ∀ x.P(x)
 */
data class Forall(override val variables: List<Var>, override val formula: Formula) : QuantifiedFormula(variables, formula) {
    override val freeVars by lazy { this.formula.freeVars - variables }

    override fun print(): String = "∀ ${variables.print()}. ${formula.printParens()}"
}

