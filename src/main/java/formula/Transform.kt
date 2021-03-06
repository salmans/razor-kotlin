package formula


const val EXPECTED_NNF_FORMULA = "Internal Error: Expecting a formula in negation normal form."

/**
 * A Substitution is a function from variables to terms.
 */
typealias Substitution = (Var) -> Term

fun substitutionMap(subMap: Map<Var, Term>): Substitution = { subMap.getOrDefault(it, it) }

/**
 * Applies a substitution function on the term.
 */
fun Term.substitute(substitutions: Substitution): Term = when (this) {
    is Const -> this
    is Var -> substitutions(this)
    is App -> this.copy(terms = this.terms.map { it.substitute(substitutions) })
}

/**
 * Applies a substitution function on a formula.
 */
fun Formula.substitute(substitutions: Substitution): Formula = when (this) {
    Top, Bottom -> this
    is Atom -> this.copy(terms = this.terms.map { it.substitute(substitutions) })
    is Equals -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
    is Not -> this.copy(formula = this.formula.substitute(substitutions))
    is And -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
    is Or -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
    is Implies -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
    is Iff -> this.copy(left = this.left.substitute(substitutions), right = this.right.substitute(substitutions))
    is Exists -> this.copy(variables = this.variables, formula = this.formula.substitute(substitutions))
    is Forall -> this.copy(variables = this.variables, formula = this.formula.substitute(substitutions))
}

/**
 * Applies a variable renaming function on the term.
 */
fun Term.renameVar(renaming: (Var) -> Var): Term = when (this) {
    is Const -> this
    is Var -> renaming(this)
    is App -> this.copy(terms = this.terms.map { it.substitute(renaming) })
}

/**
 * Applies a variable renaming function on a formula.
 */
