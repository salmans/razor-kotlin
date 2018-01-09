package chase

import sun.awt.util.IdentityLinkedList

class FIFOStrategy<S: Sequent> : Strategy<S> {
    private val queue = IdentityLinkedList<StrategyNode<S>>()

    override fun iterator(): Iterator<StrategyNode<S>>  = queue.iterator()

    override fun add(node: StrategyNode<S>): Boolean {
        return queue.add(node)
    }

    override fun remove(node: StrategyNode<S>): Boolean {
        return queue.remove(node)
    }
}