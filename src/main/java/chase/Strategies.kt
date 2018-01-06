package chase

import sun.awt.util.IdentityLinkedList

class FIFOStrategy : Strategy {
    private val queue = IdentityLinkedList<Model>()

    override fun iterator(): Iterator<Model> {
        return queue.iterator()
    }

    override fun add(model: Model): Boolean {
        return queue.add(model)
    }

    override fun remove(model: Model): Boolean {
        return queue.remove(model)
    }
}