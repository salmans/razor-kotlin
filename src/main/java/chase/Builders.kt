package chase

import formula.Func

operator fun Rel.invoke(vararg terms: WitnessTerm) = Observation.Fact(this, terms.toList())
operator fun Func.invoke(vararg terms: WitnessTerm) = WitnessApp(this, terms.toList())
infix fun WitnessTerm.equals(right: WitnessTerm) = Observation.Identity(this, right)