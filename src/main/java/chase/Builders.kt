package chase

operator fun Rel.invoke(vararg elements: Element) = Observation.Fact(this, elements.toList())
infix fun Element.equals(right: Element) = Observation.Identity(this, right)