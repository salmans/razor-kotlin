package chase

class TopDownSelector<out S : Sequent>(private val sequents: List<S>) : Selector<S> {
    override fun iterator(): Iterator<S> = sequents.iterator()
}