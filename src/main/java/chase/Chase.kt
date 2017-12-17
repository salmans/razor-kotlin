package chase

import formula.Formula
import formula.Substitution
import formula.Theory
import formula.geometric

interface Sequent<M : Model<M>> {
    fun evaluate(model: M, substitution: Substitution): List<List<Observation>>
}

interface Strategy<M : Model<M>> {
    fun hasNext(): Boolean
    fun next(): M?
    fun add(model: M): Boolean
    fun remove(model: M): Boolean
}

interface Evaluator<M : Model<M>, out S : Sequent<M>> {
    fun next(model: M): Pair<S, List<Substitution>>?
}

abstract class Chase<M : Model<M>, S : Sequent<M>, out SL: Evaluator<M, S>, out ST: Strategy<M>> {
    abstract fun emptyModel(): M
    abstract fun newSequent(formula: Formula): S
    abstract fun newSelector(sequents: List<S>): SL
    abstract fun newStrategy(models: List<M>): ST
}

fun <M: Model<M>, S: Sequent<M>, SL: Evaluator<M, S>, ST: Strategy<M>> solve(theory: Theory, chase: Chase<M, S, SL, ST>) {
    val geometricTheory = theory.geometric()
    val sequents = geometricTheory.formulas.map { chase.newSequent(it) }
    val evaluator = chase.newSelector(sequents)
    val initialModel = chase.emptyModel()
    val strategy = chase.newStrategy(listOf(initialModel))

    while (!strategy.hasNext()) {
        val model = strategy.next()!!
        strategy.remove(model)
        val evaluation = evaluator.next(model)
        if (evaluation != null) {
            val (sequent, subs) = evaluation
            subs.forEach {
                val observationsList = sequent.evaluate(model, it)
                observationsList.forEach {
                    val newModel = model.duplicate()
                    it.forEach { newModel.addObservation(it) }
                    strategy.add(newModel)
                }
            }
        } else {
            print(model)
        }

    }
}