package chase

import sun.awt.util.IdentityLinkedList

class FIFOStrategy : Strategy<BasicModel> {
    private val queue = IdentityLinkedList<BasicModel>()

    override fun iterator(): Iterator<BasicModel> {
        return queue.iterator()
    }

    override fun add(model: BasicModel): Boolean {
        return queue.add(model)
    }

    override fun remove(model: BasicModel): Boolean {
        return queue.remove(model)
    }
}