package chase

operator fun WitnessFunc.invoke(vararg terms: WitnessTerm) = WitnessApp(this, terms.toList())

operator fun Rel.invoke(vararg elements: Element) = Observation.Fact(this, elements.toList())
infix fun Element.equals(right: Element) = Observation.Identity(this, right)