package decisiontree.evaluators.heuristic

import decisiontree.util.Counter

object EntropyEvaluator : Heuristic {
    private fun log2(v: Double): Double {
        return Math.log(v) / Math.log(2.0)
    }
    private fun <L> getEntropyInDataset(c: Counter<L>): Double {
        var entropy = 0.0
        val total = c.totalCount
        for (label in c.allKeys) {
            val proportion = c.getCount(label).toDouble() / total
            entropy += proportion * log2(proportion)
        }
        return entropy * total
    }

    override fun <L> evaluate(parent: Counter<L>, first: Counter<L>, second: Counter<L>): Double {
        return getEntropyInDataset(first) + getEntropyInDataset(second)
    }
}