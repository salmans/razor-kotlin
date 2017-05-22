package formula

val TRUE = Top
val FALSE = Bottom

operator fun Func.invoke(vararg terms: Term) = App(this, terms.toList())
operator fun Pred.invoke(vararg terms: Term) = Atom(this, terms.toList())
infix fun Term.equals(right: Term) = Equals(this, right)

operator fun Formula.not() = Not(this)
infix fun Formula.and(right: Formula): Formula = And(this, right)
infix fun Formula.or(right: Formula): Formula = Or(this, right)
infix fun Formula.implies(right: Formula): Formula = Implies(this, right)
fun exists(vararg variables: Var, formula: () -> Formula) = Exists(variables.toList(), formula())
fun forall(vararg variables: Var, formula: () -> Formula) = Forall(variables.toList(), formula())