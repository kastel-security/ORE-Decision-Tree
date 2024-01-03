package decisiontree.evaluators.heuristic

import decisiontree.util.Counter

object GiniEvaluator : Heuristic {
    private fun <L> getImpurity(c: Counter<L>): Double {
        var impurity = 0.0
        val total = c.totalCount
        for (label in c.allKeys) {
            val proportion = c.getCount(label).toDouble() / total
            impurity += proportion * proportion
        }
        return impurity * total
    }

    override fun <L> evaluate(parent: Counter<L>, first: Counter<L>, second: Counter<L>): Double {
        return getImpurity(first) + getImpurity(second)
    }
}