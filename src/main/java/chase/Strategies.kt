package chase

import sun.awt.util.IdentityLinkedList

class FIFOStrategy : Strategy {
    private val queue = IdentityLinkedList<Model>()

    override fun next(): Model = queue.iterator().next()

    override fun hasNext(): Boolean = queue.iterator().hasNext()

    override fun add(model: Model): Boolean {
        return queue.add(model)
    }

    override fun remove(model: Model): Boolean {
        return queue.remove(model)
    }
}