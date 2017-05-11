package Transform

import Formula.*

/**
 * Applies a substitution function on the term.
 */
fun Term.substitute(substitutions: (Var) -> Term): Term {
    return when (this) {
        is Var -> substitutions(this)
        is App -> App(this.function, this.terms.map { it.substitute(substitutions) })
        else -> throw RuntimeException("Internal Error: Unknown Term")
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
        else -> throw RuntimeException("Internal Error: Unknown Formula")
    }
}

/**
 * Applies a variable renaming function on the term.
 */
fun Term.renameVar(renaming: (Var) -> Var): Term {
    return when (this) {
        is Var -> renaming(this)
        is App -> App(this.function, this.terms.map { it.substitute(renaming) })
        else -> throw RuntimeException("Internal Error: Unknown Term")
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
        else -> throw RuntimeException("Internal Error: Unknown Formula")
    }
}
