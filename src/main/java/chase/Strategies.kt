package chase

import sun.awt.util.IdentityLinkedList

class FIFOStrategy<S: Sequent, SEL: Selector<S>> : Strategy<S, SEL> {
    private val queue = IdentityLinkedList<StrategyNode<S, SEL>>()

    override fun iterator(): Iterator<StrategyNode<S, SEL>>  = queue.iterator()

    override fun add(node: StrategyNode<S, SEL>): Boolean {
        return queue.add(node)
    }

    override fun remove(node: StrategyNode<S, SEL>): Boolean {
        return queue.remove(node)
    }
}