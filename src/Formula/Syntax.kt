package Formula

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
abstract class Term : Syntax

/**
 * Functions
 */
data class Func(val name: String) : Syntax {
    override fun print(): String = name
}

/**
 * Variables
 * e.g., x
 */
data class Var(val name: String) : Term() {
    override fun print(): String = name
}

/**
 * Function Application
 * e.g., f(x, y)
 *
 * Note: constants are functions of arity zero
 */
data class App(val function: Func, val terms: List<Term> = emptyList()) : Term() {
    override fun print(): String = "${function.print()}(${terms.print()})"
}

/**
 * Formulas
 */
abstract class Formula : Syntax

/**
 * Prints parenthesis around non atomic formulas.
 */
fun Formula.printParens() = when(this) {
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
data class Theory(val formulas: List<Formula>): Syntax {
    override fun print() = formulas.print("\n")
}

// Formulas ------------------------------------------------------------------------
/**
 * Top (Truth)
 */
object Top : Formula() {
    override fun print() = "⊤"
}

/**
 * Bottom (Falsehood)
 */
object Bottom : Formula() {
    override fun print() = "⟘"
}

/**
 * Atom
 * e.g. R(x, f(x))
 */
data class Atom(val relation: Rel, val terms: List<Term> = emptyList()) : Formula() {
    override fun print(): String = "${relation.print()}(${terms.print()})"
}

/**
 * Equality
 * e.g. f(x) = y
 */
data class Equals(val left: Term, val right: Term) : Formula() {
    override fun print(): String = "${left.print()} = ${right.print()}"
}

/**
 * Negation
 * e.g. ¬R(x)
 */
data class Not(val formula: Formula) : Formula() {
    override fun print(): String = "¬${formula.printParens()}"
}

/**
 * Conjunction
 * e.g. R(x) ∧ Q(y)
 */
data class And(val left: Formula, val right: Formula) : Formula() {
    override fun print(): String = "${left.printParens()} ∧ ${right.printParens()}"
}

/**
 * Disjunction
 * e.g. R(x) ∨ Q(y)
 */
data class Or(val left: Formula, val right: Formula) : Formula() {
    override fun print(): String = "${left.printParens()} ∨ ${right.printParens()}"
}

/**
 * Implication
 * e.g. P(x) → Q(x)
 */
data class Implies(val left: Formula, val right: Formula) : Formula() {
    override fun print(): String = "${left.printParens()} → ${right.printParens()}"
}

/**
 * Exists
 * e.g. ∃ x.P(x)
 */
data class Exists(val variables: List<Var>, val formula: Formula) : Formula() {
    override fun print(): String = "∃ ${variables.print()}. ${formula.printParens()}"
}

/**
 * Forall
 * e.g. ∀ x.P(x)
 */
data class Forall(val variables: List<Var>, val formula: Formula) : Formula() {
    override fun print(): String = "∀ ${variables.print()}. ${formula.printParens()}"
}

