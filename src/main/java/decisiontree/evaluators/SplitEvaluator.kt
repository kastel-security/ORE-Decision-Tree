package decisiontree.evaluators

import decisiontree.data.Dataset
import decisiontree.tree.Condition
import java.util.*

interface SplitEvaluator {
    fun getOptimalCondition(dataset: Dataset<*>): Optional<Condition>
}