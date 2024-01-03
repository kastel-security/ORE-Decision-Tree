package decisiontree.evaluators.heuristic

import decisiontree.util.Counter

object ChiSquaredEvaluator : Heuristic {
    private fun chiSquared(actual: Double, expected: Double): Double {
        val diff = actual - expected
        return Math.sqrt(diff * diff / expected)
    }

    private fun getChiSquaredFromCounter(parentCounter: Counter<*>, childCounter: Counter<*>): Double {
        val parentTotal = parentCounter.totalCount.toDouble()
        val childTotal = childCounter.totalCount.toDouble()
        var chiSquared = 0.0
        for (label in parentCounter.allKeys) {
            val expected = parentCounter.getCount(label) / parentTotal
            val actual = childCounter.getCount(label) / childTotal
            chiSquared += chiSquared(actual, expected)
        }
        return chiSquared * childTotal
    }

    override fun <L> evaluate(parent: Counter<L>, first: Counter<L>, second: Counter<L>): Double {
        return getChiSquaredFromCounter(parent, first) + getChiSquaredFromCounter(parent, second)
    }
}