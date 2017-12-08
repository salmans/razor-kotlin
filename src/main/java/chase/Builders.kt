package chase

operator fun WitnessFunc.invoke(vararg terms: WitnessTerm) = WitnessApp(this, terms.toList())

operator fun Rel.invoke(vararg elements: Element) = Observation(this, elements.toList())
fun Rel.observe(vararg elements: Element) = Observation(this, elements.toList())