package chase

import sun.awt.util.IdentityLinkedList
import java.util.*

class FIFOStrategy<S: Sequent> : Strategy<S> {
    private val queue = IdentityLinkedList<StrategyNode<S>>()

    override fun isEmpty(): Boolean = queue.isEmpty()

    override fun add(node: StrategyNode<S>): Boolean = queue.add(node)

    override fun remove(): StrategyNode<S>? = if(!isEmpty()) queue.removeFirst() else null
}

class LIFOStrategy<S: Sequent> : Strategy<S> {
    private val queue = Stack<StrategyNode<S>>()

    override fun isEmpty(): Boolean = queue.isEmpty()

    override fun add(node: StrategyNode<S>): Boolean {
        queue.push(node)
        return true
    }

    override fun remove(): StrategyNode<S>? = if(!isEmpty()) queue.pop() else null
}