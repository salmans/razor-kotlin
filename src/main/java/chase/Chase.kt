package chase

interface Sequent<in M: Model> {
    fun evaluate(model: M): Set<Observation>
}
