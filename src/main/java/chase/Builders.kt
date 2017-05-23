package chase

operator fun WitnessFunc.invoke(vararg terms: WitnessTerm) = WitnessApp(this, terms.toList())

operator fun Rel.invoke(vararg terms: WitnessTerm) = Observation(this, terms.toList())
fun Rel.fact(vararg elements: Element) = Fact(this, elements.toList())