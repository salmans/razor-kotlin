package Transform

import Formula.*

private val INVALID_TERM_EXCEPTION = "Internal Error: Invalid Term"
private val INVALID_FORMULA_EXCEPTION = "Internal Error: Invalid Formula"

/**
 * Applies a substitution function on the term.
 */
fun Term.substitute(substitutions: (Var) -> Term): Term {
    return when (this) {
        is Var -> substitutions(this)
        is App -> App(this.function, this.terms.map { it.substitute(substitutions) })
        else -> throw RuntimeException(INVALID_TERM_EXCEPTION)
    }
}

/**
 * Applies a substitution function on a formula.
 */
fun Formula.substitute(substitutions: (Var) -> Term): Formula {
    return when (this) {
        is Top -> Top
        is Bottom -> Bottom
        is Atom -> Atom(this.relation, terms.map { it.substitute(substitutions) })
        is Equals -> Equals(this.left.substitute(substitutions), this.right.substitute(substitutions))
        is Not -> Not(this.formula.substitute(substitutions))
        is And -> And(this.left.substitute(substitutions), this.right.substitute(substitutions))
        is Or -> Or(this.left.substitute(substitutions), this.right.substitute(substitutions))
        is Implies -> Implies(this.left.substitute(substitutions), this.right.substitute(substitutions))
        is Exists -> Exists(this.variables, this.formula.substitute(substitutions))
        is Forall -> Forall(this.variables, this.formula.substitute(substitutions))
        else -> throw RuntimeException(INVALID_FORMULA_EXCEPTION)
    }
}

/**
 * Applies a variable renaming function on the term.
 */
fun Term.renameVar(renaming: (Var) -> Var): Term {
    return when (this) {
        is Var -> renaming(this)
        is App -> App(this.function, this.terms.map { it.substitute(renaming) })
        else -> throw RuntimeException(INVALID_TERM_EXCEPTION)
    }
}

/**
 * Applies a variable renaming function on a formula.
 */
fun Formula.renameVar(renaming: (Var) -> Var): Formula {
    return when (this) {
        is Top -> Top
        is Bottom -> Bottom
        is Atom -> Atom(this.relation, terms.map { it.renameVar(renaming) })
        is Equals -> Equals(this.left.renameVar(renaming), this.right.renameVar(renaming))
        is Not -> Not(this.formula.renameVar(renaming))
        is And -> And(this.left.renameVar(renaming), this.right.renameVar(renaming))
        is Or -> Or(this.left.renameVar(renaming), this.right.renameVar(renaming))
        is Implies -> Implies(this.left.renameVar(renaming), this.right.renameVar(renaming))
        is Exists -> Exists(this.variables.map(renaming), this.formula.renameVar(renaming))
        is Forall -> Forall(this.variables.map(renaming), this.formula.renameVar(renaming))
        else -> throw RuntimeException(INVALID_FORMULA_EXCEPTION)
    }
}

/**
 * Converts a formula to a prenex normal form.
 */
