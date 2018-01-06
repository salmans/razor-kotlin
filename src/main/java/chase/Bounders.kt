package chase

class DomainSizeBounder(private val maxDomainSize: Int): Bounder {
    override fun bound(model: Model, observation: Observation): Boolean = when (observation) {
        is Observation.Fact -> model.getDomain().size + observation.terms.map { model.element(it) }.filter { it == null }.size >= maxDomainSize
        is Observation.Identity -> {
            fun countOne(value: Element?): Int = if (value == null) 1 else 0
            model.getDomain().size + countOne(model.element(observation.left)) + countOne(model.element(observation.right)) >= maxDomainSize
        }
    }
}
