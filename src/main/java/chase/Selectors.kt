package chase

class TopDownSelector<out S : Sequent>(private val sequents: List<S>) : Selector<S> {
    override fun iterator(): Iterator<S> = sequents.iterator()
}

class FairSelector<S : Sequent>(val sequents: Array<S>) : Selector<S> {
    private var index = 0

    inner class FairIterator(private val start: Int) : Iterator<S> {
        var stop = false

        override fun hasNext(): Boolean = !sequents.isEmpty() && (index != start || !stop)
        override fun next(): S {
            if (!hasNext())
                throw NoSuchElementException()
            stop = true
            val sequent = sequents[index]
            index = (index + 1) % sequents.size
            return sequent
        }
    }

    override fun iterator(): Iterator<S> = FairIterator(index)
}