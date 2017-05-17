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
fun Formula.prenex(): Formula {
    // renames the input variable until it's not in the list of input variables.
    fun renameVar(variable: Var, noCollisionVariableList: Set<Var>): Var {
        var name = variable.name
        val names = noCollisionVariableList.map(Var::name)
        while (name in names) {
            name += "'"
        }
        return Var(name)
    }

    return when (this) {
        is Top -> Top
        is Bottom -> Bottom
        is Atom -> this
        is Equals -> this
        is Not -> {
            val f = formula.prenex()
            when (f) {
                is Forall -> Exists(f.variables, Not(f.formula).prenex())
                is Exists -> Forall(f.variables, Not(f.formula).prenex())
                else -> Not(f)
            }
        }
        is And -> {
            val l = left.prenex()
            val r = right.prenex()

            if (l is Forall) {
                val sharedVariables = l.variables.intersect(r.freeVars)
                val allVariables = (l.variables + freeVars).toSet()
                val renamedL = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Forall((renamedL as Forall).variables, And(renamedL.formula, r).prenex())
            } else if (r is Forall) {
                val sharedVariables = r.variables.intersect(l.freeVars)
                val allVariables = (r.variables + freeVars).toSet()
                val renamedR = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Forall((renamedR as Forall).variables, And(l, renamedR.formula).prenex())
            } else if (l is Exists) {
                val sharedVariables = l.variables.intersect(r.freeVars)
                val allVariables = (l.variables + freeVars).toSet()
                val renamedL = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Exists((renamedL as Exists).variables, And(renamedL.formula, right).prenex())
            } else if (r is Exists) {
                val sharedVariables = r.variables.intersect(l.freeVars)
                val allVariables = (r.variables + freeVars).toSet()
                val renamedR = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Exists((renamedR as Exists).variables, And(l, renamedR.formula).prenex())
            } else And(l, r)
        }
        is Or -> {
            val l = left.prenex()
            val r = right.prenex()
            if (l is Forall) {
                val sharedVariables = l.variables.intersect(r.freeVars)
                val allVariables = (l.variables + freeVars).toSet()
                val renamedL = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Forall((renamedL as Forall).variables, Or(renamedL.formula, r).prenex())
            } else if (r is Forall) {
                val sharedVariables = r.variables.intersect(l.freeVars)
                val allVariables = (r.variables + freeVars).toSet()
                val renamedR = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Forall((renamedR as Forall).variables, Or(l, renamedR.formula).prenex())
            } else if (l is Exists) {
                val sharedVariables = l.variables.intersect(r.freeVars)
                val allVariables = (l.variables + freeVars).toSet()
                val renamedL = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Exists((renamedL as Exists).variables, Or(renamedL.formula, right).prenex())
            } else if (r is Exists) {
                val sharedVariables = r.variables.intersect(l.freeVars)
                val allVariables = (r.variables + freeVars).toSet()
                val renamedR = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Exists((renamedR as Exists).variables, Or(l, renamedR.formula).prenex())
            } else Or(l, r)
        }
        is Implies -> {
            val l = left.prenex()
            val r = right.prenex()
            if (l is Forall) {
                val sharedVariables = l.variables.intersect(r.freeVars)
                val allVariables = (l.variables + freeVars).toSet()
                val renamedL = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Exists((renamedL as Forall).variables, Implies(renamedL.formula, r).prenex())
            } else if (r is Forall) {
                val sharedVariables = r.variables.intersect(l.freeVars)
                val allVariables = (r.variables + freeVars).toSet()
                val renamedR = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Forall((renamedR as Forall).variables, Implies(l, renamedR.formula).prenex())
            } else if (l is Exists) {
                val sharedVariables = l.variables.intersect(r.freeVars)
                val allVariables = (l.variables + freeVars).toSet()
                val renamedL = l.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Forall((renamedL as Exists).variables, Implies(renamedL.formula, right).prenex())
            } else if (r is Exists) {
                val sharedVariables = r.variables.intersect(l.freeVars)
                val allVariables = (r.variables + freeVars).toSet()
                val renamedR = r.renameVar { if (sharedVariables.contains(it)) renameVar(it, allVariables) else it }
                Exists((renamedR as Exists).variables, Implies(l, renamedR.formula).prenex())
            } else Implies(l, r)
        }
        is Forall -> Forall(variables, formula.prenex())
        is Exists -> Exists(variables, formula.prenex())
        else -> throw RuntimeException("Internal Error: Invalid Formula")
    }
}


class SkolemGenerator(private val prefix: String = "sk#") {
    /**
    An index used to generate Skolem functions during Skolemization. This index is global and will not be reset automatically
    after Skolemizing a formula.
     */
    private var skolemIndex = 0

    fun nextFunction() = "$prefix${skolemIndex++}"
}

fun Formula.skolem(generator: SkolemGenerator = SkolemGenerator()): Formula {
    val prenex = this.prenex()

    fun skolemHelper(formula: Formula, skolemVariables: List<Var>): Formula {
        return when (formula) {
            is Forall -> {
                Forall(formula.variables, skolemHelper(formula.formula, skolemVariables + formula.variables))
            }
            is Exists -> {
                val substitutionMap = formula.variables.map { Pair(it, App(Func(generator.nextFunction()), skolemVariables)) }.toMap()
                return skolemHelper(formula.formula.substitute { substitutionMap[it] ?: it }, skolemVariables)
            }
            else -> formula
        }
    }

    return skolemHelper(prenex, emptyList())
}