package Transform

import Formula.*

/**
 * Applies a substitution function on the term.
 */
fun Term.substitute(substitutions: (Var) -> Term): Term {
    return when (this) {
        is Var -> substitutions(this)
        is App -> this.copy(terms = this.terms.map { it.substitute(substitutions) })
        else -> throw INVALID_TERM.internalError()
    }
}

/**
 * Applies a substitution function on a formula.
 */
fun Formula.substitute(substitutions: (Var) -> Term): Formula {
    return when (this) {
        is Top, is Bottom -> this
        is Atom -> this.copy(terms = this.terms.map { it.substitute(substitutions) })
        is Equals -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
        is Not -> this.copy(formula = this.formula.substitute(substitutions))
        is And -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
        is Or -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
        is Implies -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
        is Exists -> this.copy(variables = this.variables, formula = this.formula.substitute(substitutions))
        is Forall -> this.copy(variables = this.variables, formula = this.formula.substitute(substitutions))
        else -> throw INVALID_FORMULA.internalError()
    }
}

/**
 * Applies a variable renaming function on the term.
 */
fun Term.renameVar(renaming: (Var) -> Var): Term {
    return when (this) {
        is Var -> renaming(this)
        is App -> this.copy(terms = this.terms.map { it.substitute(renaming) })
        else -> throw INVALID_TERM.internalError()
    }
}

/**
 * Applies a variable renaming function on a formula.
 */
fun Formula.renameVar(renaming: (Var) -> Var): Formula {
    return when (this) {
        is Top, is Bottom -> this
        is Atom -> this.copy(terms = terms.map { it.renameVar(renaming) })
        is Equals -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
        is Not -> this.copy(formula = this.formula.renameVar(renaming))
        is And -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
        is Or -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
        is Implies -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
        is Exists -> this.copy(variables = this.variables.map(renaming), formula = this.formula.renameVar(renaming))
        is Forall -> this.copy(variables = this.variables.map(renaming), formula = this.formula.renameVar(renaming))
        else -> throw INVALID_FORMULA.internalError()
    }
}

/**
 * Converts the formula to a prenex normal form.
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
        is Top, is Bottom, is Atom, is Equals -> this
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
                else -> throw INVALID_FORMULA.internalError()
            }
        }
        is Forall -> this.copy(formula = formula.pnf())
        is Exists -> this.copy(formula = formula.pnf())
        else -> throw INVALID_FORMULA.internalError()
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

/**
 * Puts the formula into Skolem normal form.
 * @param generator is an instance of {@code SkolemGenerator} that is used for generating Skolem function names.
 */
fun Formula.snf(generator: SkolemGenerator = SkolemGenerator()): Formula {
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

/**
 * Converts the formula to a negation normal form. This includes transforming implications to disjunctions.
 */
fun Formula.nnf(): Formula {
    return when (this) {
        is Top, is Bottom, is Atom, is Equals -> this
        is Not -> when (this.formula) {
            is Top -> Bottom
            is Bottom -> Top
            is Atom, is Equals -> this
            is Not -> this.formula.formula
            is And -> Or(Not(this.formula.left).nnf(), Not(this.formula.right).nnf())
            is Or -> And(Not(this.formula.left).nnf(), Not(this.formula.right).nnf())
            is Implies -> And(this.formula.left.nnf(), Not(this.formula.right).nnf())
            is Exists -> Forall(this.formula.variables, Not(this.formula.formula).nnf())
            is Forall -> Exists(this.formula.variables, Not(this.formula.formula).nnf())
            else -> throw INVALID_FORMULA.internalError()
        }
        is And -> this.copy(left = this.left.nnf(), right = this.right.nnf())
        is Or -> this.copy(left = this.left.nnf(), right = this.right.nnf())
        is Implies -> Or(Not(this.left).nnf(), this.right.nnf())
        is Exists -> this.copy(variables = this.variables, formula = this.formula.nnf())
        is Forall -> this.copy(variables = this.variables, formula = this.formula.nnf())
        else -> throw INVALID_FORMULA.internalError()
    }
}

/**
 * Converts the formula to a conjunctive normal form where variables are assumed to be universally quantified.
 * @param generator is an instance of {@code SkolemGenerator} that is used to generate Skolem functions during the transformation.
 */
fun Formula.cnf(generator: SkolemGenerator = SkolemGenerator()): Formula {
    // Skolemization (after PNF) -> Negation Normal Form -> Distribute Conjunction -> Drop Universal Quantifiers
    val nnf = this.snf(generator).nnf()

    // Distribute Conjunction
    fun pushOr(formula: Formula): Formula {
        return when (formula) {
            is Top, is Bottom, is Atom, is Equals, is Not -> formula // because already NNF
            is And -> formula.copy(left = pushOr(formula.left), right = pushOr(formula.right))
            is Or -> {
                val left = pushOr(formula.left)
                val right = pushOr(formula.right)
                if (left is And) {
                    val l = pushOr(Or(left.left, right))
                    val r = pushOr(Or(left.right, right))
                    And(l, r)
                } else if (right is And) {
                    val l = pushOr(Or(left, right.left))
                    val r = pushOr(Or(left, right.right))
                    And(l, r)
                } else {
                    Or(left, right)
                }
            }
            is Forall -> formula.copy(formula = pushOr(formula.formula))
            is Implies -> throw EXPECTED_NNF_FORMULA.internalError() // because already NNF
            is Exists -> throw EXPECTED_NNF_FORMULA.internalError()  // because already SNF
            else -> throw INVALID_FORMULA.internalError()
        }
    }

    /**
     * Removes all universal quantifiers (assuming already PNF, SNF and conjunction distributed).
     */
    fun removeForall(formula: Formula): Formula {
        return when (formula) {
            is Forall -> removeForall(formula.formula)
            else -> formula // because already PNF
        }
    }
    
    return removeForall(pushOr(nnf))
}