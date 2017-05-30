package chase

import formula.Formula

interface Sequent<in M: Model> {
    fun evaluate(model: M): Set<Fact>
}
