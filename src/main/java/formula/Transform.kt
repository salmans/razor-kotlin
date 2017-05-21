package formula

/**
 * Applies a substitution function on the term.
 */
fun Term.substitute(substitutions: (Var) -> Term): Term {
    return when (this) {
        is Const -> this
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
        is Const -> this
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

/**
 * Applies basic syntactic simplification on the formula.
 */
fun Formula.simplify(): Formula {
    return when (this) {
        is Top, is Bottom, is Atom, is Equals -> this
        is Not -> {
            val formula = formula.simplify()
            when (formula) {
                is Top -> Bottom
                is Bottom -> Top
                is Not -> formula.formula.simplify()
                else -> this.copy(formula = formula)
            }
        }
        is And -> {
            val left = left.simplify()
            val right = right.simplify()
            if (left is Bottom || right is Bottom) {
                Bottom
            } else if (right is Top) {
                left
            } else if (left is Top) {
                right
            } else {
                this.copy(left = left, right = right)
            }
        }
        is Or -> {
            val left = left.simplify()
            val right = right.simplify()
            if (left is Top || right is Top) {
                Top
            } else if (right is Bottom) {
                left
            } else if (left is Bottom) {
                right
            } else {
                this.copy(left = left, right = right)
            }
        }
        is Implies -> {
            val left = left.simplify()
            val right = right.simplify()
            if (left is Bottom || right is Top) {
                Top
            } else if (left is Top) {
                right
            } else if (right is Bottom) {
                Not(left).simplify()
            } else {
                this.copy(left = left, right = right)
            }
        }
        is Exists -> {
            val formula = formula.simplify()
            val vs = variables.intersect(formula.freeVars)
            if (!vs.isEmpty()) this.copy(variables = vs.toList(), formula = formula) else formula
        }
        is Forall -> {
            val formula = formula.simplify()
            val vs = variables.intersect(formula.freeVars)
            if (!vs.isEmpty()) this.copy(variables = vs.toList(), formula = formula) else formula
        }
        else -> throw INVALID_FORMULA.internalError()
    }
}

/**
 * Transforms the formula to a set of formulas in geometric form.
 */
fun Formula.geometric(generator: SkolemGenerator = SkolemGenerator()): Set<Formula> {
    // For any disjunct of the formula in CNF, the negative literals form the body of the geometric form
    // and the positive literals form the head of the formula:
    fun splitSides(disjunct: Formula): Pair<Set<Formula>, Set<Formula>> {
        return when (disjunct) {
            is Or -> {
                val (leftBody, leftHead) = splitSides(disjunct.left)
                val (rightBody, rightHead) = splitSides(disjunct.right)
                Pair(leftBody + rightBody, leftHead + rightHead)
            }
            is Not -> Pair(setOf(disjunct.formula), emptySet())
            else -> Pair(emptySet(), setOf(disjunct))
        }
    }

    // Convert any disjunct of the formula in CNF to an implication. These implications are geometric sequents.
    fun toImplication(disjunct: Formula): Formula {
        val (bodies, heads) = splitSides(disjunct)
        val body = bodies.fold(Top as Formula, { x, y -> And(x, y) }).simplify() // simplify to get rid of the potentially redundant initial Top
        val head = heads.fold(Bottom as Formula, { x, y -> Or(x, y) }).simplify() // simplify to get rid of the potentially redundant initial Bottom
        return Implies(body, head)
    }

    // Split the CNF form of the formula to a set of disjuncts.
    fun getDisjuncts(cnf: Formula): Set<Formula> {
        return when (cnf) {
            is And -> getDisjuncts(cnf.left) + getDisjuncts(cnf.right)
            else -> setOf(cnf)
        }
    }

    return getDisjuncts(this.cnf(generator)).map { toImplication(it) }.toSet() // convert the CNF form of the formula to geometric
}