fun Formula.renameVar(renaming: (Var) -> Var): Formula = when (this) {
    Top, Bottom -> this
    is Atom -> this.copy(terms = terms.map { it.renameVar(renaming) })
    is Equals -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
    is Not -> this.copy(formula = this.formula.renameVar(renaming))
    is And -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
    is Or -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
    is Implies -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
    is Iff -> this.copy(left = this.left.renameVar(renaming), right = this.right.renameVar(renaming))
    is Exists -> this.copy(variables = this.variables.map(renaming), formula = this.formula.renameVar(renaming))
    is Forall -> this.copy(variables = this.variables.map(renaming), formula = this.formula.renameVar(renaming))
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

    return when (this) {
        Top, Bottom, is Atom, is Equals -> this
        is Not -> { // e.g. ~(Qx. P(x)) -> Q' x. ~P(x)
            val f = formula.pnf()
            when (f) {
                is Forall -> Exists(f.variables, Not(f.formula).pnf())
                is Exists -> Forall(f.variables, Not(f.formula).pnf())
                else -> Not(f)
            }
        }
        is And -> { // e.g. (Q x. F(x)) & G(y)) => Q x'. F(x') & G(y) or F(x) & (Q y. G(y)) => Q y'. F(x) & G(y')
            val l = left.pnf()
            val r = right.pnf()
            when {
                l is Forall -> {
                    val sharedVariables = l.variables.intersect(r.freeVars)
                    val allVariables = (l.variables + freeVars).toSet()
                    val renamed = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Forall((renamed as Forall).variables, And(renamed.formula, r).pnf())
                }
                l is Exists -> {
                    val sharedVariables = l.variables.intersect(r.freeVars)
                    val allVariables = (l.variables + freeVars).toSet()
                    val renamed = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Exists((renamed as Exists).variables, And(renamed.formula, r).pnf())
                }
                r is Forall -> {
                    val sharedVariables = r.variables.intersect(l.freeVars)
                    val allVariables = (r.variables + freeVars).toSet()
                    val renamed = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Forall((renamed as Forall).variables, And(l, renamed.formula).pnf())
                }
                r is Exists -> {
                    val sharedVariables = r.variables.intersect(l.freeVars)
                    val allVariables = (r.variables + freeVars).toSet()
                    val renamed = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Exists((renamed as Exists).variables, And(l, renamed.formula).pnf())
                }
                else -> And(l, r)
            }
        }
        is Or -> { // e.g. (Q x. F(x)) | G(y)) => Q x'. F(x') | G(y) or F(x) | (Q y. G(y)) => Q y'. F(x) | G(y')
            val l = left.pnf()
            val r = right.pnf()
            when {
                l is Forall -> {
                    val sharedVariables = l.variables.intersect(r.freeVars)
                    val allVariables = (l.variables + freeVars).toSet()
                    val renamed = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Forall((renamed as Forall).variables, Or(renamed.formula, r).pnf())
                }
                l is Exists -> {
                    val sharedVariables = l.variables.intersect(r.freeVars)
                    val allVariables = (l.variables + freeVars).toSet()
                    val renamed = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Exists((renamed as Exists).variables, Or(renamed.formula, r).pnf())
                }
                r is Forall -> {
                    val sharedVariables = r.variables.intersect(l.freeVars)
                    val allVariables = (r.variables + freeVars).toSet()
                    val renamed = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Forall((renamed as Forall).variables, Or(l, renamed.formula).pnf())
                }
                r is Exists -> {
                    val sharedVariables = r.variables.intersect(l.freeVars)
                    val allVariables = (r.variables + freeVars).toSet()
                    val renamed = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Exists((renamed as Exists).variables, Or(l, renamed.formula).pnf())
                }
                else -> Or(l, r)
            }
        }
        is Implies -> { // e.g. (Q x. F(x)) -> G(y)) => Q' x'. F(x') -> G(y) or F(x) -> (Q y. G(y)) => Q' y'. F(x) -> G(y')
            val l = left.pnf()
            val r = right.pnf()
            when {
                l is Forall -> {
                    val sharedVariables = l.variables.intersect(r.freeVars)
                    val allVariables = (l.variables + freeVars).toSet()
                    val renamed = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Exists((renamed as Forall).variables, Implies(renamed.formula, r).pnf())
                }
                l is Exists -> {
                    val sharedVariables = l.variables.intersect(r.freeVars)
                    val allVariables = (l.variables + freeVars).toSet()
                    val renamed = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Forall((renamed as Exists).variables, Implies(renamed.formula, r).pnf())
                }
                r is Forall -> {
                    val sharedVariables = r.variables.intersect(l.freeVars)
                    val allVariables = (r.variables + freeVars).toSet()
                    val renamed = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Forall((renamed as Forall).variables, Implies(l, renamed.formula).pnf())
                }
                r is Exists -> {
                    val sharedVariables = r.variables.intersect(l.freeVars)
                    val allVariables = (r.variables + freeVars).toSet()
                    val renamed = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                    Exists((renamed as Exists).variables, Implies(l, renamed.formula).pnf())
                }
                else -> Implies(l, r)
            }
        }
        is Iff -> And(Implies(left, right), Implies(right, left)).pnf()
        is Forall -> this.copy(formula = formula.pnf())
        is Exists -> this.copy(formula = formula.pnf())
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
    fun skolemHelper(formula: Formula, skolemVariables: Vars): Formula {
        return when (formula) {
            is Forall -> Forall(formula.variables, skolemHelper(formula.formula, skolemVariables + formula.variables))
            is Exists -> {
                // Notice that it doesn't apply all substitutions at the end but during the recursive call to account for shadowing variable names:
                val substitutionMap = formula.variables.map {
                    Pair(it, when {
                        skolemVariables.isEmpty() -> Const(generator.nextFunction())
                        else -> App(Func(generator.nextFunction()), skolemVariables)
                    })
                }.toMap()
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
fun Formula.nnf(): Formula = when (this) {
    Top, Bottom, is Atom, is Equals -> this
    is Not -> when (this.formula) {
        Top -> Bottom
        Bottom -> Top
        is Atom, is Equals -> this
        is Not -> this.formula.formula
        is And -> Or(Not(this.formula.left).nnf(), Not(this.formula.right).nnf())
        is Or -> And(Not(this.formula.left).nnf(), Not(this.formula.right).nnf())
        is Implies -> And(this.formula.left.nnf(), Not(this.formula.right).nnf())
        is Iff -> Or(And(this.formula.left.nnf(), Not(this.formula.right).nnf()), And(Not(this.formula.left).nnf(), this.formula.right.nnf()))
        is Exists -> Forall(this.formula.variables, Not(this.formula.formula).nnf())
        is Forall -> Exists(this.formula.variables, Not(this.formula.formula).nnf())
    }
    is And -> this.copy(left = this.left.nnf(), right = this.right.nnf())
    is Or -> this.copy(left = this.left.nnf(), right = this.right.nnf())
    is Implies -> Or(Not(this.left).nnf(), this.right.nnf())
    is Iff -> And(Or(Not(this.left).nnf(), this.right.nnf()), Or(this.left.nnf(), Not(this.right).nnf()))
    is Exists -> this.copy(variables = this.variables, formula = this.formula.nnf())
    is Forall -> this.copy(variables = this.variables, formula = this.formula.nnf())
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
            Top, Bottom, is Atom, is Equals, is Not -> formula // because already NNF
            is And -> formula.copy(left = pushOr(formula.left), right = pushOr(formula.right))
            is Or -> {
                val left = pushOr(formula.left)
                val right = pushOr(formula.right)
                when {
                    left is And -> {
                        val l = pushOr(Or(left.left, right))
                        val r = pushOr(Or(left.right, right))
                        And(l, r)
                    }
                    right is And -> {
                        val l = pushOr(Or(left, right.left))
                        val r = pushOr(Or(left, right.right))
                        And(l, r)
                    }
                    else -> Or(left, right)
                }
            }
            is Forall -> formula.copy(formula = pushOr(formula.formula))
            is Implies -> throw RuntimeException(EXPECTED_NNF_FORMULA) // because already NNF
            is Iff -> throw RuntimeException(EXPECTED_NNF_FORMULA) // because already NNF
            is Exists -> throw RuntimeException(EXPECTED_NNF_FORMULA)  // because already SNF
        }
    }

    /**
     * Removes all universal quantifiers (assuming already PNF, SNF and conjunction distributed).
     */
    fun removeForall(formula: Formula): Formula = when (formula) {
        is Forall -> removeForall(formula.formula)
        else -> formula // because already PNF
    }

    return removeForall(pushOr(nnf))
}

/**
 * Applies basic syntactic simplification on the formula.
 */
fun Formula.simplify(): Formula = when (this) {
    Top, Bottom, is Atom, is Equals -> this
    is Not -> {
        val formula = formula.simplify()
        when (formula) {
            Top -> Bottom
            Bottom -> Top
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
        if (left == Top || right == Top) {
            Top
        } else if (right == Bottom) {
            left
        } else if (left == Bottom) {
            right
        } else {
            this.copy(left = left, right = right)
        }
    }
    is Implies -> {
        val left = left.simplify()
        val right = right.simplify()
        if (left == Bottom || right == Top) {
            Top
        } else if (left == Top) {
            right
        } else if (right == Bottom) {
            Not(left).simplify()
        } else {
            this.copy(left = left, right = right)
        }
    }
    is Iff -> {
        val left = left.simplify()
        val right = right.simplify()
        if (left == Bottom || right == Top) {
            Top
        } else if (left == Top) {
            right
        } else if (right == Bottom) {
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
    fun getDisjuncts(cnf: Formula): Set<Formula> = when (cnf) {
        is And -> getDisjuncts(cnf.left) + getDisjuncts(cnf.right)
        else -> setOf(cnf)
    }

    return getDisjuncts(this.cnf(generator)).map { toImplication(it) }.toSet() // convert the CNF form of the formula to geometric
}

fun Theory.geometric(): Theory {
    val generator = SkolemGenerator()
    return Theory(this.formulas.flatMap { it.geometric(generator) })
}