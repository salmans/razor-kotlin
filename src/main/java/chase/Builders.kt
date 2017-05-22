package chase

operator fun WitnessFunc.invoke(vararg terms: WitnessTerm) = WitnessApp(this, terms.toList())

operator fun Rel.invoke(vararg elements: Element) = Fact(this, elements.toList())