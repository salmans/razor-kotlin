package chase

import formula.TRUE

class TopDownSelector<out S : Sequent>(private val sequents: List<S>) : Selector<S> {
    override fun iterator(): Iterator<S> = sequents.iterator()

    override fun duplicate(): Selector<S> = this // because it always starts from the top, it's stateless and can be shared
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

    private constructor(selector: FairSelector<S>) : this(selector.sequents) { // the array of sequents can be shared
        this.index = index // the index cannot
    }

    override fun duplicate(): Selector<S> = FairSelector(this)
}

class OptimalSelector<out S : Sequent> : Selector<S> {
    inner class OneTimeIterator : Iterator<S> {
        override fun hasNext(): Boolean = index < allSequents.first.size

        override fun next(): S {
            if (!hasNext())
                throw NoSuchElementException()
            return allSequents.first[index++]
        }
    }

    private val allSequents: Pair<List<S>, List<S>>
    private val selector: Selector<S>
    private var index: Int
    private val iterator: Iterator<S>

    constructor(sequents: List<S>, selectorFunction: (sequents: List<S>) -> Selector<S>) {
        this.allSequents = sequents.partition { it.body == TRUE && it.head.freeVars.isEmpty() && it.head.freeVars.isEmpty() }
        this.selector = selectorFunction(this.allSequents.second)
        this.iterator = OneTimeIterator()
        index = 0
    }

    private constructor(selector: OptimalSelector<S>) {
        this.allSequents = selector.allSequents
        this.selector = selector.selector.duplicate()
        this.iterator = OneTimeIterator()
        this.index = selector.index
    }

    override fun iterator(): Iterator<S> = if (iterator.hasNext()) iterator else selector.iterator()

    override fun duplicate(): Selector<S> = OptimalSelector(this)
}