fun Formula.pnf(): Formula {
    /**
     * Renames the input variable until it's not in the list of input variables.
     */
    fun renameVar(variable: Var, noCollisionVariableList: Set<Var>): Var {
        var name = variable.name
        val names = noCollisionVariableList.map(Var::name)
        while (name in names) {
            name += "'"
        }
        return Var(name)
    }

    /**
     * Pulls a quantifier out of a side of binary formula. The function is primarily responsible to rename the quantified variables of
     * {@code quantified}.
     * @param quantified is the side of binary formula that is quantified.
     * @param other is the other side of binary formula.
     * @param quantifierFunction is the function that constructs the final quantified formula.
     */
    fun pullBinaryFormulaQuantifier(quantified: QuantifiedFormula, other: Formula,
                                    quantifierFunction: (QuantifiedFormula, Formula) -> QuantifiedFormula): QuantifiedFormula {
        val sharedVariables = quantified.variables.intersect(other.freeVars)
        val allVariables = (quantified.variables + freeVars).toSet()
        val renamed = quantified.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
        return quantifierFunction(renamed as QuantifiedFormula, other)
    }

    return when (this) {
        is Top -> Top
        is Bottom -> Bottom
        is Atom -> this
        is Equals -> this
    // e.g. ~(Qx. P(x)) -> Q' x. ~P(x)
        is Not -> {
            val f = formula.pnf()
            when (f) {
                is Forall -> Exists(f.variables, Not(f.formula).pnf())
                is Exists -> Forall(f.variables, Not(f.formula).pnf())
                else -> Not(f)
            }
        }
        is BinaryFormula -> {
            val l = left.pnf()
            val r = right.pnf()
            when (this) {
            // e.g. (Q x. F(x)) & G(y)) => Q x'. F(x') & G(y) or F(x) & (Q y. G(y)) => Q y'. F(x) & G(y')
                is And -> {
                    if (l is Forall) {
                        pullBinaryFormulaQuantifier(l, r, { q, f -> Forall(q.variables, And(q.formula, f).pnf()) })
                    } else if (l is Exists) {
                        pullBinaryFormulaQuantifier(l, r, { q, f -> Exists(q.variables, And(q.formula, f).pnf()) })
                    } else if (r is Forall) {
                        pullBinaryFormulaQuantifier(r, l, { q, f -> Forall(q.variables, And(f, q.formula).pnf()) })
                    } else if (r is Exists) {
                        pullBinaryFormulaQuantifier(r, l, { q, f -> Exists(q.variables, And(f, q.formula).pnf()) })
                    } else And(l, r)
                }
            // e.g. (Q x. F(x)) | G(y)) => Q x'. F(x') | G(y) or F(x) | (Q y. G(y)) => Q y'. F(x) | G(y')
                is Or -> {
                    if (l is Forall) {
                        pullBinaryFormulaQuantifier(l, r, { q, f -> Forall(q.variables, Or(q.formula, f).pnf()) })
                    } else if (l is Exists) {
                        pullBinaryFormulaQuantifier(l, r, { q, f -> Exists(q.variables, Or(q.formula, f).pnf()) })
                    } else if (r is Forall) {
                        pullBinaryFormulaQuantifier(r, l, { q, f -> Forall(q.variables, Or(f, q.formula).pnf()) })
                    } else if (r is Exists) {
                        pullBinaryFormulaQuantifier(r, l, { q, f -> Exists(q.variables, Or(f, q.formula).pnf()) })
                    } else Or(l, r)
                }
            // e.g. (Q x. F(x)) -> G(y)) => Q' x'. F(x') -> G(y) or F(x) -> (Q y. G(y)) => Q' y'. F(x) -> G(y')
                is Implies -> {
                    if (l is Forall) {
                        pullBinaryFormulaQuantifier(l, r, { q, f -> Exists(q.variables, Implies(q.formula, f).pnf()) })
                    } else if (l is Exists) {
                        pullBinaryFormulaQuantifier(l, r, { q, f -> Forall(q.variables, Implies(q.formula, f).pnf()) })
                    } else if (r is Forall) {
                        pullBinaryFormulaQuantifier(r, l, { q, f -> Forall(q.variables, Implies(f, q.formula).pnf()) })
                    } else if (r is Exists) {
                        pullBinaryFormulaQuantifier(r, l, { q, f -> Exists(q.variables, Implies(f, q.formula).pnf()) })
                    } else Implies(l, r)
                }
                else -> throw RuntimeException(INVALID_FORMULA_EXCEPTION)
            }
        }
        is Forall -> Forall(variables, formula.pnf())
        is Exists -> Exists(variables, formula.pnf())
        else -> throw RuntimeException(INVALID_FORMULA_EXCEPTION)
    }
}

/**
 * A generator for Skolem functions. A unique instance of this generator must be used when Skolemizing
 * formulas in the same theory.
 * @param prefix the prefix of the Skolem function names to generate
 */
class SkolemGenerator(private val prefix: String = "sk#") {
    /**
    An index used to generate Skolem functions during Skolemization.
     */
    private var skolemIndex = 0

    /**
     * Returns the next skolem function.
     */
    fun nextFunction() = "$prefix${skolemIndex++}"
}

fun Formula.skolem(generator: SkolemGenerator = SkolemGenerator()): Formula {
    val prenex = this.pnf()  // Skolemization for formulas that are not in PNF doesn't make sense!

    // {@code skolemHelper} helper to apply skolemization process recursively. The main purpose of
    // introducing this helper is to keep track of the universally quantified variables when generating
    // Skolem terms.
    fun skolemHelper(formula: Formula, skolemVariables: List<Var>): Formula {
        return when (formula) {
            is Forall -> {
                Forall(formula.variables, skolemHelper(formula.formula, skolemVariables + formula.variables))
            }
            is Exists -> {
                // Notice that we don't apply all substitutions at the end but during the recursive call to account for shadowing variable names:
                val substitutionMap = formula.variables.map { Pair(it, App(Func(generator.nextFunction()), skolemVariables)) }.toMap()
                return skolemHelper(formula.formula.substitute { substitutionMap[it] ?: it }, skolemVariables)
            }
            else -> formula // Nothing to do (assuming that the input formula is in PNF)
        }
    }

    return skolemHelper(prenex, emptyList())
}