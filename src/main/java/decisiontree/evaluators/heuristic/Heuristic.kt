package decisiontree.evaluators.heuristic

import decisiontree.util.Counter

interface Heuristic {
    fun <L> evaluate(parent: Counter<L>, first: Counter<L>, second: Counter<L>): Double
}